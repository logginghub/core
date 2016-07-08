package com.logginghub.logging.internallogging;

import com.logginghub.logging.AppenderHelper;
import com.logginghub.logging.AppenderHelperCustomisationInterface;
import com.logginghub.logging.AppenderHelperEventConvertor;
import com.logginghub.logging.CpuLogger;
import com.logginghub.logging.EventSnapshot;
import com.logginghub.logging.GCFileWatcher;
import com.logginghub.logging.HeapLogger;
import com.logginghub.logging.LevelSettingImplementation;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LoggingPorts;
import com.logginghub.logging.StandardAppenderFeatures;
import com.logginghub.logging.VLLogEvent;
import com.logginghub.logging.api.levelsetting.LevelSetting;
import com.logginghub.logging.api.levelsetting.LevelSettingsGroup;
import com.logginghub.logging.api.levelsetting.LevelSettingsRequest;
import com.logginghub.utils.EnvironmentProperties;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.logging.LoggerPerformanceInterface.EventContext;
import com.logginghub.utils.logging.LoggerStream;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Logging Hub Connector for LoggingHub's own internal logging implementation.
 *
 * @author jshaw
 */
public class LoggingHubStream implements LoggerStream, StandardAppenderFeatures {

    private AppenderHelper appenderHelper;

    private String propertiesPrefix = "logginghub";
    private int levelFilter = Logger.stats;

    public LoggingHubStream() {
        this("");
    }

    public LoggingHubStream(String name) {
        appenderHelper = new AppenderHelper(name, new AppenderHelperCustomisationInterface() {
            public HeapLogger createHeapLogger() {
                final Logger logger = Logger.getLoggerFor("heap-logger");
                return new HeapLogger() {
                    @Override protected void log(String format) {
                        logger.fine(format);
                    }
                };
            }

            public GCFileWatcher createGCWatcher() {
                final Logger logger = Logger.getLoggerFor("gc-logger");
                return new GCFileWatcher() {
                    @Override protected void log(String gcLine) {
                        logger.fine(gcLine);
                    }
                };
            }

            public CpuLogger createCPULogger() {
                final Logger logger = Logger.getLoggerFor("cpu-logger");
                return new CpuLogger() {
                    @Override protected void log(String message) {
                        logger.fine(message);
                    }
                };
            }
        });

        appenderHelper.setLevelSettingImplementation(new LevelSettingImplementation() {
            public boolean process(LevelSettingsRequest request) {
                return setLogLevels(request);
            }
        });

    }

    // //////////////////////////////////////////////////////////////////
    // Accessors
    // //////////////////////////////////////////////////////////////////

    public void addConnectionPoint(InetSocketAddress inetSocketAddress) {
        appenderHelper.addConnectionPoint(inetSocketAddress);
    }

    public void removeConnectionPoint(InetSocketAddress inetSocketAddress) {
        appenderHelper.removeConnectionPoint(inetSocketAddress);
    }

    public void setLevelFilter(int levelFilter) {
        this.levelFilter = levelFilter;
    }

    public int getLevelFilter() {
        return levelFilter;
    }

    public void setSourceApplication(String sourceApplication) {
        propertiesPrefix = sourceApplication;
        appenderHelper.setSourceApplication(sourceApplication);
    }

    public void setEnvironment(String environment) {
        appenderHelper.setEnvironment(environment);
    }

    public void setInstanceIdentifier(String instanceIdentifier) {
        appenderHelper.setInstanceIdentifier(instanceIdentifier);
    }

    public void setInstanceType(String instanceType) {
        appenderHelper.setInstanceType(instanceType);
    }

    public String getSourceApplication() {
        return appenderHelper.getSourceApplication();
    }

    public void startPropertiesWatcher() {

        String fixedLocation = EnvironmentProperties.getString("logginghub.properties");

        File properties = null;

        if (fixedLocation != null) {
            properties = new File(fixedLocation);
        }

        final String userhome = System.getProperty("user.home");

        if (properties == null) {
            properties = new File(userhome, ".logginghub/" + getSourceApplication() + ".properties");
        }

        if (!properties.exists()) {
            properties = new File(userhome, ".logginghub/logging.properties");
        }

        if (properties.exists()) {
            loadPropertiesFromResource(properties);
            final File propertiesFile = properties;

            if (properties.exists()) {
                WorkerThread.everyNowDaemon("LoggingHubStream-LoggerConfigReader", 5, TimeUnit.SECONDS, new Runnable() {
                    long time = 0;

                    public void run() {
                        long fileTime = propertiesFile.lastModified();
                        if (time != fileTime) {
                            loadPropertiesFromResource(propertiesFile);
                            time = fileTime;
                        }
                    }
                });
            }
        }
    }

    private void loadPropertiesFromResource(File propertiesFile) {
        Metadata metadata = Metadata.fromFile(propertiesFile);
        reconfigure(metadata);
    }

