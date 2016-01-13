package com.logginghub.logging;

import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * The main model that represents a single item of logging.
 *
 * @author admin
 */
public class DefaultLogEvent implements LogEvent, Serializable, SerialisableObject, TimeProvider {
    private static final long serialVersionUID = 1L;

    private static String lineSeparator = (String) System.getProperty("line.separator");

    private int level;
    private long sequenceNumber;
    private String sourceClassName;
    private String sourceMethodName;
    private String message;
    private String threadName;
    private long originTime;
    private String loggerName;
    private String sourceHost;
    private String sourceAddress;
    private String sourceApplication;
    private String channel;
    private long hubTime;

    private int pid;

    private String formattedException;
    private String[] formattedObject;

    private Map<String, String> metadata = null;

    // jshaw - this is cached so we don't have to constantly look it up whilst filtering
    private String levelDescription;

    public static int estimateSizeOf(String string) {

        int sizeof = 0;

        if (string == null) {
            sizeof += 8;
        } else {
            sizeof += 8; /* object reference */
            sizeof += 12; /* string int fields */
            sizeof += 4; /* array object reference */
            sizeof += 14 + string.length() * 2; /* actual bytes */
        }

        return sizeof;

    }

    public int estimateSizeOf() {

        // time + sequence + level + pid + (12 * reference)
        int sizeof = 62;

        int nonNullStrings = 0;
        int lengths = 0;

        if (message != null) {
            nonNullStrings++;
            lengths += message.length();
        }

        if (sourceHost != null) {
            nonNullStrings++;
            lengths += sourceHost.length();
        }

        if (sourceApplication != null) {
            nonNullStrings++;
            lengths += sourceApplication.length();
        }

        if (threadName != null) {
            nonNullStrings++;
            lengths += threadName.length();
        }

        if (loggerName != null) {
            nonNullStrings++;
            lengths += loggerName.length();
        }

        if (sourceAddress != null) {
            nonNullStrings++;
            lengths += sourceAddress.length();
        }

        if (channel != null) {
            nonNullStrings++;
            lengths += channel.length();
        }

        if (sourceClassName != null) {
            nonNullStrings++;
            lengths += sourceClassName.length();
        }

        if (sourceMethodName != null) {
            nonNullStrings++;
            lengths += sourceMethodName.length();
        }

        if (formattedException != null) {
            nonNullStrings++;
            lengths += formattedException.length();
        }

        sizeof += (nonNullStrings * 36) + (lengths * 2);

        // TODO: these are included
        // sizeof += estimateSizeOf(formattedObject);
        // sizeof += estimateSizeOf(metadata);


        return sizeof;

    }

    /**
     * Return the java.util.Logging style level description
     *
     * @return
     * @deprecated This is really slow, so we've cached it - use getLevelDescription instead
     */
    public String getJULILevelDescription() {
        return getLevelDescriptionInternal();
    }

