package com.logginghub.utils.logging;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by james on 07/10/15.
 */
public class LoggerPerformanceInterface {
    private final Logger logger;

    private ThreadLocal<EventContext> threadLocalEventContexts = new ThreadLocal<EventContext>() {
        @Override
        protected EventContext initialValue() {
            return new EventContext();
        }
    };

    public LoggerPerformanceInterface(Logger logger) {
        this.logger = logger;
    }


    public EventContext log() {
        return threadLocalEventContexts.get();
    }

    public final class EventContext {

        private int level = Logger.info;
        private String sourceClassName;
        private String sourceMethodName;
        private ByteBuffer buffer = ByteBuffer.allocateDirect(100 * 1024);
        private int patternId;
        private long time;

        public void commit() {

            buffer.flip();

            List<LoggerStream> streams = logger.getStreams();

            if (streams == null) {
                streams = logger.root().getStreams();
            }

            for (LoggerStream loggerStream : streams) {
                loggerStream.onNewLogEvent(this);
            }

            buffer.clear();
        }

        public EventContext time(long time) {
            this.time = time;
            return this;
        }

        public ByteBuffer getBuffer() {
            return buffer;
        }

        public long getTime() {
            return time;
        }

        public EventContext level(int level) {
            this.level = level;
            return this;
        }

        public int getPatternId() {
            return patternId;
        }

        public String getSourceClassName() {
            return sourceClassName;
        }

        public String getSourceMethodName() {
            return sourceMethodName;
        }

        public EventContext pattern(int patternId) {
            this.patternId = patternId;
            return this;
        }

        public void setBuffer(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public EventContext sourceClass(String sourceClass) {
            this.sourceClassName = sourceClass;
            return this;
        }

        public EventContext sourceMethod(String sourceMethod) {
            this.sourceMethodName = sourceMethod;
            return this;
        }

        public int getLevel() {
            return level;
        }

        public EventContext writeBoolean(boolean value) {
            buffer.put((byte) (value ? 0 : 1));
            return this;
        }

        public EventContext writeChar(char value) {
            buffer.put((byte) value);
            return this;
        }

        public EventContext writeFloat(float value) {
            buffer.putFloat(value);
            return this;
        }

        public EventContext writeInt(int value) {
            buffer.putInt(value);
            return this;
        }

        public EventContext writeLong(long value) {
            buffer.putLong(value);
            return this;
        }

        public EventContext writeString(String value) {
            byte[] bytes = value.getBytes(Charset.forName("UTF-8"));
            writeBytes(bytes);
            return this;
        }

        public EventContext writeBytes(byte[] value) {
            buffer.putInt(value.length);
            buffer.put(value);
            return this;
        }


        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("EventContext{");
            sb.append("level=").append(Logger.getLevelName(level, false));
            sb.append(", sourceClassName='").append(sourceClassName).append('\'');
            sb.append(", sourceMethodName='").append(sourceMethodName).append('\'');
            sb.append(", patternId=").append(patternId);
            sb.append(", buffer=").append(buffer);
            sb.append('}');
            return sb.toString();
        }
    }
}
