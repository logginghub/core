package com.logginghub.logging.log4j;

import com.logginghub.logging.*;
import com.logginghub.logging.api.levelsetting.LevelSetting;
import com.logginghub.logging.api.levelsetting.LevelSettingsGroup;
import com.logginghub.logging.api.levelsetting.LevelSettingsRequest;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketConnection.SlowSendingPolicy;
import com.logginghub.utils.TimeProvider;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import java.net.InetSocketAddress;
import java.util.List;

public class SocketAppender extends AppenderSkeleton implements StandardAppenderFeatures {

    private AppenderHelper appenderHelper;
    private boolean captureLocationInformation;
    private int appendCounter;

    public SocketAppender() {
        this("VertexLabs-log4jSocketAppender");
    }

    public SocketAppender(String name) {
        appenderHelper = new AppenderHelper(name, new AppenderHelperCustomisationInterface() {
            public HeapLogger createHeapLogger() {
                final Logger logger = Logger.getLogger("heap-logger");
                return new HeapLogger() {
                    @Override protected void log(String format) {
                        logger.debug(format);
                    }
                };
            }

            public GCFileWatcher createGCWatcher() {
                final Logger logger = Logger.getLogger("gc-logger");
                return new GCFileWatcher() {
                    @Override protected void log(String gcLine) {
                        logger.debug(gcLine);
                    }
                };
            }

            public CpuLogger createCPULogger() {
                final Logger logger = Logger.getLogger("cpu-logger");
                return new CpuLogger() {
                    @Override protected void log(String message) {
                        logger.debug(message);
                    }
                };
            }
        });

        appenderHelper.setLevelSettingImplementation(new LevelSettingImplementation() {
            @Override public boolean process(LevelSettingsRequest request) {
                return setLogLevels(request);
            }
        });
    }

    /**
     * Testing method; sends an event directly, bypassing the queue
     *
     * @param event
     * @throws LoggingMessageSenderException
     */
    public void sendDirect(LogEvent event) throws LoggingMessageSenderException {
        appenderHelper.send(new LogEventMessage(event));
    }

    public long getFailureDelay() {
        return appenderHelper.getFailureDelay();
    }

    public void setFailureDelay(long failureDelay) {
        appenderHelper.setFailureDelay(failureDelay);
    }

    // //////////////////////////////////////////////////////////////////
    // Accessors
    // //////////////////////////////////////////////////////////////////

    public void setWriteQueueOverflowPolicy(String policy) {
        appenderHelper.setWriteQueueOverflowPolicy(SlowSendingPolicy.valueOf(policy));
    }

    public void addConnectionPoint(InetSocketAddress inetSocketAddress) {
        appenderHelper.addConnectionPoint(inetSocketAddress);
    }

    public void removeConnectionPoint(InetSocketAddress inetSocketAddress) {
        appenderHelper.removeConnectionPoint(inetSocketAddress);
    }

    public int getMaxDispatchQueueSize() {
        return appenderHelper.getMaxDispatchQueueSize();
    }

    public void setMaxDispatchQueueSize(int maxQueueSize) {
        appenderHelper.setMaxDispatchQueueSize(maxQueueSize);
    }

    public boolean isDontThrowExceptionsIfHubIsntUp() {
        return appenderHelper.isDontThrowExceptionsIfHubIsntUp();
    }

    public void setDontThrowExceptionsIfHubIsntUp(boolean dontThrowExceptionsIfHubIsntUp) {
        appenderHelper.setDontThrowExceptionsIfHubIsntUp(dontThrowExceptionsIfHubIsntUp);
    }

    public String getSourceApplication() {
        return appenderHelper.getSourceApplication();
    }

    public void setSourceApplication(String sourceApplication) {
        appenderHelper.setSourceApplication(sourceApplication);
    }

    public void setHost(String host) {
        appenderHelper.setHost(host);
    }

    public String getTelemetry() {
        return appenderHelper.getTelemetry();
    }

    public synchronized void setTelemetry(String connectionString) {
        appenderHelper.setTelemetry(connectionString);
    }

    public boolean isPublishMachineTelemetry() {
        return appenderHelper.isPublishMachineTelemetry();
    }

    public synchronized void setPublishMachineTelemetry(boolean publishMachineTelemetry) {
        appenderHelper.setPublishMachineTelemetry(publishMachineTelemetry);
    }

    public boolean isPublishProcessTelemetry() {
        return appenderHelper.isPublishProcessTelemetry();
    }

    public synchronized void setPublishProcessTelemetry(boolean publishProcessTelemetry) {
        appenderHelper.setPublishProcessTelemetry(publishProcessTelemetry);
    }

    public void setForceFlush(boolean forceFlush) {
        appenderHelper.setForceFlush(forceFlush);
    }

    public synchronized void setHeapLogging(boolean value) {
        appenderHelper.setHeapLogging(value);
    }

    public synchronized void setCpuLogging(boolean value) {
        appenderHelper.setCpuLogging(value);
    }

    public synchronized void setDetailedCpuLogging(boolean value) {
        appenderHelper.setDetailedCpuLogging(value);
    }

    public void setGCLogging(String path) {
        appenderHelper.setGCLogging(path);
    }

    public void setJava7GCLogging(boolean enabled) {
        appenderHelper.setJava7GCLogging(enabled);
    }

    public boolean isUseDispatchThread() {
        return appenderHelper.isUseDispatchThread();
    }