    private void reconfigure(Metadata metadata) {
        appenderHelper.stop();

        String applicationNameProperty = metadata.getString("applicationName");
        if (applicationNameProperty != null) {
            setSourceApplication(applicationNameProperty);
        }

        String property = metadata.getString("connectionPoints");
        if (property != null) {
            List<InetSocketAddress> parseAddressAndPortList = NetUtils.toInetSocketAddressList(property, LoggingPorts.getSocketHubDefaultPort());
            appenderHelper.replaceConnectionList(parseAddressAndPortList);
        }

        // This is the log4j style way of setting connection points, it'll compliment the other
        // setting rather than override it
        String host = metadata.getString("host");
        if (host != null) {
            setHost(host);
        }

        String forceFlushProperty = metadata.getString("forceFlush");
        if (forceFlushProperty != null) {
            boolean value = Boolean.parseBoolean(forceFlushProperty);
            setForceFlush(value);
        }

        String publishProcessTelemetry = metadata.getString("publishProcessTelemetry");
        if (publishProcessTelemetry != null) {
            setPublishProcessTelemetry(Boolean.parseBoolean(publishProcessTelemetry));
        }

        String publishMachineTelemetry = metadata.getString("publishMachineTelemetry");
        if (publishMachineTelemetry != null) {
            setPublishMachineTelemetry(Boolean.parseBoolean(publishMachineTelemetry));
        }

        String useDispatchThread = metadata.getString("useDispatchThread");
        if (useDispatchThread != null) {
            setUseDispatchThread(Boolean.parseBoolean(useDispatchThread));
        }

        String telemetry = metadata.getString("telemetry");
        if (telemetry != null) {
            setTelemetry(telemetry);
        }

        String channel = metadata.getString("channel");
        if (channel != null) {
            appenderHelper.setChannel(channel);
        }

        String stackTraceModuleBroadcastInterval = metadata.getString("stackTraceModuleBroadcastInterval");
        if (stackTraceModuleBroadcastInterval != null) {
            setStackTraceModuleBroadcastInterval(stackTraceModuleBroadcastInterval);
        }

        String stackTraceModuleEnabled = metadata.getString("stackTraceModuleEnabled");
        if (stackTraceModuleEnabled != null) {
            setStackTraceModuleEnabled(Boolean.parseBoolean(stackTraceModuleEnabled));
        }

        String gatherCallerDetails = metadata.getString("gatherCallerDetails");
        if (gatherCallerDetails != null) {
            setGatheringCallerDetails(Boolean.parseBoolean(gatherCallerDetails));
        }

        String java7GC = metadata.getString("java7GCLogging");
        if (java7GC != null) {
            setJava7GCLogging(Boolean.parseBoolean(java7GC));
        }

        String failureDelay = metadata.getString("failureDelayMaximum");
        String failureDelayMaximum = metadata.getString("failureDelayMaximum");
        String failureDelayMultiplier = metadata.getString("failureDelayMultiplier");
        String writeQueueOverflowPolicy = metadata.getString("writeQueueOverflowPolicy");
        String cpuLogging = metadata.getString("cpuLogging");
        String gcLogging = metadata.getString("gcLogging");
        String heapLogging = metadata.getString("heapLogging");
        String maximumQueuedMessages = metadata.getString("maximumQueuedMessages");
        String dontThrowExceptionsIfHubIsntUp = metadata.getString("dontThrowExceptionsIfHubIsntUp");

        if (maximumQueuedMessages != null) {
            setMaximumQueuedMessages(Integer.parseInt(maximumQueuedMessages));
        }

        if (failureDelay != null) {
            setFailureDelay(Long.parseLong(failureDelay));
        }

        if (failureDelayMaximum != null) {
            setFailureDelayMaximum(Long.parseLong(failureDelayMaximum));
        }

        if (failureDelayMultiplier != null) {
            setFailureDelayMultiplier(Long.parseLong(failureDelayMultiplier));
        }

        if (writeQueueOverflowPolicy != null) {
            setWriteQueueOverflowPolicy(writeQueueOverflowPolicy);
        }

        if (cpuLogging != null) {
            setCpuLogging(Boolean.parseBoolean(cpuLogging));
        }

        if (dontThrowExceptionsIfHubIsntUp != null) {
            setDontThrowExceptionsIfHubIsntUp(Boolean.parseBoolean(dontThrowExceptionsIfHubIsntUp));
        }

        if (gcLogging != null) {
            setGCLogging(gcLogging);
        }

        if (heapLogging != null) {
            setHeapLogging(Boolean.parseBoolean(heapLogging));
        }

        appenderHelper.start();
    }

    public void start() {
        appenderHelper.start();
    }

    public void stop() {
        appenderHelper.stop();
    }

