package com.logginghub.logging.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.logginghub.logging.BaseLogEvent;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * LogEvent wrapper for the logback log record object.
 * 
 * @author admin
 */
public class LogbackLogEvent extends BaseLogEvent {
    private ILoggingEvent record;

    private static String lineSeparator = (String) System.getProperty("line.separator");
    private final LogbackDetailsSnapshot snapshot;

    public LogbackLogEvent(ILoggingEvent record, String sourceApplication, InetAddress sourceHost, String threadName, LogbackDetailsSnapshot snapshot) {
        super();

        this.snapshot = snapshot;
        this.record = record;
        setSourceApplication(sourceApplication);
        setSourceHost(sourceHost.getHostName());
        setSourceAddress(sourceHost.getHostAddress());
        setThreadName(threadName);
    }

    public String getFormattedException() {
        String formatted = null;

        IThrowableProxy throwableProxy = record.getThrowableProxy();

        if (throwableProxy != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(throwableProxy.getMessage());
            builder.append(lineSeparator);

            String sep = "";
            StackTraceElementProxy[] stackTraceElementProxyArray = throwableProxy.getStackTraceElementProxyArray();
            for (StackTraceElementProxy stackTraceElementProxy : stackTraceElementProxyArray) {
                builder.append(sep);
                builder.append(stackTraceElementProxy.getSTEAsString());
                sep = lineSeparator;
            }

            formatted = builder.toString();
        }
        else {
            formatted = null;
        }

        return formatted;
    }

    public String[] getFormattedObject() {
        String[] formatted = null;

        Map mdc = snapshot.getMdc();
        if (mdc != null) {

            int count = 0;
            if (mdc != null) {
                count += mdc.size();
            }

            int index = 0;
            formatted = new String[count];

            if (mdc != null) {
                Set<Object> keySet = mdc.keySet();
                for (Object key : keySet) {
                    Object object = mdc.get(key);
                    String line = key + " : " + object;
                    formatted[index] = line;
                    index++;
                }
            }
        }

        return formatted;
    }

    public int getLevel() {
        return logbackToLoggingHubLevel(record.getLevel()).intValue();
    }

    public long getOriginTime() {
        return snapshot.getTimestamp();
    }

    public long getHubTime() {
        return snapshot.getTimestamp();
    }
    
    public String getLoggerName() {
        return record.getLoggerName();
    }

    public String getMessage() {
        return record.getFormattedMessage();
    }

    public long getSequenceNumber() {
        return 0;
    }

    public String getSourceClassName() {
        return snapshot.getClassName();
    }

    public String getSourceMethodName() {
        return snapshot.getMethodName();
    }

    public String getThreadName() {
        return snapshot.getThreadName();
    }

    public String getLevelDescription() {
        return logbackToLoggingHubLevel(record.getLevel()).toString();
    }

    public Level getJavaLevel() {
        Level juliLevel = logbackToLoggingHubLevel(record.getLevel());
        return juliLevel;
    }

    @Override
    public Map<String, String> getMetadata() {
        return null;
    }

    private Level logbackToLoggingHubLevel(ch.qos.logback.classic.Level level) {
        // jshaw = the toInts are to avoid any weirdness like slf4j/commons
        // providing different instances of the log level type
        Level juliLevel;
        if (level.toInt() == ch.qos.logback.classic.Level.DEBUG.toInt()) {
            juliLevel = Level.FINE;
        }
        else if (level.toInt() == ch.qos.logback.classic.Level.ERROR.toInt()) {
            juliLevel = Level.SEVERE;
        }
        else if (level.toInt() == ch.qos.logback.classic.Level.INFO.toInt()) {
            juliLevel = Level.INFO;
        }
        else if (level.toInt() == ch.qos.logback.classic.Level.OFF.toInt()) {
            juliLevel = Level.OFF;
        }
        else if (level.toInt() == ch.qos.logback.classic.Level.TRACE.toInt()) {
            juliLevel = Level.FINEST;
        }
        else if (level.toInt() == ch.qos.logback.classic.Level.WARN.toInt()) {
            juliLevel = Level.WARNING;
        }
        else {
            throw new RuntimeException(String.format("Dont know how to translate level [%s (%d)] into a loggingHub level",
                                                     level.toString(),
                                                     level.toInt()));
        }
        return juliLevel;
    }

    public String getFlavour() {
        return "logback";
    }

}
