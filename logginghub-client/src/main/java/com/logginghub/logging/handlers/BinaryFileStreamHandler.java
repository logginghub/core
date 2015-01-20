package com.logginghub.logging.handlers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import com.logginghub.logging.JuliLogEvent;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.LogEventCodex;
import com.logginghub.logging.utils.LoggingUtils;
import com.logginghub.utils.ExpandingByteBuffer;
import com.logginghub.utils.RunnableWorkerThread;
import com.logginghub.utils.WorkerThread;

/**
 * A variation of the JULI file handler that writes log events in binary encoding. This is many
 * times faster than writing formatted log output, but comes at the cost of needing to parse the
 * binary file at a later date. In theory this should be much more effective in the long run, as
 * many more log entries are written but never looked at over time - so there is an argument for
 * optimising the write at the expense of a an extra step when reading.
 * 
 * @author admin
 * 
 */
public class BinaryFileStreamHandler extends Handler implements PropertyChangeListener {
    // private ThreadLocal<LogEvent> m_logEventsByThread = new ThreadLocal<LogEvent>();
    private String m_sourceApplication = "<unknown source application>";
    private LinkedList<LogRecord> m_eventsToBeDispatched = new LinkedList<LogRecord>();
    private boolean m_useDispatchThread = false;
    private Map<LogRecord, String> m_threadNames = new HashMap<LogRecord, String>();
    private OutputStream m_outputStream;
    private ExpandingByteBuffer m_buffer = new ExpandingByteBuffer();
    private String m_outputFilename = "default.log.binary";
    private InetAddress m_host;

    public BinaryFileStreamHandler() {
        try {
            m_host = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e1) {
            throw new RuntimeException("Failed to get local host", e1);
        }

        LogManager manager = LogManager.getLogManager();
        manager.addPropertyChangeListener(this);

        reconfigure();

        if (m_useDispatchThread) {
            Runnable runnable = new Runnable() {
                public void run() {
                    LogRecord record;

                    synchronized (m_eventsToBeDispatched) {
                        while (m_eventsToBeDispatched.isEmpty()) {
                            try {
                                m_eventsToBeDispatched.wait();
                            }
                            catch (InterruptedException e) {}
                        }

                        record = m_eventsToBeDispatched.remove(0);
                    }

                    // LogEvent converted = convert(record);
                    try {
                        JuliLogEvent event = new JuliLogEvent(record, m_sourceApplication, m_host, Thread.currentThread().getName(), true);

                        write(new LogEventMessage(event));
                        // m_publisher.publish(converted);
                        // m_socketClient.send(new LogEventMessage(converted));
                    }
                    catch (IOException ftse) {
                        ftse.printStackTrace();

                        // Put it back on the queue
                        synchronized (m_eventsToBeDispatched) {
                            m_eventsToBeDispatched.addFirst(record);
                        }
                    }
                }
            };

            WorkerThread thread = new RunnableWorkerThread("AsynchronousSocketHandler-DispatchThread", runnable);
            thread.setDaemon(true);
            thread.start();
        }
    }

    // //////////////////////////////////////////////////////////////////
    // Accessors
    // //////////////////////////////////////////////////////////////////

    protected synchronized void write(LogEventMessage logEventMessage) throws IOException {
        LogEventCodex.encode(m_buffer, logEventMessage.getLogEvent());
        m_buffer.flip();
        ensureChannelOpen();
        m_outputStream.write(m_buffer.getContents());
        m_buffer.compact();
    }

    private void ensureChannelOpen() throws IOException {
        if (m_outputStream == null) {
            File file = new File(m_outputFilename);
            m_outputStream = new BufferedOutputStream(new FileOutputStream(file));
        }
    }

    public void setSourceApplication(String sourceApplication) {
        m_sourceApplication = sourceApplication;
    }

    public String getSourceApplication() {
        return m_sourceApplication;
    }

