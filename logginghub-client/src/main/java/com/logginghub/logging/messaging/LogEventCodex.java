package com.logginghub.logging.messaging;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messages.PartialMessageException;
import com.logginghub.utils.ExpandingByteBuffer;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.logging.Logger;

public class LogEventCodex extends AbstractCodex {

    private static final Logger logger = Logger.getLoggerFor(LogEventCodex.class);

    private final static byte versionOne = 1;
    private final static int lengthPlaceHolder = 0;

    /**
     * If we get asked to decode anything bigger than this then assume something
     * has gone wrong...
     */
    private static final int stringSizeCutoff = Integer.getInteger("logEventCodex.stringSizeCutoff", 10 * 1024 * 1024);

    public static LogEvent decode(ByteBuffer buffer) throws PartialMessageException {
        logger.finer("Attempting to decode log event from buffer '{}'", buffer);

        DefaultLogEvent event = null;

        buffer.mark();

        try {
            byte b = buffer.get();
            int version = b;

            if (version == versionOne) {
                int length = buffer.getInt();

                int payloadStart = buffer.position();

                logger.finer("Length decoded '{}', buffer state is '{}'", length, buffer);

                BufferDebugger bd = new BufferDebugger(buffer);

                if (length == 0) {
                    throw new RuntimeException(String.format("Invalid event size '%d'", length));
                }

                if (buffer.remaining() >= length) {
                    event = new DefaultLogEvent();

                    int decodeProgress =0;
                    try {
                        event.setLevel(buffer.getShort());
                        decodeProgress = 1;
                        event.setMessage(decodeString(buffer));                        
                        decodeProgress = 2;
                        event.setLocalCreationTimeMillis(buffer.getLong());
                        decodeProgress = 3;
                        event.setSourceApplication(decodeString(buffer));
                        decodeProgress = 4;
                        event.setFormattedException(decodeString(buffer));
                        decodeProgress = 5;
                        event.setFormattedObject(decodeStringArray(buffer));
                        decodeProgress = 6;
                        event.setLoggerName(decodeString(buffer));
                        decodeProgress = 7;
                        event.setSequenceNumber(buffer.getLong());
                        decodeProgress = 8;
                        event.setSourceClassName(decodeString(buffer));
                        decodeProgress = 9;
                        event.setSourceHost(decodeString(buffer));
                        decodeProgress = 10;
                        event.setSourceAddress(decodeString(buffer));
                        decodeProgress = 11;
                        event.setSourceMethodName(decodeString(buffer));
                        decodeProgress = 12;
                        event.setThreadName(decodeString(buffer));
                       
                        // Read optional fields - pid
                        int progress = buffer.position() - payloadStart;
                        if (progress < length) {
                            event.setPid(buffer.getInt());
                            decodeProgress = 14;
                        }
                        
                        // Read optional fields - channel
                        progress = buffer.position() - payloadStart;
                        if (progress < length) {
                            event.setChannel(decodeString(buffer));
                            decodeProgress = 15;
                        }
                        
                        decodeProgress = 16;
                        
                        // Skip anything we dont support at the end of the
                        // message
                        int toSkip = length - (buffer.position() - payloadStart);
                        buffer.position(buffer.position() + toSkip);
                        decodeProgress = 17;
                    } catch (RuntimeException re) {
                        throw new FormattedRuntimeException(re, "Decoding failed at position {} (decode progress {}) : {}", buffer.position(), decodeProgress, re.getMessage());
                    }

                } else {
                    buffer.reset();
                    throw new PartialMessageException();
                }
            } else {
                throw new RuntimeException("Unknown encoding version number " + version);
            }
        } catch (BufferUnderflowException bufferUnderflowException) {
            // This is ok, it just means the entire event isn't in the buffer
            // yet.
            buffer.reset();
            throw new PartialMessageException();
        }

        logger.finer("Log event '{}' decoded, buffer state is now '{}'", event, buffer);

        return event;
    }

