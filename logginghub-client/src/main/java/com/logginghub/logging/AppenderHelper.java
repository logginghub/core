package com.logginghub.logging;

import com.google.gson.JsonSyntaxException;
import com.logginghub.logging.api.levelsetting.InstanceFilter;
import com.logginghub.logging.api.levelsetting.LevelSettingsConfirmation;
import com.logginghub.logging.api.levelsetting.LevelSettingsRequest;
import com.logginghub.logging.api.patterns.InstanceDetails;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.log4j.PublishingListener;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.InstanceKey;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.ReportExecuteRequest;
import com.logginghub.logging.messages.ReportExecuteResponse;
import com.logginghub.logging.messages.ReportExecuteResult;
import com.logginghub.logging.messages.ReportListRequest;
import com.logginghub.logging.messages.ReportListResponse;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.logging.messaging.SocketConnection.SlowSendingPolicy;
import com.logginghub.logging.modules.StackCaptureConfiguration;
import com.logginghub.logging.modules.StackCaptureModule;
import com.logginghub.logging.telemetry.MachineTelemetryGenerator;
import com.logginghub.logging.telemetry.ProcessTelemetryGenerator;
import com.logginghub.logging.telemetry.SigarTelemetryHelper;
import com.logginghub.logging.telemetry.TelemetryHelper;
import com.logginghub.logging.utils.InstanceDetailsContainsFilter;
import com.logginghub.logging.utils.Java7GCMonitor;
import com.logginghub.utils.Destination;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Out;
import com.logginghub.utils.Result;
import com.logginghub.utils.RunnableWorkerThread;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.logging.GlobalLoggingParameters;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Because the j.u.l and log4j handlers/appenders need to extend base classes, we have to include our generic implementation via composition.
 *
 * @author James
 */
public class AppenderHelper {

    private static final Logger logger = Logger.getLoggerFor(AppenderHelper.class);
    private AppenderHelperCustomisationInterface customisationInterface;

    // private SocketPublisher m_publisher = new SocketPublisher();
    private SocketClientManager socketClientManager;
    private SocketClient socketClient;
    //    private String sourceApplication = "<unknown source application>";
    // private LinkedList<DetailsSnapshot> eventsToBeDispatched = new
    // LinkedList<DetailsSnapshot>();

    private BlockingDeque eventsToBeDispatched = new LinkedBlockingDeque();
    private long failureDelay = 50;
    private long currentFailureDelay = failureDelay;

    private boolean useDispatchThread = true;
    private InetAddress host;
    private HeapLogger heapLogger;
    private GCFileWatcher gcFileWatcher;
    private Java7GCMonitor gcMonitor = new Java7GCMonitor();
    private CpuLogger cpuLogger;
    private boolean dontThrowExceptionsIfHubIsntUp = false;
    private boolean publishProcessTelemetry = false;
    private boolean publishMachineTelemetry = false;
    private int maxDispatchQueueSize = 1000;
    //    private int pid = -1;

    private boolean reportsModuleEnabled = false;
    private boolean stackTraceModuleEnabled = false;
    private String stackTraceModuleBroadcastInterval = "0";

    private boolean publishHumanReadableTelemetry = false;

    private Destination<DataStructure> telemetryListener = new Destination<DataStructure>() {
        public void send(DataStructure t) {
            try {
                if (socketClient != null && socketClient.isConnected()) {
                    socketClient.send(new ChannelMessage(Channels.telemetryUpdates, t));

                    if (publishHumanReadableTelemetry) {
                        socketClient.send(new LogEventMessage(LogEventBuilder.start().setSourceApplication(instanceKey.getSourceApplication()).setSourceHost(getSourceHost()).setSourceAddress(
                                getSourceAddress()).setChannel("telemetry").setLevel(Logger.fine).setMessage(t.toString()).setPid(instanceKey.getPid()).toLogEvent()));
                    }
                }
            } catch (LoggingMessageSenderException e) {
                // TODO : should we tell anyone about this?
            }
        }
    };