    // //////////////////////////////////////////////////////////////////
    // Handler overrides
    // //////////////////////////////////////////////////////////////////

    @Override public void close() throws SecurityException {
        if (m_outputStream != null) {
            try {
                m_outputStream.close();
            }
            catch (IOException ioe) {
                throw new RuntimeException("Failed to close underlying file channel", ioe);
            }
        }
    }

    @Override public void flush() {

    }

    @Override public void publish(LogRecord record) {
        if (m_useDispatchThread) {
            synchronized (m_eventsToBeDispatched) {
                m_eventsToBeDispatched.add(record);
                m_eventsToBeDispatched.notifyAll();
                m_threadNames.put(record, Thread.currentThread().getName());
            }
        }
        else {
            // LogEvent convert = convert(record);

            JuliLogEvent event = new JuliLogEvent(record, m_sourceApplication, m_host, Thread.currentThread().getName(), true);

            // m_publisher.publish(convert);
            // convert.setThreadName(Thread.currentThread().getName());
            try {
                // write(new LogEventMessage(convert));
                write(new LogEventMessage(event));
                // m_socketClient.send(new LogEventMessage(convert));
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to send log event", e);
            }
        }
    }

    // //////////////////////////////////////////////////////////////////
    // The LogManager property change implementation
    // //////////////////////////////////////////////////////////////////

    public void propertyChange(PropertyChangeEvent evt) {
        reconfigure();
    }

    // //////////////////////////////////////////////////////////////////
    // Private methods
    // //////////////////////////////////////////////////////////////////

    /*
     * private LogEvent getEventForThread() { LogEvent fullLogEvent = m_logEventsByThread.get();
     * 
     * if(fullLogEvent == null) { fullLogEvent = new LogEvent();
     * m_logEventsByThread.set(fullLogEvent); }
     * 
     * return fullLogEvent; }
     */

    private void reconfigure() {
        LogManager manager = LogManager.getLogManager();

        String cname = getClass().getName();

        Level level = LoggingUtils.getLevelProperty(cname + ".level");
        if (level != null) {
            setLevel(level);
        }

        String applicationNameProperty = manager.getProperty(cname + ".applicationName");
        if (applicationNameProperty != null) {
            setSourceApplication(applicationNameProperty);
        }

        // String property = manager.getProperty(cname + ".connectionPoints");
        // if(property != null)
        // {
        // List<InetSocketAddress> parseAddressAndPortList =
        // LoggingUtils.parseAddressAndPortList(property,
        // SocketHub.defaultPort);
        // m_socketClient.replaceConnectionList(parseAddressAndPortList);
        // }
    }

    // //////////////////////////////////////////////////////////////////
    // Protected methods
    // //////////////////////////////////////////////////////////////////

    /**
     * @return the publisher instance.
     */
    // private SocketPublisher getPublisher()
    // {
    // return m_publisher;
    // }
    /**
     * Convert a LogRecord into a LogEvent, uses a thread local so its nice and fast
     * 
     * @param record
     * @return
     */
    /*
     * private LogEvent convert(LogRecord record) { LogEvent eventForThread = getEventForThread();
     * eventForThread.populateFromLogRecord(record, m_sourceApplication);
     * eventForThread.setThreadName(m_threadNames.remove(record)); return eventForThread; }
     */

    public void waitUntilAllRecordsHaveBeenPublished() {
        boolean done = false;

        while (!done) {
            synchronized (m_eventsToBeDispatched) {
                done = m_eventsToBeDispatched.isEmpty();
            }

            if (!done) {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {}
            }
        }
    }

    public void setUseDispatchThread(boolean value) {
        m_useDispatchThread = value;

    }

    /**
     * Sets the filename for the binary output, closing the existing file if it is already open. The
     * next event will be written to the new filename.
     */
    public void setOutputFilename(String string) {
        m_outputFilename = string;
        close();
    }
}
