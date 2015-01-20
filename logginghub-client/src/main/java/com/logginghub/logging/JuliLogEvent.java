package com.logginghub.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * LogEvent wrapper for the juli log record object.
 * 
 * @author admin
 */
public class JuliLogEvent extends BaseLogEvent {
    private LogRecord record;

    private static String lineSeparator = (String) System.getProperty("line.separator");

    private boolean gatheringCallerDetails;

    public JuliLogEvent(LogRecord record, String sourceApplication, InetAddress sourceHost, String threadName, boolean gatheringCallerDetails) {
        super();
        this.record = record;
        this.gatheringCallerDetails = gatheringCallerDetails;
        setSourceApplication(sourceApplication);
        setSourceHost(sourceHost.getHostName());
        setSourceAddress(sourceHost.getHostAddress());
        setThreadName(threadName);
    }

    public static String formatException(Throwable thrown) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        // sw.append(thrown.getMessage());
        // sw.append(lineSeparator);
        thrown.printStackTrace(pw);
        return sw.toString();
    }

    public String getFormattedException() {
        String formatted;

        Throwable thrown = record.getThrown();
        if (thrown != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            sw.append(thrown.getMessage());
            sw.append(lineSeparator);
            thrown.printStackTrace(pw);
            formatted = sw.toString();
        }
        else {
            formatted = null;
        }

        return formatted;
    }

    public String[] getFormattedObject() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getLevel() {
        return record.getLevel().intValue();
    }

    public long getOriginTime() {
        return record.getMillis();
    }

    public String getLoggerName() {
        return record.getLoggerName();
    }

    public String getMessage() {
        return record.getMessage();
    }

    public long getSequenceNumber() {
        return record.getSequenceNumber();
    }

    public String getSourceClassName() {
        if (gatheringCallerDetails) {
            return record.getSourceClassName();
        }
        else {
            return null;
        }
    }

    public String getSourceMethodName() {
        if (gatheringCallerDetails) {
            return record.getSourceMethodName();
        }
        else {
            return null;
        }
    }

    public String getLevelDescription() {
        return record.getLevel().getName();
    }

    public Level getJavaLevel() {
        return record.getLevel();
    }

    public String getFlavour() {
        return "Juli";
    }

    @Override public long getHubTime() {
        return record.getMillis();         
    }

}
