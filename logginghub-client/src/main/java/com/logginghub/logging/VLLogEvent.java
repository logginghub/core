package com.logginghub.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.logging.Level;

import com.logginghub.utils.StringUtils;

/**
 * LogEvent wrapper for the com.logginghub.util.logging log record object.
 * 
 * @author admin
 */
public class VLLogEvent extends BaseLogEvent {

    private com.logginghub.utils.logging.LogEvent event;
    private static String lineSeparator = StringUtils.newline;

    public VLLogEvent(com.logginghub.utils.logging.LogEvent event, int pid, String sourceApplication, String sourceAddress, String sourceHost) {
        this.event = event;
        setSourceApplication(sourceApplication);
        setSourceAddress(sourceAddress);
        setSourceHost(sourceHost);
        setPid(pid);
    }

    public int getLevel() {
        return event.getLevel();
    }

    public long getSequenceNumber() {
        return event.getSequenceNumber();
    }

    public String getSourceClassName() {
        return event.getSourceClassName();
    }

    public String getSourceMethodName() {
        return event.getSourceMethodName();
    }

    public String getMessage() {
        return event.getMessage();
    }

    public String getThreadName() {
        return event.getThreadName();
    }

    public long getOriginTime() {
        return event.getOriginTime();
    }

    public String getLoggerName() {
        return "?";
    }

    public String getFormattedException() {
        String formatted;

        Throwable thrown = event.getThrowable();
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
        return null;
    }

    public String getFlavour() {
        return "vllogging";

    }

    public String getLevelDescription() {
        return Level.parse("" + event.getLevel()).getName();
    }

    public Level getJavaLevel() {
        return Level.parse("" + event.getLevel());
    }

    @Override public long getHubTime() {
        return event.getOriginTime();
    }

}
