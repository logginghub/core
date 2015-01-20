package com.logginghub.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import com.logginghub.logging.log4j.Log4jDetailsSnapshot;


/**
 * LogEvent wrapper for the log4j log record object.
 * 
 * @author admin
 */
public class Log4jLogEvent extends BaseLogEvent {
    private LoggingEvent record;
    
    private static String lineSeparator = (String) System.getProperty("line.separator");
    private final Log4jDetailsSnapshot snapshot;

    public Log4jLogEvent(LoggingEvent record, String sourceApplication, InetAddress sourceHost, String threadName, Log4jDetailsSnapshot snapshot) {
        super();

        this.snapshot = snapshot;
        this.record = record;
        setSourceApplication(sourceApplication);
        setSourceHost(sourceHost.getHostName());
        setSourceAddress(sourceHost.getHostAddress());
        setThreadName(threadName);
    }

    public String getFormattedException() {
        String formatted;

        ThrowableInformation throwableInformation = record.getThrowableInformation();
        if (throwableInformation != null) {
            Throwable thrown = throwableInformation.getThrowable();
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
        String[] formatted = null;

        Hashtable<String, Object> mdc = snapshot.getMdc();
        String ndc = snapshot.getNdc();
        if (mdc != null || ndc != null) {

            int count = 0;
            if (mdc != null) {
                count += mdc.size();
            }

            if (ndc != null) {
                count++;
            }

            int index = 0;
            formatted = new String[count];

            if (ndc != null) {
                formatted[index] = ndc;
                index++;
            }

            if (mdc != null) {
                Set<String> keySet = mdc.keySet();
                for (String key : keySet) {
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
        return log4jToJuliLevel(record.getLevel()).intValue();
    }

    @Override public long getHubTime() {
        return snapshot.getTimestamp();         
    }
    
    public long getOriginTime() {
        return snapshot.getTimestamp();
    }

    public String getLoggerName() {
        return record.getLoggerName();
    }

    public String getMessage() {
        return record.getMessage().toString();
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
        return record.getLevel().toString();
    }

    public Level getJavaLevel() {
        Level juliLevel = log4jToJuliLevel(record.getLevel());
        return juliLevel;
    }

    private Level log4jToJuliLevel(org.apache.log4j.Level level) {
        // jshaw = the toInts are to avoid any weirdness like slf4j/commons
        // providing different instances of the log level type
        Level juliLevel;
        if (level.toInt() == org.apache.log4j.Level.DEBUG.toInt()) {
            juliLevel = Level.FINE;
        }
        else if (level.toInt() == org.apache.log4j.Level.ERROR.toInt()) {
            juliLevel = Level.SEVERE;
        }
        else if (level.toInt() == org.apache.log4j.Level.FATAL.toInt()) {
            juliLevel = Level.SEVERE;
        }
        else if (level.toInt() == org.apache.log4j.Level.INFO.toInt()) {
            juliLevel = Level.INFO;
        }
        else if (level.toInt() == org.apache.log4j.Level.OFF.toInt()) {
            juliLevel = Level.OFF;
        }
        else if (level.toInt() == org.apache.log4j.Level.TRACE.toInt()) {
            juliLevel = Level.FINEST;
        }
        else if (level.toInt() == org.apache.log4j.Level.WARN.toInt()) {
            juliLevel = Level.WARNING;
        }
        else {
            throw new RuntimeException(String.format("Dont know how to translate level [%s (%d)] into a juli level using it checks",
                                                     level.toString(),
                                                     level.toInt()));
        }
        return juliLevel;
    }

    public String getFlavour() {
        return "Log4j";
    }
    
}