    /**
     * A testability feature: this allows the test to get notifications when events are published asynchronously.
     */
    private PublishingListener publishingListener = null;
    private RunnableWorkerThread dispatcherThread;
    private TelemetryHelper telemetryClient;
    private String telemetry;
    private TimeProvider timeProvider = null;

    private boolean gatheringCallerDetails = false;
    private boolean closing = false;
    private String channel;
    private double failureDelayMultiplier = 2;
    private long failureDelayMaximum = TimeUtils.minutes(1);
    private int discards;
    private StackCaptureModule stackTraceModule;
    private int appended;
    private LevelSettingImplementation levelSettingImplementation;
    //    private String environment;
    //    private int instanceNumber = 1;
    //    private String hostOverride = null;
    //    private String sourceHost;
    //    private String sourceAddress;
    private ReportsHelper reportsHelper;
    private String reportsConfigurationPath = "reports.txt";
    private InstanceKey instanceKey;

    public AppenderHelper(String name, AppenderHelperCustomisationInterface ahci) {
        customisationInterface = ahci;
        socketClient = new SocketClient(name);

        if(SigarSetting.noSigar()) {
            telemetryClient = new NoopTelemetryHelper();
        }else {
            telemetryClient = new SigarTelemetryHelper();
        }

        // LogManager manager = LogManager.getLogManager();
        // manager.addPropertyChangeListener(this);

        // Set the connection to throw old messages away if the write side of
        // the connection starts blocking up
        socketClient.setWriteQueueOverflowPolicy(SlowSendingPolicy.discard);

        // We dont want to get events sent back to us
        socketClient.setAutoSubscribe(false);

        // Add a default connection point for lazy configurations
        socketClient.getConnector().getConnectionPointManager().setDefaultConnectionPoint(new InetSocketAddress("localhost", LoggingPorts.getSocketHubDefaultPort()));

        if (useDispatchThread) {
            Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        runDispatchLoop();
                    } catch (InterruptedException e) {
                        logger.debug("Dispatch thread interupted", e);
                    }
                }
            };

            dispatcherThread = new RunnableWorkerThread("AsynchronousSocketHandler-DispatchThread", runnable);
            dispatcherThread.setDaemon(true);
            dispatcherThread.start();
        }

        instanceKey = new InstanceKey();

        try {
            host = InetAddress.getLocalHost();
            instanceKey.setHost(host.getHostName());
        } catch (UnknownHostException e1) {
            throw new RuntimeException("Failed to get local host", e1);
        }

        // Make a cautious attempt at getting the pid - we dont want things to
        // blow up if this doesn't work though
        int pid = PidHelper.getPid();

        instanceKey.setPid(pid);
        GlobalLoggingParameters.pid = pid;

        stackTraceModule = new StackCaptureModule(null, null);

    }

    private void runDispatchLoop() throws InterruptedException {
        EventSnapshot snapshot = (EventSnapshot) eventsToBeDispatched.takeFirst();
        LogEvent event = snapshot.rebuildEvent();

        try {
            socketClient.send(new LogEventMessage(event));
            if (publishingListener != null) {
                publishingListener.onSuccessfullyPublished(event);
            }

            // Reset the failure delay
            currentFailureDelay = failureDelay;
        } catch (LoggingMessageSenderException ftse) {
            if (!closing) {
                if (publishingListener != null) {
                    publishingListener.onUnsuccessfullyPublished(event, ftse);
                }

                // Stick the event back
                eventsToBeDispatched.putFirst(snapshot);

                if (!isDontThrowExceptionsIfHubIsntUp()) {
                    String message = StringUtils.format("Couldnt connect to any hubs; waiting {} ms until the next connection attempt", currentFailureDelay);
                    LoggingMessageSenderException topLevel = new LoggingMessageSenderException(message, ftse);
                    topLevel.printStackTrace();
                }

                // Do the failure delay sleep
                ThreadUtils.sleep(currentFailureDelay);

                currentFailureDelay *= failureDelayMultiplier;

                // Make sure it doesn't get too crazy
                currentFailureDelay = Math.min(currentFailureDelay, failureDelayMaximum);
            }
        }
    }

    public void setFailureDelayMaximum(long failureDelayMaximum) {
        this.failureDelayMaximum = failureDelayMaximum;
    }

    public void setFailureDelayMultiplier(double failureDelayMultiplier) {
        this.failureDelayMultiplier = failureDelayMultiplier;
    }

    public InetAddress getHost() {
        return host;
    }

    public void addConnectionPoint(String host) {

        host = StringUtils.environmentReplacement(host);

        List<InetSocketAddress> inetSocketAddresses = NetUtils.toInetSocketAddressList(host, VLPorts.getSocketHubDefaultPort());
        socketClient.addConnectionPoints(inetSocketAddresses);

        //        String[] split = host.split(":");

        //        String hostname = split[0];
        //        int port = LoggingPorts.getSocketHubDefaultPort();
        //        if (split.length > 1) {
        //            port = Integer.parseInt(split[1]);
        //        }
        //
        //        GlobalLoggingParameters.destination = hostname;

        //        addConnectionPoint(new InetSocketAddress(hostname, port));
    }

    public int getPid() {
        return instanceKey.getPid();
    }

    /**
     * Testing method; sends an event directly, bypassing the queue
     *
     * @param event
     * @throws LoggingMessageSenderException
     */
    public void sendDirect(LogEvent event) throws LoggingMessageSenderException {
        socketClient.send(new LogEventMessage(event));
    }

    public long getFailureDelay() {
        return failureDelay;
    }

    // //////////////////////////////////////////////////////////////////
    // Accessors
    // //////////////////////////////////////////////////////////////////

    public void setFailureDelay(long failureDelay) {
        this.failureDelay = failureDelay;
    }

    public void addToQueue(EventSnapshot snapshot) {
        eventsToBeDispatched.addLast(snapshot);

        if (eventsToBeDispatched.size() > maxDispatchQueueSize) {
            discards++;
            try {
                eventsToBeDispatched.removeFirst();
            } catch (NoSuchElementException e) {
                // jshaw - it is possible that the dispatcher thread has just sent the entire queue
                // in one go. As we aren't synchronising the queue access on the writer side we just
                // have to suck this up and move on
            }
        }
    }

    public int getDiscards() {
        return discards;
    }

    public void setWriteQueueOverflowPolicy(String policy) {
        socketClient.setWriteQueueOverflowPolicy(SlowSendingPolicy.valueOf(policy));
    }

    public void addConnectionPoint(InetSocketAddress inetSocketAddress) {
        // m_publisher.addConnectionPoint(inetSocketAddress);
        socketClient.addConnectionPoint(inetSocketAddress);
    }

    public void removeConnectionPoint(InetSocketAddress inetSocketAddress) {
        // m_publisher.removeConnectionPoint(inetSocketAddress);
        socketClient.removeConnectionPoint(inetSocketAddress);
    }

    public int getMaxDispatchQueueSize() {
        return maxDispatchQueueSize;
    }

    public void setMaxDispatchQueueSize(int maxQueueSize) {
        this.maxDispatchQueueSize = maxQueueSize;
    }

    public boolean isDontThrowExceptionsIfHubIsntUp() {
        return dontThrowExceptionsIfHubIsntUp;
    }

    public void setDontThrowExceptionsIfHubIsntUp(boolean dontThrowExceptionsIfHubIsntUp) {
        this.dontThrowExceptionsIfHubIsntUp = dontThrowExceptionsIfHubIsntUp;
    }

    public String getSourceApplication() {
        return instanceKey.getSourceApplication();
    }

    public void setSourceApplication(String sourceApplication) {

        // jshaw - we never followed through with this change, and its annoying logging you can't get rid off!
        //Out.err("[logginghub] setSourceApplication has been deprecated - you should use setInstanceType and setInstanceIdentifier instead to provide richer metadata. We'll try and parse it as " + "best we can.");

        String actualValue = StringUtils.environmentReplacement(sourceApplication);

        try {
            String trailingNumber = StringUtils.trailingInteger(sourceApplication);
            if (StringUtils.isNotNullOrEmpty(trailingNumber)) {
                String remained = StringUtils.before(sourceApplication, trailingNumber);
                if (remained.endsWith(".") || remained.endsWith("-")) {
                    remained = remained.substring(0, remained.length() - 1);
                }

                this.instanceKey.setInstanceType(remained);
                this.instanceKey.setInstanceIdentifier(trailingNumber);
            } else {
                this.instanceKey.setInstanceType(actualValue);
                this.instanceKey.setInstanceIdentifier(null);
            }
        } catch (RuntimeException e) {
            Out.err("Failed to parse application name - will use default values : {}", e.getMessage());
        }

        telemetryClient.setSourceApplication(actualValue);
        GlobalLoggingParameters.applicationName = actualValue;
    }

    public String getTelemetry() {
        return telemetry;
    }

    // TODO : refactor this to be a boolean? Its a bit daft just having it as a random method that
    // just needs to have /something/ passed in.
    public synchronized void setTelemetry(String connectionString) {
        // connectionString = StringUtils.environmentReplacement(connectionString);
        // this.telemetry = connectionString;
        // if (telemetryClient == null) {
        //
        //
        // if (publishMachineTelemetry) {
        // MachineTelemetryGenerator machineTelemetryGenerator =
        // telemetryClient.startMachineTelemetryGenerator();
        // machineTelemetryGenerator.getDataStructureMultiplexer().addDestination(listener);
        // }
        //
        // if (publishProcessTelemetry) {
        // ProcessTelemetryGenerator processTelemetryGenerator =
        // telemetryClient.startProcessTelemetryGenerator(sourceApplication);
        // processTelemetryGenerator.getDataStructureMultiplexer().addDestination(listener);
        // }
        // }
    }

    public boolean isPublishMachineTelemetry() {
        return publishMachineTelemetry;
    }

    public synchronized void setPublishMachineTelemetry(boolean publishMachineTelemetry) {
        this.publishMachineTelemetry = publishMachineTelemetry;

        if (publishMachineTelemetry) {
            MachineTelemetryGenerator machineTelemetryGenerator = telemetryClient.startMachineTelemetryGenerator();
            machineTelemetryGenerator.getDataStructureMultiplexer().addDestination(telemetryListener);
        }
    }

    public boolean isPublishProcessTelemetry() {
        return publishProcessTelemetry;
    }

    public synchronized void setPublishProcessTelemetry(boolean publishProcessTelemetry) {
        this.publishProcessTelemetry = publishProcessTelemetry;

        if (publishProcessTelemetry) {
            ProcessTelemetryGenerator processTelemetryGenerator = telemetryClient.startProcessTelemetryGenerator(instanceKey.getSourceApplication());
            processTelemetryGenerator.getDataStructureMultiplexer().addDestination(telemetryListener);
        }
    }

    public void setForceFlush(boolean forceFlush) {
        socketClient.setForceFlush(forceFlush);
    }

    public boolean isCpuLogging() {
        return cpuLogger != null;
    }

    public synchronized void setCpuLogging(boolean value) {
        if (value && cpuLogger == null) {
            cpuLogger = customisationInterface.createCPULogger();
            cpuLogger.setDisplayPerThreadDetails(false);
            cpuLogger.start();
        }
    }

    public boolean isHeapLogging() {
        return heapLogger != null;
    }

    public synchronized void setHeapLogging(boolean value) {
        if (value && heapLogger == null) {
            heapLogger = customisationInterface.createHeapLogger();
            heapLogger.start();
        }
    }

    public boolean isGCLogging() {
        return gcFileWatcher != null;
    }

    public void setGCLogging(String path) {
        if (gcFileWatcher == null) {
            gcFileWatcher = customisationInterface.createGCWatcher();
            try {
                gcFileWatcher.start(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void setDetailedCpuLogging(boolean value) {
        if (value && cpuLogger == null) {
            cpuLogger = customisationInterface.createCPULogger();
            cpuLogger.setDisplayPerThreadDetails(true);
            cpuLogger.start();
        }
    }

    public void setJava7GCLogging(boolean enabled) {
        if (enabled) {
            gcMonitor.getEventMultiplexer().addDestination(new Destination<Java7GCMonitor.GCEvent>() {
                @Override public void send(Java7GCMonitor.GCEvent gcEvent) {
                    try {
                        socketClient.send(new LogEventMessage(LogEventBuilder.start().setChannel("gcmonitor").setSourceHost(getSourceHost()).setSourceAddress(getSourceAddress()).setSourceApplication(
                                instanceKey.getSourceApplication()).setPid(instanceKey.getPid()).setMessage("GC pause {} ms collected {} kb - {}/{}/{}",
                                gcEvent.duration,
                                NumberFormat.getInstance().format(gcEvent.bytes / 1024f),
                                gcEvent.type,
                                gcEvent.name,
                                gcEvent.cause).toLogEvent()));
                    } catch (LoggingMessageSenderException e) {

                    }
                }
            });
            if (gcMonitor.isSupported()) {
                gcMonitor.installGCMonitoring();
            }
        } else {
            if (gcMonitor.isSupported()) {
                gcMonitor.uninstall();
            }
        }
    }

    public boolean isUseDispatchThread() {
        return useDispatchThread;
    }

    public void setUseDispatchThread(boolean value) {
        useDispatchThread = value;
    }

    public void setLevelSettingImplementation(LevelSettingImplementation levelSettingImplementation) {
        this.levelSettingImplementation = levelSettingImplementation;
    }

    public synchronized void start() {

        socketClient.subscribe(Channels.levelSetting, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                LevelSettingsRequest request = (LevelSettingsRequest) t.getPayload();
                if (levelSettingImplementation != null) {

                    InstanceDetails instanceDetails = getSocketClient().getInstanceDetails();

                    InstanceFilter instanceFilter = request.getFilter();

                    InstanceDetailsContainsFilter containsFilter = new InstanceDetailsContainsFilter();

                    containsFilter.setHostFilter(instanceFilter.getHostFilter());
                    containsFilter.setIPFilter(instanceFilter.getIpFilter());
                    containsFilter.setNameFilter(instanceFilter.getNameFilter());
                    containsFilter.setPidFilter(instanceFilter.getPidFilter());
                    containsFilter.setPortFilter(instanceFilter.getPortFilter());

                    boolean success;

                    if (containsFilter.passes(instanceDetails)) {
                        // Looks like we fit the bill
                        success = levelSettingImplementation.process(request);
                    } else {
                        success = false;
                    }

                    InstanceDetails details = socketClient.getInstanceDetails();

                    LevelSettingsConfirmation confirmation = new LevelSettingsConfirmation();
                    confirmation.setInstanceDetails(details);

                    Result<LevelSettingsConfirmation> result = new Result<LevelSettingsConfirmation>(confirmation);

                    if (!success) {
                        result.failed("Level settings not applied; the instance filter didn't match");
                    }

                    ChannelMessage reply = new ChannelMessage(t.getRespondToChannel(), result);
                    reply.setCorrelationID(t.getCorrelationID());

                    try {
                        socketClient.send(reply);
                    } catch (LoggingMessageSenderException e) {
                        logger.warning(e, "Failed to send level setting confirmation");
                    }

                }
            }
        });

        if (stackTraceModuleEnabled) {

            StackCaptureConfiguration configuration = new StackCaptureConfiguration();
            configuration.setSnapshotInterval(stackTraceModuleBroadcastInterval);
            configuration.setInstanceKey(instanceKey);

            // TODO : fill in other bits
            stackTraceModule.setChannelSubscriptions(socketClient);
            stackTraceModule.setLoggingMessageSender(socketClient);
            stackTraceModule.configure(configuration, null);
            stackTraceModule.start();
        }

        if (reportsModuleEnabled) {
            try {
                reportsHelper = new ReportsHelper(reportsConfigurationPath);
                initialiseReportsModule();
            }catch (JsonSyntaxException e){
                Out.err("[logginghub] Reports module was enabled, but the reports configuration file '{}' or '{}' failed to parse - please check your configuration", reportsConfigurationPath, new
                        File(
                        reportsConfigurationPath).getAbsolutePath());
                e.printStackTrace(System.err);
            }
            catch (RuntimeException e) {
                Out.err("[logginghub] Reports module was enabled, but the reports configuration file wasn't found '{}' or '{}' - please check your configuration", reportsConfigurationPath, new File(
                        reportsConfigurationPath).getAbsolutePath());
            }
        }

        socketClientManager = new SocketClientManager(socketClient);
        socketClientManager.start();
    }

    private void initialiseReportsModule() {

        socketClient.subscribe(Channels.reportExecuteRequests, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage channelMessage) {
                final SerialisableObject payload = channelMessage.getPayload();
                ReportExecuteRequest request = (ReportExecuteRequest) payload;

                logger.info("Received request to execute report '{}'", request.getReportName());
                Result<ReportExecuteResult> result = reportsHelper.execute(request.getReportName());

                ReportExecuteResponse response = new ReportExecuteResponse();
                response.setInstanceKey(instanceKey);
                response.setResult(result);

                ChannelMessage replyWrapper = new ChannelMessage();
                replyWrapper.setChannel(Channels.getPrivateConnectionChannel(request.getRespondToChannel()));
                replyWrapper.setPayload(response);

                try {
                    socketClient.send(replyWrapper);
                } catch (LoggingMessageSenderException e) {
                    logger.info(e, "Failed to send report reply");
                }
            }
        });

        socketClient.subscribe(Channels.reportListRequests, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage channelMessage) {
                final SerialisableObject payload = channelMessage.getPayload();
                ReportListRequest request = (ReportListRequest) payload;

                ReportListResponse response = reportsHelper.getReportList();
                response.setInstanceKey(instanceKey);

                // Need to send them back to the hub, but need to make sure they went back to the person that asked for them?
                ChannelMessage reply = new ChannelMessage(Channels.getPrivateConnectionChannel(request.getRespondToChannel()), response);

                try {
                    socketClient.send(reply);
                } catch (LoggingMessageSenderException e) {
                    logger.info(e, "Failed to send report reply");
                }
            }
        });

    }

    public synchronized void stop() {
        if (stackTraceModule != null) {
            stackTraceModule.stop();
        }

        if (socketClientManager != null) {
            socketClientManager.stop();
        }

    }

    public synchronized void close() {
        logger.debug("Closing appender helper...");
        this.closing = true;

        stop();

        if (cpuLogger != null) {
            cpuLogger.stop();
            logger.debug("Stopped CPU Logger");
        }

        if (heapLogger != null) {
            heapLogger.stop();
            logger.debug("Stopped Heap Logger");
        }

        if (socketClient != null) {
            socketClient.close();
            logger.debug("Closed socket client");
        }

        if (dispatcherThread != null) {
            dispatcherThread.stop();
            logger.debug("Stopped dispatcher thread");
        }

        if (gcFileWatcher != null) {
            gcFileWatcher.stop();
            logger.debug("Stopped gc watcher thread");
        }

        if (telemetryClient != null) {
            telemetryClient.stop();
        }

        logger.debug("Closed appender helper.");
    }

    public void setPublishingListener(PublishingListener publishingListener) {
        this.publishingListener = publishingListener;
    }

    public void flush() {
        if (socketClient != null) {
            socketClient.flush();
        }
    }

    public void waitUntilAllRecordsHaveBeenPublished() {
        boolean done = false;

        while (!done) {
            synchronized (eventsToBeDispatched) {
                done = eventsToBeDispatched.isEmpty();
            }

            if (!done) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public void replaceConnectionList(List<InetSocketAddress> parseAddressAndPortList) {
        socketClient.replaceConnectionList(parseAddressAndPortList);
    }

    public void send(LoggingMessage logEventMessage) throws LoggingMessageSenderException {
        socketClient.send(logEventMessage);
    }

    public void setWriteQueueOverflowPolicy(SlowSendingPolicy valueOf) {
        socketClient.setWriteQueueOverflowPolicy(valueOf);
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public void append(AppenderHelperEventConvertor appenderHelperEventConvertor) {
        if (useDispatchThread) {
            addToQueue(appenderHelperEventConvertor.createSnapshot());
        } else {
            appended++;
            LogEvent event = appenderHelperEventConvertor.createLogEvent();
            try {

                LogEventMessage message = new LogEventMessage(event);
                socketClient.send(message);
                if (publishingListener != null) {
                    publishingListener.onSuccessfullyPublished(event);
                }
            } catch (LoggingMessageSenderException e) {
                if (publishingListener != null) {
                    publishingListener.onUnsuccessfullyPublished(event, e);
                }

                boolean okToThrow = true;
                if (e.getCause() instanceof ConnectorException) {
                    if (isDontThrowExceptionsIfHubIsntUp()) {
                        // Fine, we have been told to supress these
                        okToThrow = false;
                    }
                }

                if (okToThrow) {
                    throw new RuntimeException("Failed to send log event", e);
                }
            }
        }

    }

    public BlockingDeque getEventsToBeDispatched() {
        return eventsToBeDispatched;
    }

    public boolean isGatheringCallerDetails() {
        return gatheringCallerDetails;
    }

    public void setGatheringCallerDetails(boolean gatheringCallerDetails) {
        this.gatheringCallerDetails = gatheringCallerDetails;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = StringUtils.environmentReplacement(channel);
    }

    public SocketClient getSocketClient() {
        return socketClient;
    }

    public void setSocketClient(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public String getStackTraceModuleBroadcastInterval() {
        return stackTraceModuleBroadcastInterval;
    }

    public void setStackTraceModuleBroadcastInterval(String stackTraceModuleBroadcastInterval) {
        this.stackTraceModuleBroadcastInterval = stackTraceModuleBroadcastInterval;
    }

    public boolean isStackTraceModuleEnabled() {
        return stackTraceModuleEnabled;
    }

    public void setStackTraceModuleEnabled(boolean stackTraceModuleEnabled) {
        this.stackTraceModuleEnabled = stackTraceModuleEnabled;
    }

    public StackCaptureModule getStackTraceModule() {
        return stackTraceModule;
    }

    public void setMaxWriteQueueSize(int maximumWriteQueueSize) {
        socketClient.setWriteQueueMaximumSize(maximumWriteQueueSize);
    }

    public int getAppended() {
        return appended;
    }

    public void setPublishHumanReadableTelemetry(boolean publishHumanReadableTelemetry) {
        this.publishHumanReadableTelemetry = publishHumanReadableTelemetry;
    }

    public void setEnvironment(String environment) {
        this.instanceKey.setEnvironment(StringUtils.environmentReplacement(environment));
    }

    public String getEnvironment() {
        return instanceKey.getEnvironment();
    }

    public void setInstanceIdentifier(String instanceIdentifier) {
        this.instanceKey.setInstanceIdentifier(StringUtils.environmentReplacement(instanceIdentifier));
    }

    public String getInstanceIdentifier() {
        return instanceKey.getInstanceIdentifier();
    }

    public void setSourceHostX(String hostAddressOverride) {
        instanceKey.setHost(StringUtils.environmentReplacement(hostAddressOverride));
    }

    public String getSourceHost() {
        return instanceKey.getHost();
    }

    public void setSourceAddressOverride(String address) {
        instanceKey.setAddress(StringUtils.environmentReplacement(address));
    }

    public String getSourceAddress() {
        return instanceKey.getAddress();
    }

    public void setReportsModuleEnabled(boolean reportsModuleEnabled) {
        this.reportsModuleEnabled = reportsModuleEnabled;
    }

    public boolean isReportsModuleEnabled() {
        return reportsModuleEnabled;
    }

    public void setReportsConfigurationPath(String reportsConfigurationPath) {
        this.reportsConfigurationPath = StringUtils.environmentReplacement(reportsConfigurationPath);
    }

    public String getReportsConfigurationPath() {
        return reportsConfigurationPath;
    }

    public InstanceKey getInstanceKey() {
        return instanceKey;
    }

    public void setInstanceType(String instanceType) {
        instanceKey.setInstanceType(StringUtils.environmentReplacement(instanceType));
    }
}