    public synchronized Map<String, String> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<String, String>();
        }
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public long getTime() {
        return getOriginTime();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((formattedException == null) ? 0 : formattedException.hashCode());
        result = prime * result + Arrays.hashCode(formattedObject);
        result = prime * result + level;
        result = prime * result + (int) (originTime ^ (originTime >>> 32));
        result = prime * result + ((loggerName == null) ? 0 : loggerName.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
        result = prime * result + pid;
        result = prime * result + (int) (sequenceNumber ^ (sequenceNumber >>> 32));
        result = prime * result + ((sourceAddress == null) ? 0 : sourceAddress.hashCode());
        result = prime * result + ((sourceApplication == null) ? 0 : sourceApplication.hashCode());
        result = prime * result + ((sourceClassName == null) ? 0 : sourceClassName.hashCode());
        result = prime * result + ((sourceHost == null) ? 0 : sourceHost.hashCode());
        result = prime * result + ((sourceMethodName == null) ? 0 : sourceMethodName.hashCode());
        result = prime * result + ((threadName == null) ? 0 : threadName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultLogEvent other = (DefaultLogEvent) obj;
        if (formattedException == null) {
            if (other.formattedException != null)
                return false;
        } else if (!formattedException.equals(other.formattedException))
            return false;
        if (!Arrays.equals(formattedObject, other.formattedObject))
            return false;
        if (level != other.level)
            return false;
        if (originTime != other.originTime)
            return false;
        if (loggerName == null) {
            if (other.loggerName != null)
                return false;
        } else if (!loggerName.equals(other.loggerName))
            return false;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (metadata == null) {
            if (other.metadata != null)
                return false;
        } else if (!metadata.equals(other.metadata))
            return false;
        if (pid != other.pid)
            return false;
        if (sequenceNumber != other.sequenceNumber)
            return false;
        if (sourceAddress == null) {
            if (other.sourceAddress != null)
                return false;
        } else if (!sourceAddress.equals(other.sourceAddress))
            return false;
        if (sourceApplication == null) {
            if (other.sourceApplication != null)
                return false;
        } else if (!sourceApplication.equals(other.sourceApplication))
            return false;
        if (sourceClassName == null) {
            if (other.sourceClassName != null)
                return false;
        } else if (!sourceClassName.equals(other.sourceClassName))
            return false;
        if (sourceHost == null) {
            if (other.sourceHost != null)
                return false;
        } else if (!sourceHost.equals(other.sourceHost))
            return false;
        if (sourceMethodName == null) {
            if (other.sourceMethodName != null)
                return false;
        } else if (!sourceMethodName.equals(other.sourceMethodName))
            return false;
        if (threadName == null) {
            if (other.threadName != null)
                return false;
        } else if (!threadName.equals(other.threadName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        int maxMessageDump = 400;
        if (message.length() > maxMessageDump) {
            return "[LogEvent message [" + Logger.toDateString(originTime) + "] ='" + message.substring(0, maxMessageDump) + "...']";
        } else {
            return "[LogEvent message [" + Logger.toDateString(originTime) + "] ='" + getMessage() + "']";
        }
    }

    /**
     * A version of populate that doesn't force you to specific a source app. You just need to hope someone further down the chain will set it for
     * you...
     *
     * @param lr
     */
    public void populateFromLogRecord(LogRecord lr) {
        populateFromLogRecord(lr, null, null);
    }

    public void populateFromLogRecord(LogRecord record, String sourceApplication) {
        populateFromLogRecord(record, sourceApplication, null);
    }

    public void populateFromLogRecord(LogRecord record, String sourceApplication, InetAddress sourceAddress) {
        level = record.getLevel().intValue();
        levelDescription = getLevelDescriptionInternal();

        sequenceNumber = record.getSequenceNumber();
        sourceClassName = record.getSourceClassName();
        sourceMethodName = record.getSourceMethodName();
        message = record.getMessage();

        // jshaw - todo - see if there is a better way to get the thread name
        // from the record thread ID
        threadName = Thread.currentThread().getName();

        originTime = record.getMillis();
        loggerName = record.getLoggerName();

        if (sourceAddress == null) {
            try {
                sourceAddress = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                throw new RuntimeException("Failed to resolve local host address", e);
            }
        }

        this.sourceAddress = sourceAddress.getHostAddress();
        this.sourceHost = sourceAddress.getHostName();

        this.sourceApplication = sourceApplication;
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            formattedException = formatException(thrown);
        } else {
            formattedException = null;
        }

        Object[] parameters = record.getParameters();
        if (parameters != null) {
            formattedObject = formatObjects(parameters);
        }
    }

    private String getLevelDescriptionInternal() {
        Level javaLevel = getJavaLevel();
        if (javaLevel != null) {
            return javaLevel.toString();
        } else {
            return String.format("Unknown level, int value [%d]", getLevel());
        }
    }

    public static String formatException(Throwable thrown) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        sw.append(thrown.getMessage());
        sw.append(lineSeparator);
        thrown.printStackTrace(pw);
        return sw.toString();
    }

    private String[] formatObjects(Object[] parameters) {
        String[] formattedObjects = new String[parameters.length];

        int i = 0;
        for (Object object : parameters) {
            formattedObjects[i++] = object.toString();
        }

        return formattedObjects;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        this.levelDescription = getLevelDescriptionInternal();
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getSourceClassName() {
        return sourceClassName;
    }

    public void setSourceClassName(String sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    public String getSourceMethodName() {
        return sourceMethodName;
    }

    public void setSourceMethodName(String sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public long getOriginTime() {
        return originTime;
    }

    public void setOriginTime(long originTime) {
        this.originTime = originTime;
    }

    @Override
    public long getHubTime() {
        return hubTime;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public String getSourceApplication() {
        return sourceApplication;
    }

    public void setSourceApplication(String sourceApplication) {
        this.sourceApplication = sourceApplication;
    }

    public String getFormattedException() {
        return formattedException;
    }

    public void setFormattedException(String formattedException) {
        this.formattedException = formattedException;
    }

    public String[] getFormattedObject() {
        return formattedObject;
    }

    public void setFormattedObject(String[] formattedObject) {
        this.formattedObject = formattedObject;
    }

    public String getFlavour() {
        return "DefaultImpl";
    }

    public String getLevelDescription() {
        return levelDescription;
    }

    public java.util.logging.Level getJavaLevel() {
        Level level = null;

        int levelValue = getLevel();

        if (levelValue == Level.INFO.intValue()) {
            level = Level.INFO;
        } else if (levelValue == Level.WARNING.intValue()) {
            level = Level.WARNING;
        } else if (levelValue == Level.SEVERE.intValue()) {
            level = Level.SEVERE;
        } else if (levelValue == Level.SEVERE.intValue()) {
            level = Level.SEVERE;
        } else if (levelValue == Level.CONFIG.intValue()) {
            level = Level.CONFIG;
        } else if (levelValue == Level.FINE.intValue()) {
            level = Level.FINE;
        } else if (levelValue == Level.FINER.intValue()) {
            level = Level.FINER;
        } else if (levelValue == Level.FINEST.intValue()) {
            level = Level.FINEST;
        } else if (levelValue == Level.OFF.intValue()) {
            level = Level.OFF;
        } else {
            level = null;
            // throw new
            // RuntimeException(String.format("Dont know what Juli level to use for int value [%d]",
            // levelValue));
        }

        return level;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getChannel() {
        return channel;
    }

//    @Override
//    public String getMetadata(String key) {
//        String value;
//        if (metadata != null) {
//            value = metadata.getString(key);
//        } else {
//            value = null;
//        }
//        return value;
//    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setSourceAddress(String decodeString) {
        this.sourceAddress = decodeString;
    }

    public void read(SofReader reader) throws SofException {
        originTime = reader.readLong(1);
        level = reader.readInt(2);
        levelDescription = getLevelDescriptionInternal();
        message = reader.readString(3);
        sourceHost = reader.readString(4);
        sourceApplication = reader.readString(5);
        pid = reader.readInt(6);
        threadName = reader.readString(7);
        loggerName = reader.readString(8);
        sourceAddress = reader.readString(9);
        channel = reader.readString(10);
        sourceClassName = reader.readString(11);
        sourceMethodName = reader.readString(12);
        formattedException = reader.readString(13);
        formattedObject = reader.readStringArray(14);
        sequenceNumber = reader.readLong(15);
        hubTime = reader.readLong(16);

        int index = 17;
        int metadataCount = reader.readInt(index++);
        if(metadataCount > 0) {
            metadata = new HashMap<String, String>();
            for (int i = 0; i < metadataCount; i++) {
                String key = reader.readString(index++);
                String value = reader.readString(index++);
                metadata.put(key, value);
            }
        }

    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, originTime);
        writer.write(2, level);
        writer.write(3, message);
        writer.write(4, sourceHost);
        writer.write(5, sourceApplication);
        writer.write(6, pid);
        writer.write(7, threadName);
        writer.write(8, loggerName);
        writer.write(9, sourceAddress);
        writer.write(10, channel);
        writer.write(11, sourceClassName);
        writer.write(12, sourceMethodName);
        writer.write(13, formattedException);
        writer.write(14, formattedObject);
        writer.write(15, sequenceNumber);
        writer.write(16, hubTime);

        int index = 17;
        if(metadata != null && !metadata.isEmpty()) {
            writer.write(index++, metadata.size());

            for (Entry<String, String> entry : metadata.entrySet()) {
                writer.write(index++, entry.getKey());
                writer.write(index++, entry.getValue());
            }

        }else{
            writer.write(index++, 0);
        }

    }

    /**
     * @param localCreationTimeMillis
     * @deprecated Use setOriginTime instead - it sets the same field. This accessor is here for backwards compatibility and will be removed.
     */
    @Deprecated
    public void setLocalCreationTimeMillis(long localCreationTimeMillis) {
        this.originTime = localCreationTimeMillis;
    }

}