    public void waitUntilAllRecordsHaveBeenPublished() {
        appenderHelper.waitUntilAllRecordsHaveBeenPublished();
    }

    public void setUseDispatchThread(boolean value) {
        appenderHelper.setUseDispatchThread(value);
    }

    public void setForceFlush(boolean b) {
        appenderHelper.setForceFlush(b);
    }

    public synchronized void setPublishMachineTelemetry(boolean publishMachineTelemetry) {
        appenderHelper.setPublishMachineTelemetry(publishMachineTelemetry);
    }

    public void setPublishHumanReadableTelemetry(boolean publishHumanReadableTelemetry) {
        this.appenderHelper.setPublishHumanReadableTelemetry(publishHumanReadableTelemetry);
    }

    public synchronized void setTelemetry(String connectionString) {
        appenderHelper.setTelemetry(connectionString);
    }

    public synchronized void setPublishProcessTelemetry(boolean publishProcessTelemetry) {
        appenderHelper.setPublishProcessTelemetry(publishProcessTelemetry);
    }

    public void setDontThrowExceptionsIfHubIsntUp(boolean dontThrowExceptionsIfHubIsntUp) {
        appenderHelper.setDontThrowExceptionsIfHubIsntUp(dontThrowExceptionsIfHubIsntUp);
    }

    public void setMaximumQueuedMessages(int maximumQueuedMessages) {
        appenderHelper.setMaxDispatchQueueSize(maximumQueuedMessages);
    }

    public void setGatheringCallerDetails(boolean gatheringCallerDetails) {
        appenderHelper.setGatheringCallerDetails(gatheringCallerDetails);
    }

    public AppenderHelper getAppenderHelper() {
        return appenderHelper;
    }

    public void setFailureDelayMaximum(long failureDelayMaximum) {
        appenderHelper.setFailureDelayMaximum(failureDelayMaximum);
    }

    public void setFailureDelayMultiplier(double failureDelayMultiplier) {
        appenderHelper.setFailureDelayMultiplier(failureDelayMultiplier);
    }

    public void setWriteQueueOverflowPolicy(String policy) {
        appenderHelper.setWriteQueueOverflowPolicy(policy);
    }

    public void setHost(String host) {
        appenderHelper.addConnectionPoint(host);
    }

    public void setCpuLogging(boolean value) {
        appenderHelper.setCpuLogging(value);
    }

    public void setGCLogging(String path) {
        appenderHelper.setGCLogging(path);
    }

    public void setHeapLogging(boolean value) {
        appenderHelper.setHeapLogging(value);
    }

    public void setFailureDelay(long failureDelay) {
        appenderHelper.setFailureDelay(failureDelay);
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

    public void setJava7GCLogging(boolean enabled) {
        appenderHelper.setJava7GCLogging(enabled);
    }

    public void setStackTraceModuleEnabled(boolean value) {
        appenderHelper.setStackTraceModuleEnabled(value);
    }

    protected boolean setLogLevels(LevelSettingsRequest request) {

        LevelSettingsGroup levelSettingsGroup = request.getLevelSettings();
        List<LevelSetting> settings = levelSettingsGroup.getSettings();
        for (LevelSetting levelSetting : settings) {
            String levelText = levelSetting.getLevel();
            String loggerName = levelSetting.getLoggerName();
            Logger.setLevel(loggerName, Logger.parseLevel(levelText));
        }

        return true;
    }


    public void onNewLogEvent(final com.logginghub.utils.logging.LogEvent vlevent) {
        if(vlevent.getLevel() >= levelFilter) {
            if (appenderHelper.isGatheringCallerDetails()) {
                // Forces lazy gathering of caller details
            }

            appenderHelper.append(new AppenderHelperEventConvertor() {
                public EventSnapshot createSnapshot() {
                    return new EventSnapshot() {
                        public LogEvent rebuildEvent() {
                            return createLogEvent();
                        }
                    };
                }

                public LogEvent createLogEvent() {
                    VLLogEvent event = new VLLogEvent(vlevent,
                                                      appenderHelper.getPid(),
                                                      appenderHelper.getSourceApplication(),
                                                      appenderHelper.getSourceAddress(),
                                                      appenderHelper.getSourceHost());
                    event.setChannel(appenderHelper.getChannel());
                    return event;
                }
            });
        }
    }

    public void onNewLogEvent(EventContext eventContext) {

    }

    public void setHostOverride(String hostOverride) {
        appenderHelper.setSourceHostX(hostOverride);
    }

    public void setHostAddressOverride(String hostAddressOverride) {
        appenderHelper.setSourceAddressOverride(hostAddressOverride);
    }

    public void setReportsModuleEnabled(boolean value) {
        this.appenderHelper.setReportsModuleEnabled(value);
    }

    public void setReportsModuleConfiguration(String path) {
        this.appenderHelper.setReportsConfigurationPath(path);
    }
}
