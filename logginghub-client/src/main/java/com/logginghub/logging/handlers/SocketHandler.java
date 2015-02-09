package com.logginghub.logging.handlers;

import com.logginghub.logging.AppenderHelper;
import com.logginghub.logging.AppenderHelperCustomisationInterface;
import com.logginghub.logging.AppenderHelperEventConvertor;
import com.logginghub.logging.CpuLogger;
import com.logginghub.logging.EventSnapshot;
import com.logginghub.logging.GCFileWatcher;
import com.logginghub.logging.HeapLogger;
import com.logginghub.logging.JuliLogEvent;
import com.logginghub.logging.LevelSettingImplementation;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LoggingPorts;
import com.logginghub.logging.StandardAppenderFeatures;
import com.logginghub.logging.api.levelsetting.LevelSetting;
import com.logginghub.logging.api.levelsetting.LevelSettingsGroup;
import com.logginghub.logging.api.levelsetting.LevelSettingsRequest;
import com.logginghub.logging.juli.JULDetailsSnapshot;
import com.logginghub.logging.utils.LoggingUtils;
import com.logginghub.utils.NetUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Java.util.logger implementation of the Logging Hub Connector.
 *
 * @author admin
 */
public class SocketHandler extends Handler implements PropertyChangeListener, StandardAppenderFeatures {

    private AppenderHelper appenderHelper;

    public SocketHandler() {
        this("");
    }

