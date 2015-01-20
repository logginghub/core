package com.logginghub.logging.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.interfaces.LoggingMessageSource;
import com.logginghub.logging.listeners.ConnectionListener;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.PartialMessageException;
import com.logginghub.utils.ExpandingByteBuffer;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.HexDump;
import com.logginghub.utils.WorkerThread;

/**
 * Connection handler for socket based logging messaging connections. As both reading and writing are potentially blocking operations, each connection maintains two worker threads to manage IO.
 * 
 * @author admin
 */
public class OldSocketConnection implements LoggingMessageSource, LoggingMessageSender {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private WorkerThread readerThread;
    private WorkerThread writerThread;
    private LinkedBlockingQueue<LoggingMessage> writeQueue;
    private Object closeLock = new Object();
    private volatile boolean closing = false;

    /**
     * Each sweep we use this list to store the messages to be sent - I'm not sure if its going to be quicker to create a new empty list than it is to clear it down
     */
    private List<LoggingMessage> temporaryMessagesToSendBuffer = new ArrayList<LoggingMessage>();
    private List<LoggingMessageListener> messageListeners = new CopyOnWriteArrayList<LoggingMessageListener>();

    private int writeBufferSize = 10000;
    private Socket socket;

    private ExpandingByteBuffer sendBuffer = new ExpandingByteBuffer();
    private ExpandingByteBuffer receiveBuffer = new ExpandingByteBuffer();

    private byte[] fixedReceiverBuffer = new byte[4096];

    private OutputStream outputStream;
    private InputStream inputStream;

    private LoggingMessageCodex codex = new LoggingMessageCodex();

    private List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<ConnectionListener>();
    private static AtomicInteger nextThread = new AtomicInteger();

    private boolean forceFlush = false;
    private boolean blockingSends = true;

    private String name;

    private int messagesDiscarded;

    private Timer timer;

    private int messagesReceived;

    private int messagesSent;

    private boolean statusLogging;

    private int connectionID;

    public OldSocketConnection(Socket socket) throws IOException {
        this(socket, Integer.toString(nextThread.getAndIncrement()));
    }

    /**
     * 
     * @param socket
     * @param listener
     *            In order to ensure you dont miss any messages, it is important to add a listener at construction time.
     * @throws IOException
     *             if something goes wrong getting the socket io input and output streams
     */
    public OldSocketConnection(Socket socket, String name) throws IOException {
        this.socket = socket;

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        this.name = name;

        writeQueue = new LinkedBlockingQueue<LoggingMessage>(writeBufferSize);
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
        if (readerThread != null) {
            readerThread.stop();
        }

        if (writerThread != null) {
            writerThread.stop();
        }

        stopTimer();
    }

    private void startReaderThread() {

        if (readerThread != null) {
            throw new RuntimeException("You've started the socket connection reader thread already.");
        }

        readerThread = new WorkerThread("SocketConnection-Reader-" + this.name) {
            @Override protected void onRun() throws Throwable {
                readMessages();
            }
        };
        readerThread.start();
    }

