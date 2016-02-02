package com.logginghub.logging.messaging;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.FilteredMessageSender;
import com.logginghub.logging.interfaces.LoggingMessageSource;
import com.logginghub.logging.interfaces.QueueAwareLoggingMessageSender;
import com.logginghub.logging.listeners.ConnectionListener;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.ConnectedMessage;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.PartialMessageException;
import com.logginghub.utils.*;
import com.logginghub.utils.ExceptionPolicy.Policy;
import com.logginghub.utils.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Connection handler for socket based logging messaging connections. As both reading and writing are potentially
 * blocking operations, each connection maintains two worker threads to manage IO.
 *
 * @author admin
 */
public class SocketConnection implements LoggingMessageSource,
                                         FilteredMessageSender,
                                         Destination<LogEvent>,
                                         QueueAwareLoggingMessageSender,
                                         SocketConnectionInterface {
    public enum SlowSendingPolicy {
        disconnect,
        block,
        discard
    }

    public final static int writeBufferDefaultSize = 20000;
    public final static int CONNECTION_TYPE_NORMAL = 0;
    public final static int CONNECTION_TYPE_HUB_BRIDGE = 1;
    private static final Logger logger = Logger.getLoggerFor(SocketConnection.class);
    private static AtomicInteger nextThread = new AtomicInteger();
    private boolean debug = false;
    // private ExceptionHandler exceptionHandler;
    private WorkerThread readerThread;
    private WorkerThread writerThread;
    private LinkedBlockingQueue<LoggingMessage> writeQueue;
    private Object closeLock = new Object();
    private volatile boolean closing = false;
    private SlowSendingPolicy slowSendingPolicy = SlowSendingPolicy.disconnect;
    private ExceptionPolicy exceptionPolicy = new ExceptionPolicy(Policy.SystemErr);
    private Throttler throttler = new Throttler(10, TimeUnit.SECONDS);
    /**
     * Each sweep we use this list to store the messages to be sent - I'm not sure if its going to be quicker to create
     * a new empty list than it is to clear it down
     */
    private List<LoggingMessage> temporaryMessagesToSendBuffer = new ArrayList<LoggingMessage>();
    private List<LoggingMessageListener> messageListeners = new CopyOnWriteArrayList<LoggingMessageListener>();
    private Socket socket;
    private ExpandingByteBuffer sendBuffer = new ExpandingByteBuffer();
    private ExpandingByteBuffer receiveBuffer = new ExpandingByteBuffer();
    private byte[] fixedReceiverBuffer = new byte[4096];
    private OutputStream outputStream;
    private InputStream inputStream;
    private LoggingMessageCodex codex = new LoggingMessageCodex();
    // private boolean blockingSends = true;
    private List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<ConnectionListener>();
    private boolean forceFlush = false;
    private String name;
    private int messagesDiscarded;
    private Timer timer;
    private int messagesReceived;
    private int messagesSent;
    private boolean statusLogging;
    private int levelFilter = Level.ALL.intValue();
    // TODO : what is the impact of changing these to true? They do have to
    // default to true so that processes are not kept alive just by logging
    private boolean readThreadDaemon = true;
    private boolean writeThreadDaemon = true;
    private Destination<LoggingMessage> messageDestination = new Destination<LoggingMessage>() {
        public void send(LoggingMessage t) {
            try {
                logger.fine("Routing message '{}' to connection '{}'", t, SocketConnection.this);
                SocketConnection.this.send(t);
            } catch (LoggingMessageSenderException e) {
                // TODO : shouldn't we disconnect people if the message doesn't
                // send?
                exceptionPolicy.handle(e);
            }
        }
    };
    private IntegerStat messagesOut;
    private int queuedCounter;
    private int sentCounter;
    private int connectionID = -1;
    private int connectionType = CONNECTION_TYPE_NORMAL;
    private String connectionDescription = "";

    // private int connectionID;

    public SocketConnection(Socket socket) throws IOException {
        this(socket, Integer.toString(nextThread.getAndIncrement()));
    }

    /**
     * @param socket In order to ensure you dont miss any messages, it is important to add a listener at construction
     *               time.
     * @throws IOException if something goes wrong getting the socket io input and output streams
     */
    public SocketConnection(Socket socket, String name) throws IOException {
        this.socket = socket;

        // TODO : if we are force flush we can get a performance boost on bigger messages with
        // these?
        // inputStream = new BufferedInputStream(socket.getInputStream());
        // outputStream = new BufferedOutputStream(socket.getOutputStream());

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        this.name = name;

        setWriteQueueMaximumSize(writeBufferDefaultSize);
    }

    public boolean isReadThreadDaemon() {
        return readThreadDaemon;
    }

    public void setReadThreadDaemon(boolean readThreadDaemon) {
        this.readThreadDaemon = readThreadDaemon;
    }

    public boolean isWriteThreadDaemon() {
        return writeThreadDaemon;
    }

    public void setWriteThreadDaemon(boolean writeThreadDaemon) {
        this.writeThreadDaemon = writeThreadDaemon;
    }

    public void setSlowSendingApproach(SlowSendingPolicy slowSendingApproach) {
        this.slowSendingPolicy = slowSendingApproach;
    }

    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    public void start() {
        startReaderThread();
        startWriterThread();
        if (statusLogging) {
            startTimer();
        }
    }

    public void stop() {
        if (!isClosing()) {
            close("The socketConnection thread was stopped");

            if (readerThread != null) {
                readerThread.stop();
            }

            if (writerThread != null) {
                writerThread.stop();
            }
        }

        stopTimer();
    }

    private void startReaderThread() {

        if (readerThread != null) {
            throw new RuntimeException("You've started the socket connection reader thread already.");
        }

        readerThread = new WorkerThread("LoggingHub-SocketConnection-Reader-" + this.name) {
            @Override
            protected void onRun() throws Throwable {
                readMessages();
            }
        };
        readerThread.setDaemon(isReadThreadDaemon());
        readerThread.start();
    }

    private void startWriterThread() {

        if (writerThread != null) {
            throw new RuntimeException("You've started the socket connection writer thread already.");
        }

        writerThread = new WorkerThread("LoggingHub-SocketConnection-Writer-" + this.name) {
            @Override
            protected void onRun() throws Throwable {
                writeMessages();
            }
        };

        writerThread.setDaemon(isWriteThreadDaemon());
        writerThread.start();
    }

    public void waitForSend() {
        ThreadUtils.untilTrue(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return writeQueue.isEmpty();
            }
        });
    }

    protected void writeMessages() {
        temporaryMessagesToSendBuffer.clear();

        try {
            // This run from a worker thread, so its nice to return back there
            // every now and again otherwise
            LoggingMessage nextMessage = writeQueue.poll(1, TimeUnit.SECONDS);
            if (nextMessage != null) {
                // We got one, lets see if there are any more
                temporaryMessagesToSendBuffer.add(nextMessage);
                writeQueue.drainTo(temporaryMessagesToSendBuffer);

                for (LoggingMessage message : temporaryMessagesToSendBuffer) {
                    try {
                        sendInternal(message);
                    } catch (IllegalArgumentException iae) {
                        throw iae;
                    } catch (RuntimeException t) {
                        if (!closing) {
                            close("An exception was caught sending data to the stream : " + StacktraceUtils.combineMessages(
                                    t));
                            exceptionPolicy.handle(t, "An exception was caught sending data to the stream");
                        }
                        break;
                    } catch (IOException t) {
                        if (!closing) {
                            close("An exception was caught sending data to the stream : " + StacktraceUtils.combineMessages(
                                    t));
                            exceptionPolicy.handle(t, "An exception was caught sending data to the stream");
                        }
                        break;
                    }
                }
            }
        } catch (InterruptedException e1) {
            // This is ok, just loop around again
        }
    }

    private void closeSocketAndStreams() {
        try {
            inputStream.close();
        } catch (IOException e) {
            logger.warn(e, "Failed to close input stream during socket close operation");
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            logger.warn(e, "Failed to close output stream during socket close operation");
        }

        try {
            socket.close();
        } catch (IOException e) {
            logger.warn(e, "Failed to close socket during socket close operation");
        }
    }

    protected void readMessages() {
        try {
            int read = inputStream.read(fixedReceiverBuffer);
            if (read != -1) {
                if (debug) {
                    Out.out("{} | Read {} bytes", name, read);
                }
                receiveBuffer.put(fixedReceiverBuffer, 0, read);
                logger.finest("Read '{}' bytes from the input stream, and appended to buffer '{}'",
                              read,
                              receiveBuffer);
                processReceiveBuffer();
            } else {
                close("The incomming stream closed gracefully");
            }
        } catch (IOException e) {
            if (!closing) {
                close("An exception was caught whilst reading from the stream :" + e.getMessage());
                exceptionPolicy.handle(e, "An exception was caught reading data from the stream");
            }
        } catch (Throwable ioe) {
            logger.warn(ioe, "A non-io exception was caught processing a message");
        }
    }

    private void processReceiveBuffer() {
        ByteBuffer buffer = receiveBuffer.getBuffer();
        buffer.flip();

        if (logger.willLog(Logger.finer)) {
            logger.finer("Incomming receive buffer {}", buffer);
        }

        if (logger.willLog(Logger.finest)) {
            logger.finest("Incomming receive buffer content {}", HexDump.format(buffer));
        }

        int position = 0;
        try {
            while (buffer.hasRemaining() && !isClosing()) {
                position = buffer.position();
                LoggingMessage message = codex.decode(buffer);
                if (message != null) {
                    processMessage(message);
                } else {
                    // If the message was null, it means the codex had to reject
                    // something in the buffer as it couldn't decode it
                }
            }
        } catch (PartialMessageException e) {
            // Fine, need to read more shit, set the position back
            buffer.position(position);
        }

        buffer.compact();
    }

    private void processMessage(LoggingMessage message) {
        messagesReceived++;

        if (message instanceof ConnectedMessage) {
            ConnectedMessage connectedMessage = (ConnectedMessage) message;
            logger.fine("Received connected message, connection ID is {}", connectedMessage.getConnectionID());
            this.connectionID = connectedMessage.getConnectionID();
        }

        try {
            fireNewMessage(message);
        } catch (RuntimeException t) {
            logger.warn(t,
                        "Exception caught processing message '{}' - we should be able to continue after this however",
                        message);
        }
    }

    //    public void sendDirectWithNoQueue(LoggingMessage message) throws LoggingMessageSenderException {
    //        if (isClosing()) {
    //
    //        } else {
    //            try {
    //                sendInternal(message);
    //            } catch (IOException e) {
    //                throw new LoggingMessageSenderException(e);
    //            }
    //        }
    //    }

    // protected void sendSerialisableObject(SerialisableObject t) throws
    // LoggingMessageSenderException {
    // send(new SerialisableObjectWrapper(t));
    // }

    public void send(LoggingMessage message) throws LoggingMessageSenderException {
        if (isClosing()) {
            // This is protected against the race condition where we've killed
            // the threads that are pulling things from the blockingSends queue
            // but we've not quite notified the outside world we've closed. So
            // the silly muppets will still be sending us stuff until we tell
            // them otherwise. If we try adding things to the blocking queue
            // however we are in real danger or blocking forever, as that queue
            // may never be cleared down
        } else {
            // There are two options now - we either block waiting to send or we
            // start throwing away messages. Blocking is necessary when you dont
            // want to loose any messages - ie in debugging - but in production
            // we probably want to throw stuff away. In that case we need a
            // message to flag up the fact we aren't able to send fast enough...

            if (slowSendingPolicy == SlowSendingPolicy.block) {
                try {
                    queuedCounter++;
                    writeQueue.put(message);
                } catch (InterruptedException e) {
                    throw new RuntimeException(
                            "This thread was interupted waiting to post to the write queue, which was blocked as we aren't able to send events as quickly as we are generating them");
                }

            } else {
                if (!writeQueue.offer(message)) {
                    if (slowSendingPolicy == SlowSendingPolicy.disconnect) {
                        close("The internal write queue was full ('{}' items); the current policy is to disconnect when this occurs",
                              writeQueue.size());
                        // throw new
                        // LoggingMessageSenderException("The write queue was full when we tried to send a message, and the policy is to disconnect when this happens");
                    } else if (slowSendingPolicy == SlowSendingPolicy.discard) {
                        while (!writeQueue.offer(message)) {
                            writeQueue.poll();
                            messagesDiscarded++;
                            if (throttler.isOkToFire()) {
                                System.err.println(
                                        "The LoggingHub socket connection write queue is full, and we've discarded '" +
                                                messagesDiscarded +
                                                "' event(s) (total since the process started). This message will only be repeat once every 10 seconds if the send rate continues to overwhelm the write queue.");
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isClosing() {
        synchronized (closeLock) {
            return closing;
        }
    }

    public synchronized void sendInternal(LoggingMessage message) throws IOException {
        if (logger.willLog(Logger.finer)) {
            logger.finer("Sending message '{}', sendBuffer is currently '{}'", message, sendBuffer);
        }

        codex.encode(sendBuffer, message);

        if (logger.willLog(Logger.finer)) {
            logger.finer("Codex encoded to sendBuffer '{}'", sendBuffer);
        }

        sendBuffer.flip();

        sendBufferInternal(sendBuffer, message);

        if (messagesOut != null) {
            messagesOut.increment();
        }

        sentCounter++;
    }

    public void sendBufferInternal(ExpandingByteBuffer buffer, LoggingMessage message) throws IOException {
        byte[] contents = buffer.getContents();

        if (logger.willLog(Logger.finest)) {
            logger.finest("Send buffer contents {}", HexDump.format(contents));
        }

        logger.fine("Sending message '{}' to '{}', encoded size is {} bytes", message, socket, contents.length);

        outputStream.write(contents);

        if (debug) {
            Out.out("{} | wrote {} bytes", name, contents.length);
        }

        if (forceFlush) {
            outputStream.flush();
        }

        logger.finer("Sent {} bytes (force flush was {})", contents.length, forceFlush);

        messagesSent++;

        buffer.compact();

        logger.finer("Send buffer is now '{}'", buffer);
    }

    private void stopThreads() {
        readerThread.stop();
        writerThread.stop();

        readerThread = null;
        writerThread = null;
    }

    @Override
    public String toString() {
        return "[SocketConnection socket=" + socket + "]";
    }

    public void close() {
        close("close() was called");
    }

    private void close(String reason, Object... params) {
        boolean needToAct;

        if (debug) {
            Out.out("{} | Closing socket connection {} : {}", name, System.identityHashCode(this), reason);
        }

        synchronized (closeLock) {
            if (!closing) {
                needToAct = true;
                closing = true;
            } else {
                needToAct = false;
            }
        }

        if (needToAct) {
            // jshaw - this is the new concept for stopping threads. When we shut the socket and
            // streams before stopping the threads we can get into a race condition where they may
            // try and access the resources again before they actually stop properly. So we pre-stop
            // them, without actually joining the threads to wait for them to die.
            if (readerThread != null) {
                readerThread.dontRunAgain();
            }

            if (writerThread != null) {
                writerThread.dontRunAgain();
            }

            if (debug) {
                Out.out("{} | Threads ready to die", name);
            }

            closeSocketAndStreams();
            if (debug) {
                Out.out("{} | Closed socket", name);
            }

            stopThreads();
            if (debug) {
                Out.out("{} | Threads dead", name);
            }

            clearBuffers();
            if (debug) {
                Out.out("{} | Buffers cleared ready for gc - notifying", name);
            }

            fireConnectionClosed(StringUtils.format(reason, params));
            if (debug) {
                Out.out("{} | Close notification complete", name);
            }
        }
    }

    private void clearBuffers() {
        sendBuffer.clear();
        receiveBuffer.clear();
        writeQueue.clear();
    }

    private void fireConnectionClosed(String reason) {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onConnectionClosed(reason);
        }
    }

    public void setForceFlush(boolean forceFlush) {
        this.forceFlush = forceFlush;
    }

    private void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logStatus();
            }
        }, 1000, 1000);
    }

    protected void logStatus() {
        logger.info("Socket connection status : {}, {} messages in, {} message out, {} messages discarded",
                    socket,
                    messagesReceived,
                    messagesSent,
                    messagesDiscarded);
        messagesSent = 0;
        messagesReceived = 0;
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void setStatusLogging(boolean statusLogging) {
        this.statusLogging = statusLogging;
    }

    public void addLoggingMessageListener(LoggingMessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeLoggingMessageListener(LoggingMessageListener listener) {
        messageListeners.remove(listener);
    }

    protected void fireNewMessage(LoggingMessage message) {
        for (LoggingMessageListener listener : messageListeners) {
            listener.onNewLoggingMessage(message);
        }
    }

    public void flush() {
        if (outputStream != null) {
            try {
                outputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException("Failed to flush output stream", e);
            }
        }
    }

    public void setWriteQueueMaximumSize(int writeQueueMaximumSize) {
        writeQueue = new LinkedBlockingQueue<LoggingMessage>(writeQueueMaximumSize);
    }

    public void setWriteQueueOverflowPolicy(SlowSendingPolicy policy) {
        slowSendingPolicy = policy;
    }

    public LinkedBlockingQueue<LoggingMessage> getWriteQueue() {
        return writeQueue;
    }

    public int getLevelFilter() {
        return levelFilter;
    }

    public void setLevelFilter(int levelFilter) {
        this.levelFilter = levelFilter;
    }

    @Override
    public int getConnectionType() {
        return connectionType;
    }

    @Override
    public void setConnectionType(int i) {
        this.connectionType = i;
    }

    @Override
    public String getConnectionDescription() {
        return connectionDescription;
    }

    @Override
    public void setConnectionDescription(String description) {
        this.connectionDescription = description;
    }

    public Socket getSocket() {
        return socket;
    }

    public void send(LogEvent logEvent) {
        // TODO : could move this to a filter that is connected rather than embedded?
        if (logEvent.getLevel() >= getLevelFilter()) {
            try {
                send(new LogEventMessage(logEvent));
            } catch (LoggingMessageSenderException e) {
                exceptionPolicy.handle(e, "Exception caught sending a log event to the connection");
            }
        } else {
            logger.finer("Connection {} has a level filter that blocks this event ({} < {})",
                         this,
                         getLevelFilter(),
                         logEvent.getLevel());
        }
    }

    public ExceptionPolicy getExceptionPolicy() {
        return exceptionPolicy;
    }

    public void setExceptionPolicy(ExceptionPolicy exceptionPolicy) {
        this.exceptionPolicy = exceptionPolicy;
    }

    public Destination<LoggingMessage> getMessageDestination() {
        return messageDestination;
    }

    public boolean isSendQueueEmpty() {
        return writeQueue.size() == 0;
    }

    public void setMessagesOutCounter(IntegerStat messagesOut) {
        this.messagesOut = messagesOut;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getQueuedCounter() {
        return queuedCounter;
    }

    public int getSentCounter() {
        return sentCounter;
    }

    public int getConnectionID() {
        return connectionID;
    }

    public void setConnectionID(int connectionID) {
        this.connectionID = connectionID;
    }


}