    public SocketHandler(String name) {
        appenderHelper = new AppenderHelper(name, new AppenderHelperCustomisationInterface() {
            public HeapLogger createHeapLogger() {
                final Logger logger = Logger.getLogger("heap-logger");
                return new HeapLogger() {
                    @Override protected void log(String format) {
                        logger.fine(format);
                    }
                };
            }

            public GCFileWatcher createGCWatcher() {
                final Logger logger = Logger.getLogger("gc-logger");
                return new GCFileWatcher() {
                    @Override protected void log(String gcLine) {
                        logger.fine(gcLine);
                    }
                };
            }

            public CpuLogger createCPULogger() {
                final Logger logger = Logger.getLogger("cpu-logger");
                return new CpuLogger() {
                    @Override protected void log(String message) {
                        logger.fine(message);
                    }
                };
            }
        });

        appenderHelper.setLevelSettingImplementation(new LevelSettingImplementation() {
            @Override public boolean process(LevelSettingsRequest request) {
                return setLogLevels(request);
            }
        });

        LogManager manager = LogManager.getLogManager();
        manager.addPropertyChangeListener(this);
        reconfigure();
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

    public void setSourceApplication(String sourceApplication) {
        appenderHelper.setSourceApplication(sourceApplication);
    }

    @Override public void setEnvironment(String environment) {
        appenderHelper.setEnvironment(environment);
    }

    @Override public void setInstanceIdentifier(String instanceIdentifier) {
        appenderHelper.setInstanceIdentifier(instanceIdentifier);
    }

    @Override public void setInstanceType(String instanceType) {
        appenderHelper.setInstanceType(instanceType);
    }

    public String getSourceApplication() {
        return appenderHelper.getSourceApplication();
    }

    // //////////////////////////////////////////////////////////////////
    // Handler overrides
    // //////////////////////////////////////////////////////////////////

    @Override public void close() throws SecurityException {
        appenderHelper.close();

        LogManager manager = LogManager.getLogManager();
        manager.removePropertyChangeListener(this);
    }

    @Override public void flush() {
        appenderHelper.flush();
    }

    @Override public void publish(final LogRecord record) {

        if (appenderHelper.isGatheringCallerDetails()) {
            // Forces lazy gathering of caller details
            record.getSourceClassName();
        }
        final JULDetailsSnapshot snapshot = JULDetailsSnapshot.fromLoggingEvent(record, appenderHelper.getTimeProvider());

        appenderHelper.append(new AppenderHelperEventConvertor() {

            public EventSnapshot createSnapshot() {
                return new EventSnapshot() {
                    public LogEvent rebuildEvent() {
                        return createLogEvent();
                    }
                };
            }

            public LogEvent createLogEvent() {
                JuliLogEvent event = new JuliLogEvent(record, appenderHelper.getSourceApplication(), appenderHelper.getHost(), snapshot.getThreadName(), appenderHelper.isGatheringCallerDetails());
                event.setPid(appenderHelper.getPid());
                event.setChannel(appenderHelper.getChannel());
                return event;
            }
        });
    }

    // //////////////////////////////////////////////////////////////////
    // The LogManager property change implementation
    // //////////////////////////////////////////////////////////////////

    public void propertyChange(PropertyChangeEvent evt) {
        reconfigure();
    }

    // //////////////////////////////////////////////////////////////////
    // Private methods
    // //////////////////////////////////////////////////////////////////

    /*
     * private LogEvent getEventForThread() { LogEvent fullLogEvent = m_logEventsByThread.get();
     * 
     * if(fullLogEvent == null) { fullLogEvent = new LogEvent();
     * m_logEventsByThread.set(fullLogEvent); }
     * 
     * return fullLogEvent; }
     */

    private void reconfigure() {
        appenderHelper.stop();

        LogManager manager = LogManager.getLogManager();

        String cname = getClass().getName();

        Level level = LoggingUtils.getLevelProperty(cname + ".level");
        if (level != null) {
            setLevel(level);
        }

        String applicationNameProperty = manager.getProperty(cname + ".applicationName");
        if (applicationNameProperty != null) {
            setSourceApplication(applicationNameProperty);
        }

        String property = manager.getProperty(cname + ".connectionPoints");
        if (property != null) {
            List<InetSocketAddress> parseAddressAndPortList = NetUtils.toInetSocketAddressList(property, LoggingPorts.getSocketHubDefaultPort());
            appenderHelper.replaceConnectionList(parseAddressAndPortList);
        }

        // This is the log4j style way of setting connection points, it'll compliment the other
        // setting rather than override it
        String host = manager.getProperty(cname + ".host");
        if (host != null) {
            setHost(host);
        }

        String forceFlushProperty = manager.getProperty(cname + ".forceFlush");
        if (forceFlushProperty != null) {
            boolean value = Boolean.parseBoolean(forceFlushProperty);
            setForceFlush(value);
        }

        String publishProcessTelemetry = manager.getProperty(cname + ".publishProcessTelemetry");
        if (publishProcessTelemetry != null) {
            setPublishProcessTelemetry(Boolean.parseBoolean(publishProcessTelemetry));
        }

        String publishMachineTelemetry = manager.getProperty(cname + ".publishMachineTelemetry");
        if (publishMachineTelemetry != null) {
            setPublishMachineTelemetry(Boolean.parseBoolean(publishMachineTelemetry));
        }

        String useDispatchThread = manager.getProperty(cname + ".useDispatchThread");
        if (useDispatchThread != null) {
            setUseDispatchThread(Boolean.parseBoolean(useDispatchThread));
        }

        String telemetry = manager.getProperty(cname + ".telemetry");
        if (telemetry != null) {
            setTelemetry(telemetry);
        }

        String channel = manager.getProperty(cname + ".channel");
        if (channel != null) {
            appenderHelper.setChannel(channel);
        }

        String stackTraceModuleBroadcastInterval = manager.getProperty(cname + ".stackTraceModuleBroadcastInterval");
        if (stackTraceModuleBroadcastInterval != null) {
            setStackTraceModuleBroadcastInterval(stackTraceModuleBroadcastInterval);
        }

        String stackTraceModuleEnabled = manager.getProperty(cname + ".stackTraceModuleEnabled");
        if (stackTraceModuleEnabled != null) {
            setStackTraceModuleEnabled(Boolean.parseBoolean(stackTraceModuleEnabled));
        }

        String gatherCallerDetails = manager.getProperty(cname + ".gatherCallerDetails");
        if (gatherCallerDetails != null) {
            setGatheringCallerDetails(Boolean.parseBoolean(gatherCallerDetails));
        }

        String java7GC = manager.getProperty(cname + ".java7GCLogging");
        if (java7GC != null) {
            setJava7GCLogging(Boolean.parseBoolean(java7GC));
        }

        String instanceIdentifier = manager.getProperty(cname + ".instanceIdentifier");
        if (instanceIdentifier != null) {
            setInstanceIdentifier(instanceIdentifier);
        }

        String instanceType = manager.getProperty(cname + ".instanceType");
        if (instanceType != null) {
            setInstanceType(instanceType);
        }

        String environment = manager.getProperty(cname + ".environment");
        if (environment != null) {
            setEnvironment(environment);
        }

        String reportsModuleEnabled = manager.getProperty(cname + ".reportsModuleEnabled");
        if (reportsModuleEnabled != null) {
            setReportsModuleEnabled(Boolean.parseBoolean(reportsModuleEnabled));
        }

        String reportsModuleConfiguration = manager.getProperty(cname + ".reportsModuleConfiguration");
        if (reportsModuleConfiguration != null) {
            setReportsModuleConfiguration(reportsModuleConfiguration);
        }

        String failureDelay = manager.getProperty(cname + ".failureDelayMaximum");
        String failureDelayMaximum = manager.getProperty(cname + ".failureDelayMaximum");
        String failureDelayMultiplier = manager.getProperty(cname + ".failureDelayMultiplier");
        String writeQueueOverflowPolicy = manager.getProperty(cname + ".writeQueueOverflowPolicy");
        String cpuLogging = manager.getProperty(cname + ".cpuLogging");
        String gcLogging = manager.getProperty(cname + ".gcLogging");
        String heapLogging = manager.getProperty(cname + ".heapLogging");
        String maximumQueuedMessages = manager.getProperty(cname + ".maximumQueuedMessages");
        String dontThrowExceptionsIfHubIsntUp = manager.getProperty(cname + ".dontThrowExceptionsIfHubIsntUp");

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

    // //////////////////////////////////////////////////////////////////
    // Protected methods
    // //////////////////////////////////////////////////////////////////

    /**
     * @return the publisher instance.
     */
    // private SocketPublisher getPublisher()
    // {
    // return m_publisher;
    // }

    /**
     * Convert a LogRecord into a LogEvent, uses a thread local so its nice and fast
     *
     * @param record
     * @return
     */
    /*
     * private LogEvent convert(LogRecord record) { LogEvent eventForThread = getEventForThread();
     * eventForThread.populateFromLogRecord(record, m_sourceApplication);
     * eventForThread.setThreadName(m_threadNames.remove(record)); return eventForThread; }
     */
    public void waitUntilAllRecordsHaveBeenPublished() {
        appenderHelper.waitUntilAllRecordsHaveBeenPublished();
    }

    public void setUseDispatchThread(boolean value) {
        appenderHelper.setUseDispatchThread(value);
    }

    // public void publish(TelemetryStack telemetryStackForThread) throws
    // LoggingMessageSenderException {
    // appenderHelper.publish(telemetryStackForThread);
    // }

    public void setForceFlush(boolean b) {
        appenderHelper.setForceFlush(b);
    }

    public synchronized void setPublishMachineTelemetry(boolean publishMachineTelemetry) {
        appenderHelper.setPublishMachineTelemetry(publishMachineTelemetry);
    }

    public synchronized void setTelemetry(String connectionString) {
        appenderHelper.setTelemetry(connectionString);
    }

    // public String getTelemetry() {
    // return appenderHelper.getTelemetry();
    // }

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

    // public boolean isGatheringCallerDetails() {
    // return appenderHelper.isGatheringCallerDetails();
    // }

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

            java.util.logging.Level level = parseLevel(levelText);
            java.util.logging.Logger.getLogger(loggerName).setLevel(level);
        }

        return true;
    }

