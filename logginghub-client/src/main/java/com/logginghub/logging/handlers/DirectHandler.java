package com.logginghub.logging.handlers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import com.logginghub.logging.JuliLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.utils.LoggingUtils;

public class DirectHandler extends Handler implements PropertyChangeListener {
    private ThreadLocal<LogEvent> m_logEventsByThread = new ThreadLocal<LogEvent>();
    private String m_sourceApplication = "<unknown source application>";
    private LogEventListener m_destination;
    private boolean m_reuseLogEvents = true;
    private InetAddress m_host;

    public DirectHandler(LogEventListener destination) {
        LogManager manager = LogManager.getLogManager();
        manager.addPropertyChangeListener(this);

        m_destination = destination;

        reconfigure();

        try {
            m_host = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e1) {
            throw new RuntimeException("Failed to get local host", e1);
        }
    }

    // //////////////////////////////////////////////////////////////////
    // Accessors
    // //////////////////////////////////////////////////////////////////

    public void setSourceApplication(String sourceApplication) {
        m_sourceApplication = sourceApplication;
    }

    public String getSourceApplication() {
        return m_sourceApplication;
    }

    // //////////////////////////////////////////////////////////////////
    // Handler overrides
    // //////////////////////////////////////////////////////////////////

    @Override public void close() throws SecurityException {}

    @Override public void flush() {

    }

    @Override public void publish(LogRecord record) {
        // LogEvent eventForThread = getEventForThread();
        // eventForThread.populateFromLogRecord(record, m_sourceApplication);

        JuliLogEvent event = new JuliLogEvent(record, m_sourceApplication, m_host, Thread.currentThread().getName(), true);

        m_destination.onNewLogEvent(event);
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
     * private LogEvent getEventForThread() { LogEvent fullLogEvent; if(m_reuseLogEvents) {
     * fullLogEvent = m_logEventsByThread.get(); } else { fullLogEvent = new LogEvent(); }
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
    }

    /**
     * Handlers commonly reuse log events on thread locals to avoid instantiation costs. For in
     * memory situations where log events are collected in any way, this is a bad thing, so set this
     * false.
     * 
     * @param b
     */
    public void setResuseLogEvents(boolean reuseEvents) {
        m_reuseLogEvents = reuseEvents;
    }
}
