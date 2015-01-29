package com.logginghub.logging.logback;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.BlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import com.logginghub.logging.AppenderHelper;
import com.logginghub.logging.AppenderHelperCustomisationInterface;
import com.logginghub.logging.AppenderHelperEventConvertor;
import com.logginghub.logging.CpuLogger;
import com.logginghub.logging.EventSnapshot;
import com.logginghub.logging.GCFileWatcher;
import com.logginghub.logging.HeapLogger;
import com.logginghub.logging.LevelSettingImplementation;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.StandardAppenderFeatures;
import com.logginghub.logging.api.levelsetting.LevelSetting;
import com.logginghub.logging.api.levelsetting.LevelSettingsGroup;
import com.logginghub.logging.api.levelsetting.LevelSettingsRequest;
import com.logginghub.logging.log4j.PublishingListener;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketConnection.SlowSendingPolicy;
import com.logginghub.utils.TimeProvider;

public class SocketAppender extends AppenderBase<ILoggingEvent> implements StandardAppenderFeatures {

    private AppenderHelper appenderHelper;

    public SocketAppender() {
        this("VertexLabs-logbackSocketAppender");
    }
    
    public SocketAppender(String name) {
        appenderHelper = new AppenderHelper("VertexLabs-logbackSocketAppender", new AppenderHelperCustomisationInterface() {
            public HeapLogger createHeapLogger() {
                final Logger logger = LoggerFactory.getLogger("heap-logger");
                return new HeapLogger() {
                    @Override protected void log(String format) {
                        logger.debug(format);
                    }
                };
            }

            public GCFileWatcher createGCWatcher() {
                final Logger logger = LoggerFactory.getLogger("gc-logger");
                return new GCFileWatcher() {
                    @Override protected void log(String gcLine) {
                        logger.debug(gcLine);
                    }
                };
            }

            public CpuLogger createCPULogger() {
                final Logger logger = LoggerFactory.getLogger("cpu-logger");
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

    public void setFailureDelay(long failureDelay) {
        appenderHelper.setFailureDelay(failureDelay);
    }

    public long getFailureDelay() {
        return appenderHelper.getFailureDelay();
    }
    
    protected boolean setLogLevels(LevelSettingsRequest request) {

        LevelSettingsGroup levelSettingsGroup = request.getLevelSettings();
        List<LevelSetting> settings = levelSettingsGroup.getSettings();
        for (LevelSetting levelSetting : settings) {
            String levelText = levelSetting.getLevel();
            String loggerName = levelSetting.getLoggerName();

            ch.qos.logback.classic.Level level = parseLevel(levelText);
            ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(loggerName);
            logger.setLevel(level);
        }

        return true;
    }
    
    private ch.qos.logback.classic.Level parseLevel(String level) {

        ch.qos.logback.classic.Level levelValue;

        String lowerCase = level.toLowerCase();
        char first = lowerCase.charAt(0);
        switch (first) {
            case 'a': {
                levelValue = ch.qos.logback.classic.Level.ALL;
                break;
            }
            case 'd': {
                levelValue = ch.qos.logback.classic.Level.DEBUG;
                break;
            }
            case 't': {
                levelValue = ch.qos.logback.classic.Level.TRACE;
                break;
            }
            case 's': {
                levelValue = ch.qos.logback.classic.Level.ERROR;
                break;
            }
            case 'f': {
                if (lowerCase.equals("fatal")) {
                    levelValue = ch.qos.logback.classic.Level.ERROR;
                }
                else if (lowerCase.equals("finer")) {
                    levelValue = ch.qos.logback.classic.Level.TRACE;
                }
                else if (lowerCase.equals("finest")) {
                    levelValue = ch.qos.logback.classic.Level.TRACE;
                }
                else if (lowerCase.equals("fine")) {
                    levelValue = ch.qos.logback.classic.Level.DEBUG;
                }
                else {
                    // TODO : how to indicate a problem!
                    levelValue = ch.qos.logback.classic.Level.INFO;
                }
                break;
            }
            case 'w': {
                levelValue = ch.qos.logback.classic.Level.WARN;
                break;
            }
            case 'i': {
                levelValue = ch.qos.logback.classic.Level.INFO;
                break;
            }
            case 'c': {
                levelValue = ch.qos.logback.classic.Level.DEBUG;
                break;
            }
            default: {
                levelValue = ch.qos.logback.classic.Level.INFO;
            }
        }

        return levelValue;

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

    public void setMaxDispatchQueueSize(int maxQueueSize) {
        appenderHelper.setMaxDispatchQueueSize(maxQueueSize);
    }

    public int getMaxDispatchQueueSize() {
        return appenderHelper.getMaxDispatchQueueSize();
    }

    public void setDontThrowExceptionsIfHubIsntUp(boolean dontThrowExceptionsIfHubIsntUp) {
        appenderHelper.setDontThrowExceptionsIfHubIsntUp(dontThrowExceptionsIfHubIsntUp);
    }

    public boolean isDontThrowExceptionsIfHubIsntUp() {
        return appenderHelper.isDontThrowExceptionsIfHubIsntUp();
    }

    public void setSourceApplication(String sourceApplication) {
        appenderHelper.setSourceApplication(sourceApplication);
    }

    @Override public void setEnvironment(String environment) {
        appenderHelper.setEnvironment(environment);
    }

    @Override public void setInstanceNumber(int instanceNumber) {
        appenderHelper.setInstanceNumber(instanceNumber);
    }

    public String getSourceApplication() {
        return appenderHelper.getSourceApplication();
    }

    public void setHost(String host) {
        appenderHelper.setHost(host);
    }

    public synchronized void setPublishMachineTelemetry(boolean publishMachineTelemetry) {
        appenderHelper.setPublishProcessTelemetry(publishMachineTelemetry);
    }

    public synchronized void setTelemetry(String connectionString) {
        appenderHelper.setTelemetry(connectionString);
    }

    public String getTelemetry() {
        return appenderHelper.getTelemetry();
    }

    public synchronized void setPublishProcessTelemetry(boolean publishProcessTelemetry) {
        appenderHelper.setPublishProcessTelemetry(publishProcessTelemetry);
    }

    public void setJava7GCLogging(boolean enabled) {
        appenderHelper.setJava7GCLogging(enabled);
    }

    public boolean isPublishMachineTelemetry() {
        return appenderHelper.isPublishMachineTelemetry();
    }

    public boolean isPublishProcessTelemetry() {
        return appenderHelper.isPublishProcessTelemetry();
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

    public void setUseDispatchThread(boolean value) {
        appenderHelper.setUseDispatchThread(value);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // The logback classic appender interface
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override protected void append(ILoggingEvent record) {
        final LogbackDetailsSnapshot details = LogbackDetailsSnapshot.fromLoggingEvent(record, appenderHelper.getTimeProvider());
        appenderHelper.append(new AppenderHelperEventConvertor() {

            public EventSnapshot createSnapshot() {
                return new EventSnapshot() {
                    public LogEvent rebuildEvent() {
                        return createLogEvent();
                    }
                };

            }

            public LogEvent createLogEvent() {
                ILoggingEvent record = details.getLoggingEvent();
                LogbackLogEvent event = new LogbackLogEvent(record,
                                                            appenderHelper.getSourceApplication(),
                                                            appenderHelper.getHost(),
                                                            record.getThreadName(),
                                                            details);
                event.setPid(appenderHelper.getPid());
                event.setChannel(appenderHelper.getChannel());
                return event;
            }
        });
    }

    @Override public void start() {
        super.start();
        appenderHelper.start();
    }

    @Override public void stop() {
        super.stop();
        appenderHelper.close();
    }

    public void setPublishingListener(PublishingListener publishingListener) {
        appenderHelper.setPublishingListener(publishingListener);
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        appenderHelper.setTimeProvider(timeProvider);
    }

    public void setChannel(String channel) {
        appenderHelper.setChannel(channel);
    }

    public String getChannel() {
        return appenderHelper.getChannel();
    }

    /**
     * For testing purposes only.
     * 
     * @param socketClient
     */
    public void setSocketClient(SocketClient socketClient) {
        appenderHelper.setSocketClient(socketClient);
    }

    public BlockingDeque getEventsToBeDispatched() {
        return appenderHelper.getEventsToBeDispatched();
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

    public void setGCLogging(boolean value) {
        appenderHelper.setGCLogging(name);
    }

    public boolean isStackTraceModuleEnabled() {
        return appenderHelper.isStackTraceModuleEnabled();
    }

    public String getStackTraceModuleBroadcastInterval() {
        return appenderHelper.getStackTraceModuleBroadcastInterval();
    }

    public void setStackTraceModuleBroadcastInterval(String string) {
        appenderHelper.setStackTraceModuleBroadcastInterval(string);
    }

    public void setStackTraceModuleEnabled(boolean value) {
        appenderHelper.setStackTraceModuleEnabled(value);
    }

    public AppenderHelper getAppenderHelper() {
        return appenderHelper;
    }
}