    private Level parseLevel(String level) {

        Level levelValue;

        String lowerCase = level.toLowerCase();
        char first = lowerCase.charAt(0);
        switch (first) {
            case 'a': {
                levelValue = Level.ALL;
                break;
            }
            case 'd': {
                levelValue = Level.FINE;
                break;
            }
            case 't': {
                levelValue = Level.FINEST;
                break;
            }
            case 's': {
                levelValue = Level.SEVERE;
                break;
            }
            case 'f': {
                if (lowerCase.equals("fatal")) {
                    levelValue = Level.SEVERE;
                } else if (lowerCase.equals("finer")) {
                    levelValue = Level.FINER;
                } else if (lowerCase.equals("finest")) {
                    levelValue = Level.FINEST;
                } else if (lowerCase.equals("fine")) {
                    levelValue = Level.FINE;
                } else {
                    // TODO : how to indicate a problem!
                    levelValue = Level.INFO;
                }
                break;
            }
            case 'w': {
                levelValue = Level.WARNING;
                break;
            }
            case 'i': {
                levelValue = Level.INFO;
                break;
            }
            case 'c': {
                levelValue = Level.CONFIG;
                break;
            }
            default: {
                levelValue = Level.INFO;
            }
        }

        return levelValue;

    }


    public void setReportsModuleEnabled(boolean value) {
        this.appenderHelper.setReportsModuleEnabled(value);
    }

    public void setReportsModuleConfiguration(String path) {
        this.appenderHelper.setReportsConfigurationPath(path);
    }
}