    private void startWriterThread() {

        if (writerThread != null) {
            throw new RuntimeException("You've started the socket connection writer thread already.");
        }

        writerThread = new WorkerThread("SocketConnection-Writer-" + this.name) {
            @Override protected void onRun() throws Throwable {
                writeMessages();
            }
        };
        writerThread.start();
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
                    }
                    catch (LoggingMessageSenderException e) {
                        logger.log(Level.INFO, "Failed to send message : " + e.getCause().getMessage());
                        break;
                    }
                }
            }
        }
        catch (InterruptedException e1) {}
    }

    protected void readMessages() {
        try {
            int read = inputStream.read(fixedReceiverBuffer);
            if (read != -1) {
                receiveBuffer.put(fixedReceiverBuffer, 0, read);

                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest(String.format("Read '%d' bytes from the input stream, and appended to buffer '%s'", read, receiveBuffer));
                }

                processReceiveBuffer();
            }
            else {
                stopThreads();
                close();
            }
        }
        catch (IOException ioe) {
            stopThreads();
            if (!closing) {
                close();
            }
        }
    }

    private void processReceiveBuffer() {
        ByteBuffer buffer = receiveBuffer.getBuffer();
        buffer.flip();

        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Incomming receive buffer contenxt", HexDump.format(buffer));
        }

        int position = 0;
        try {
            while (buffer.hasRemaining()) {
                position = buffer.position();
                LoggingMessage message = codex.decode(buffer);
                processMessage(message);
            }
        }
        catch (PartialMessageException e) {
            // Fine, need to read more shit, set the position back
            buffer.position(position);
        }

        buffer.compact();
    }

    private void processMessage(LoggingMessage message) {
        fireNewMessage(message);
        messagesReceived++;
    }

    public void send(LoggingMessage message) throws LoggingMessageSenderException {
        // There are two options now - we either block waiting to send or we
        // start throwing away messages. Blocking is necessary when you dont
        // want to loose any messages - ie in debugging - but in production we
        // probably want to throw stuff away. In that case we need a message to
        // flag up the fact we aren't able to send fast enough...
        if (blockingSends) {
            try {
                writeQueue.put(message);
            }
            catch (InterruptedException e) {
                throw new RuntimeException("This thread was interupted waiting to post to the write queue, which was blocked as we aren't able to send events as quickly as we are generating them");
            }
        }
        else {
            if (!writeQueue.offer(message)) {
                // The queue is full, flag this up so the next time we do send
                // something we tell the world we can't keep up
                messagesDiscarded++;
            }
        }
    }

    private void sendInternal(LoggingMessage message) throws LoggingMessageSenderException {
        if (logger.isLoggable(Level.FINER)) {
            logger.fine(String.format("Sending message '%s', sendBuffer is currently '%s'", message, sendBuffer));
        }

        codex.encode(sendBuffer, message);

        if (logger.isLoggable(Level.FINER)) {
            logger.fine(String.format("Codex encoded to sendBuffer '%s'", sendBuffer));
        }

        sendBuffer.flip();

        byte[] contents = sendBuffer.getContents();

        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Send buffer contents", HexDump.format(contents));
        }

        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(String.format("Sending message '%s' to '%s', encoded size is %d bytes", message, socket, contents.length));
            }

            outputStream.write(contents);
            if (forceFlush) {
                outputStream.flush();
            }

            if (logger.isLoggable(Level.FINER)) {
                logger.finer(String.format("Sent %d bytes", contents.length));
            }

            messagesSent++;

        }
        catch (IOException ioe) {
            stopThreads();
            close();
            throw new LoggingMessageSenderException("Failed to send message due to socket IO problem", ioe);
        }

        sendBuffer.compact();

        if (logger.isLoggable(Level.FINER)) {
            logger.fine(String.format("Send buffer is now '%s'", sendBuffer));
        }
    }

    private void stopThreads() {
        readerThread.stop();
        writerThread.stop();
    }

    @Override public String toString() {
        return "[SocketConnection socket=" + socket + "]";
    }

    public void close() {
        boolean needToAct;

        synchronized (closeLock) {
            if (!closing) {
                needToAct = true;
                closing = true;
            }
            else {
                needToAct = false;
            }
        }

        if (needToAct) {
            FileUtils.closeQuietly(socket);
            readerThread.stop();
            writerThread.stop();
            sendBuffer.clear();
            receiveBuffer.clear();

            fireConnectionClosed();
        }
    }

    private void fireConnectionClosed() {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onConnectionClosed("");
        }
    }

    public void setForceFlush(boolean forceFlush) {
        this.forceFlush = forceFlush;
    }

    private void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override public void run() {
                logStatus();
            }
        }, 1000, 1000);
    }

    protected void logStatus() {
        logger.info(String.format("Socket connection status : %s, %d messages in, %d message out, %d messages discarded", socket, messagesReceived, messagesSent, messagesDiscarded));
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
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to flush output stream", e);
            }
        }
    }
}
