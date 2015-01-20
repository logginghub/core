package com.logginghub.logging;

import com.logginghub.utils.Dictionary;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

/**
 * The main model that represents a single item of logging.
 * 
 * @author admin
 */
public class DictionaryLogEvent implements SerialisableObject {

    private int level;
    private long sequenceNumber;
    private int sourceClassName;
    private int sourceMethodName;
    private String message;
    private int threadName;
    private long localCreationTimeMillis;
    private int loggerName;
    private int sourceHost;
    private int sourceAddress;
    private int sourceApplication;
    private int channel;

    private int pid;

    private String formattedException;
    private String[] formattedObject;

    private Metadata metadata;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public int getSourceClassName() {
        return sourceClassName;
    }

    public void setSourceClassName(int sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    public int getSourceMethodName() {
        return sourceMethodName;
    }

    public void setSourceMethodName(int sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getThreadName() {
        return threadName;
    }

    public void setThreadName(int threadName) {
        this.threadName = threadName;
    }

    public long getOriginTime() {
        return localCreationTimeMillis;
    }

    public void setLocalCreationTimeMillis(long localCreationTimeMillis) {
        this.localCreationTimeMillis = localCreationTimeMillis;
    }

    public int getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(int loggerName) {
        this.loggerName = loggerName;
    }

    public int getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(int sourceHost) {
        this.sourceHost = sourceHost;
    }

    public int getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(int sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public int getSourceApplication() {
        return sourceApplication;
    }

    public void setSourceApplication(int sourceApplication) {
        this.sourceApplication = sourceApplication;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
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

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public static DictionaryLogEvent fromLogEvent(LogEvent logEvent, Dictionary dictionary) {

        DictionaryLogEvent dictionaryEvent = new DictionaryLogEvent();
        dictionaryEvent.setChannel(dictionary.getID(logEvent.getChannel()));
        dictionaryEvent.setFormattedException(logEvent.getFormattedException());
        dictionaryEvent.setFormattedObject(logEvent.getFormattedObject());
        dictionaryEvent.setLevel(logEvent.getLevel());
        dictionaryEvent.setLocalCreationTimeMillis(logEvent.getOriginTime());
        dictionaryEvent.setLoggerName(dictionary.getID(logEvent.getLoggerName()));
        dictionaryEvent.setMessage(logEvent.getMessage());
        // TODO: metadata isn't on the logevent interface?
        // dictionaryEvent.setMetadata(logEvent.getMetadata());
        dictionaryEvent.setPid(logEvent.getPid());
        dictionaryEvent.setSequenceNumber(logEvent.getSequenceNumber());
        dictionaryEvent.setSourceAddress(dictionary.getID(logEvent.getSourceAddress()));
        dictionaryEvent.setSourceApplication(dictionary.getID(logEvent.getSourceApplication()));
        dictionaryEvent.setSourceClassName(dictionary.getID(logEvent.getSourceClassName()));
        dictionaryEvent.setSourceHost(dictionary.getID(logEvent.getSourceHost()));
        dictionaryEvent.setSourceMethodName(dictionary.getID(logEvent.getSourceMethodName()));
        dictionaryEvent.setThreadName(dictionary.getID(logEvent.getThreadName()));

        return dictionaryEvent;
    }

    public DefaultLogEvent toLogEvent(Dictionary dictionary) {
        DefaultLogEvent event = new DefaultLogEvent();
        event.setChannel(dictionary.getString(this.getChannel()));
        event.setFormattedException(this.getFormattedException());
        event.setFormattedObject(this.getFormattedObject());
        event.setLevel(this.getLevel());
        event.setLocalCreationTimeMillis(this.getOriginTime());
        event.setLoggerName(dictionary.getString(this.getLoggerName()));
        event.setMessage(this.getMessage());
        event.setMetadata(this.getMetadata());
        event.setPid(this.getPid());
        event.setSequenceNumber(this.getSequenceNumber());
        event.setSourceAddress(dictionary.getString(this.getSourceAddress()));
        event.setSourceApplication(dictionary.getString(this.getSourceApplication()));
        event.setSourceClassName(dictionary.getString(this.getSourceClassName()));
        event.setSourceHost(dictionary.getString(this.getSourceHost()));
        event.setSourceMethodName(dictionary.getString(this.getSourceMethodName()));
        event.setThreadName(dictionary.getString(this.getThreadName()));

        return event;
    }

    public void read(SofReader reader) throws SofException {
        localCreationTimeMillis = reader.readLong(1);
        level = reader.readInt(2);
        message = reader.readString(3);
        sourceHost = reader.readInt(4);
        sourceApplication = reader.readInt(5);
        pid = reader.readInt(6);
        threadName = reader.readInt(7);
        loggerName = reader.readInt(8);
        sourceAddress = reader.readInt(9);
        channel = reader.readInt(10);
        sourceClassName = reader.readInt(11);
        sourceMethodName = reader.readInt(12);
        formattedException = reader.readString(13);
        formattedObject = reader.readStringArray(14);
        sequenceNumber = reader.readLong(15);
        // FIXME : encode metadata
        // metadata = reader.readObject(16);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, localCreationTimeMillis);
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
        // FIXME : encode metadata
        // writer.write(15, metadata);
    }

    public int estimateSizeOf() {

        int sizeof = 0;

        sizeof += 8; /* time */
        sizeof += 8; /* sequence */
        sizeof += 4; /* level */
        sizeof += 4; /* pid */

        sizeof += DefaultLogEvent.estimateSizeOf(message);
        sizeof += 4; /* sourceHost */
        sizeof += 4; /* sourceApplication */
        sizeof += 4; /* threadName */
        sizeof += 4; /* loggerName */
        sizeof += 4; /* sourceAddress */
        sizeof += 4; /* channel */
        sizeof += 4; /* sourceClassName */
        sizeof += 4; /* sourceMethodName */
        sizeof += DefaultLogEvent.estimateSizeOf(formattedException);

        // TODO: these are included
        // sizeof += estimateSizeOf(formattedObject);
        // sizeof += estimateSizeOf(metadata);

        return sizeof;

    }

}