    public static void encodeInternal_version1(ExpandingByteBuffer buffer, LogEvent event) {
        buffer.put(versionOne);

        int lengthPosition = buffer.position();
        buffer.putInt(lengthPlaceHolder);

        int contentPosition = buffer.position();

        buffer.putShort((short) event.getLevel());
        encodeString(buffer, event.getMessage());
        buffer.putLong(event.getOriginTime());
        encodeString(buffer, event.getSourceApplication());
        encodeString(buffer, event.getFormattedException());
        encodeStringArray(buffer, event.getFormattedObject());
        encodeString(buffer, event.getLoggerName());
        buffer.putLong(event.getSequenceNumber());
        encodeString(buffer, event.getSourceClassName());
        encodeString(buffer, event.getSourceHost());
        encodeString(buffer, event.getSourceAddress());
        encodeString(buffer, event.getSourceMethodName());
        encodeString(buffer, event.getThreadName());

        int endPosition = buffer.position();
        int length = endPosition - contentPosition;

        buffer.position(lengthPosition);
        buffer.putInt(length);
        buffer.position(endPosition);
    }

    public static void encodeInternal_version1_with_pid(ExpandingByteBuffer buffer, LogEvent event) {
        buffer.put(versionOne);

        int lengthPosition = buffer.position();
        buffer.putInt(lengthPlaceHolder);

        int contentPosition = buffer.position();

        buffer.putShort((short) event.getLevel());
        encodeString(buffer, event.getMessage());
        buffer.putLong(event.getOriginTime());
        encodeString(buffer, event.getSourceApplication());
        encodeString(buffer, event.getFormattedException());
        encodeStringArray(buffer, event.getFormattedObject());
        encodeString(buffer, event.getLoggerName());
        buffer.putLong(event.getSequenceNumber());
        encodeString(buffer, event.getSourceClassName());
        encodeString(buffer, event.getSourceHost());
        encodeString(buffer, event.getSourceAddress());
        encodeString(buffer, event.getSourceMethodName());
        encodeString(buffer, event.getThreadName());
        buffer.putInt(event.getPid());

        int endPosition = buffer.position();
        int length = endPosition - contentPosition;

        buffer.position(lengthPosition);
        buffer.putInt(length);
        buffer.position(endPosition);
    }
    
    public static void encodeInternal_version1_with_channel_and_pid(ExpandingByteBuffer buffer, LogEvent event) {
        buffer.put(versionOne);

        int lengthPosition = buffer.position();
        buffer.putInt(lengthPlaceHolder);

        int contentPosition = buffer.position();

        buffer.putShort((short) event.getLevel());        
        encodeString(buffer, event.getMessage());
        buffer.putLong(event.getOriginTime());
        encodeString(buffer, event.getSourceApplication());
        encodeString(buffer, event.getFormattedException());
        encodeStringArray(buffer, event.getFormattedObject());
        encodeString(buffer, event.getLoggerName());
        buffer.putLong(event.getSequenceNumber());
        encodeString(buffer, event.getSourceClassName());
        encodeString(buffer, event.getSourceHost());
        encodeString(buffer, event.getSourceAddress());
        encodeString(buffer, event.getSourceMethodName());
        encodeString(buffer, event.getThreadName());
        buffer.putInt(event.getPid());
        encodeString(buffer, event.getChannel());

        int endPosition = buffer.position();
        int length = endPosition - contentPosition;

        buffer.position(lengthPosition);
        buffer.putInt(length);
        buffer.position(endPosition);
    }


    public static void encode(ExpandingByteBuffer buffer, LogEvent event) {
        logger.finer("Attempting to encode log event '{}' into buffer '{}'", event, buffer);

        boolean done = false;

        while (!done) {
            try {
                encodeInternal_version1_with_channel_and_pid(buffer, event);
                done = true;
            } catch (BufferOverflowException blow) {
                buffer.doubleSize();
                buffer.clear();
                done = false;
            }
        }
    }
}