    public void setUseDispatchThread(boolean value) {
        appenderHelper.setUseDispatchThread(value);
        // useDispatchThread = value;
    }

    protected boolean setLogLevels(LevelSettingsRequest request) {

        LevelSettingsGroup levelSettingsGroup = request.getLevelSettings();
        List<LevelSetting> settings = levelSettingsGroup.getSettings();
        for (LevelSetting levelSetting : settings) {
            String levelText = levelSetting.getLevel();
            String loggerName = levelSetting.getLoggerName();

            org.apache.log4j.Level level = parseLevel(levelText);
            org.apache.log4j.Logger.getLogger(loggerName).setLevel(level);
        }

        return true;
    }

    private org.apache.log4j.Level parseLevel(String level) {

        org.apache.log4j.Level levelValue;

        String lowerCase = level.toLowerCase();
        char first = lowerCase.charAt(0);
        switch (first) {
            case 'a': {
                levelValue = org.apache.log4j.Level.ALL;
                break;
            }
            case 'd': {
                levelValue = org.apache.log4j.Level.DEBUG;
                break;
            }
            case 't': {
                levelValue = org.apache.log4j.Level.TRACE;
                break;
            }
            case 's': {
                levelValue = org.apache.log4j.Level.FATAL;
                break;
            }
            case 'f': {
                if (lowerCase.equals("fatal")) {
                    levelValue = org.apache.log4j.Level.FATAL;
                } else if (lowerCase.equals("finer")) {
                    levelValue = org.apache.log4j.Level.TRACE;
                } else if (lowerCase.equals("finest")) {
                    levelValue = org.apache.log4j.Level.TRACE;
                } else if (lowerCase.equals("fine")) {
                    levelValue = org.apache.log4j.Level.DEBUG;
                } else {
                    // TODO : how to indicate a problem!
                    levelValue = org.apache.log4j.Level.INFO;
                }
                break;
            }
            case 'w': {
                levelValue = org.apache.log4j.Level.WARN;
                break;
            }
            case 'i': {
                levelValue = org.apache.log4j.Level.INFO;
                break;
            }
            case 'c': {
                levelValue = org.apache.log4j.Level.DEBUG;
                break;
            }
            default: {
                levelValue = org.apache.log4j.Level.INFO;
            }
        }

        return levelValue;

    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // The log4j appender interface
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override public void activateOptions() {
        super.activateOptions();

        appenderHelper.start();
    }

    @Override public void append(final LoggingEvent record) {
        appendCounter++;
        final Log4jDetailsSnapshot details = Log4jDetailsSnapshot.fromLoggingEvent(record,
                                                                                   appenderHelper.getTimeProvider(),
                                                                                   captureLocationInformation);

        appenderHelper.append(new AppenderHelperEventConvertor() {
            public LogEvent createLogEvent() {
                Log4jLogEvent event = new Log4jLogEvent(record,
                                                        appenderHelper.getSourceApplication(),
                                                        appenderHelper.getHost(),
                                                        Thread.currentThread().getName(),
                                                        details);
                event.setPid(appenderHelper.getPid());
                event.setChannel(appenderHelper.getChannel());
                return event;
            }

            public EventSnapshot createSnapshot() {
                return new EventSnapshot() {
                    public LogEvent rebuildEvent() {
                        return createLogEvent();
                    }
                };
            }

        });
    }

    public int getAppendCounter() {
        return appendCounter;
    }

    @Override public void close() {
        appenderHelper.close();
    }

    @Override public boolean requiresLayout() {
        return false;
    }

    public void setPublishingListener(PublishingListener publishingListener) {
        appenderHelper.setPublishingListener(publishingListener);
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        appenderHelper.setTimeProvider(timeProvider);
    }

    public void setSocketClient(SocketClient socketClient) {
        appenderHelper.setSocketClient(socketClient);
    }

    public void setChannel(String string) {
        appenderHelper.setChannel(string);
    }

    public void setFailureDelayMaximum(long failureDelayMaximum) {
        appenderHelper.setFailureDelayMaximum(failureDelayMaximum);
    }

    public void setFailureDelayMultiplier(double failureDelayMultiplier) {
        appenderHelper.setFailureDelayMultiplier(failureDelayMultiplier);
    }

    public void setMaximumQueuedMessages(int maximumQueuedMessages) {
        appenderHelper.setMaxDispatchQueueSize(maximumQueuedMessages);
    }

    public boolean isStackTraceModuleEnabled() {
        return appenderHelper.isStackTraceModuleEnabled();
    }

    public void setStackTraceModuleEnabled(boolean value) {
        appenderHelper.setStackTraceModuleEnabled(value);
    }

    public String getStackTraceModuleBroadcastInterval() {
        return appenderHelper.getStackTraceModuleBroadcastInterval();
    }

    public void setStackTraceModuleBroadcastInterval(String string) {
        appenderHelper.setStackTraceModuleBroadcastInterval(string);
    }

    public boolean isCaptureLocationInformation() {
        return captureLocationInformation;
    }

    public void setCaptureLocationInformation(boolean captureLocationInformation) {
        this.captureLocationInformation = captureLocationInformation;
    }

    public AppenderHelper getAppenderHelper() {
        return appenderHelper;
    }

    public void setMaxWriteQueueSize(int i) {
        appenderHelper.setMaxWriteQueueSize(i);
    }
}
