package com.logginghub.logging.launchers;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFormatter;
import com.logginghub.logging.VLLogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.hub.configuration.FileLogConfiguration;
import com.logginghub.logging.hub.configuration.LegacySocketHubConfiguration;
import com.logginghub.logging.hub.configuration.RollingFileLoggerConfiguration;
import com.logginghub.logging.hub.configuration.SocketHubConfiguration;
import com.logginghub.logging.hub.configuration.TimestampVariableRollingFileLoggerConfiguration;
import com.logginghub.logging.interfaces.FilteredMessageSender;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.InternalConnection;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.modules.RollingFileLogger;
import com.logginghub.logging.modules.TimestampFixedRollingFileLogger;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.logging.utils.AggregatedFileLogger;
import com.logginghub.utils.EnvironmentProperties;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.MainUtils;
import com.logginghub.utils.MemorySnapshot;
import com.logginghub.utils.ProcessUtils;
import com.logginghub.utils.ReflectionEnvironmentVariableReplacer;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.Throttler;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.logging.LoggerStream;
import com.logginghub.utils.logging.SystemErrStream;
import com.logginghub.utils.module.ProxyServiceDiscovery;

@Deprecated
public class LegacyRunHub implements Closeable {

    private static final Logger logger = Logger.getLoggerFor(LegacyRunHub.class);

    private SocketHub hub;
    private TimestampFixedRollingFileLogger timestampAggregatedFileLogger;
    private RollingFileLogger aggregatedFileLogger;

    private ThreadPoolExecutor timestampAggregatedFileLoggerPool;
    private ThreadPoolExecutor aggregatedFileLoggerPool;

    public static void main(String[] args) throws Exception {
        // Logger.setLevel(Logger.trace, ObjectDecoder.class,
        // ReflectionSerialiser.class);
        mainInternal(args);
    }

    public static LegacyRunHub mainInternal(String[] args) throws Exception {
        Logger.setLevelFromSystemProperty();
        SystemErrStream.gapThreshold = 1100;
        
        MemorySnapshot.runMonitor(95);

        String configurationFilename = MainUtils.getStringArgument(args, 0, "hub.xml");
        boolean configSearch = MainUtils.getBooleanArgument(args, 1, false);

        File configurationFile = new File(configurationFilename);
        if (configSearch) {
            logger.info("Original configuration is here '{}', but the config parent search feature has been turned on", configurationFilename);
            File parentConfig = searchParentFolders(configurationFile);
            if (parentConfig != null) {
                configurationFile = parentConfig;
            }
        }

        LegacySocketHubConfiguration configuration;

        if (configurationFile.exists()) {
            logger.info("Loading configuration from {}", configurationFile.getAbsolutePath());
            configuration = LegacySocketHubConfiguration.fromFile(configurationFile);
            return mainInternal(configuration);
        }
        else {
            logger.warning("Failed to find configuration file '{}'", configurationFile.getAbsolutePath());
            return null;
        }
    }

    private static File searchParentFolders(File configurationFile) {

        String filename = configurationFile.getName();
        File folder = configurationFile.getParentFile();

        try {
            folder = folder.getCanonicalFile();
        }
        catch (IOException e) {
            // Probably means this is a dodgy folder
            logger.warning(e,
                           "Failed to turn the configuration path provided ({}) into a real folder, can you double check its a legitimate folder?",
                           configurationFile.getAbsolutePath());
        }

        File found = configurationFile;
        if (folder != null) {
            File parent = folder.getParentFile();
            found = recursiveSearch(filename, parent);
        }

        return found;

    }

    private static File recursiveSearch(String filename, File parent) {
        logger.debug("Checking folder '{}' for a config file called '{}'", parent.getAbsolutePath(), filename);
        File found;
        File attempt = new File(parent, filename);
        if (attempt.exists()) {
            found = attempt;
            logger.debug("A parent configuration has been found here '{}'", found.getAbsolutePath());
        }
        else {

            File grandParent = parent.getParentFile();
            if (grandParent != null) {
                found = recursiveSearch(filename, grandParent);
            }
            else {
                found = null;
            }
        }

        return found;

    }

    public static LegacyRunHub mainInternal(LegacySocketHubConfiguration configuration) throws Exception {
        if (System.getProperty("java.util.logging.config.file") == null) {
            LogManager.getLogManager().readConfiguration(ResourceUtils.openStream("configs/juli/hub.logging.properties"));
        }

        ReflectionEnvironmentVariableReplacer.doReplacements(configuration);

        final SocketHub hub = new SocketHub();
        SocketHubConfiguration newConfiguration = new SocketHubConfiguration();
        newConfiguration.setMaximumClientSendQueueSize(configuration.getMaximumClientSendQueueSize());
        newConfiguration.setOutputStats(configuration.isOutputStats());
//        newConfiguration.setOutputTelemetryToLoggingStream(configuration.isOutputTelemetryToLoggingStream());
        newConfiguration.setPort(configuration.getPort());
        hub.configure(newConfiguration, null);

        // TODO : refactor all this stuff either to be independent modules or be configured inside
        // the SocketHub
//        List<SocketTextReaderConfiguration> socketTextReaders = configuration.getSocketTextReaders();
//        for (SocketTextReaderConfiguration socketTestReaderConfiguration : socketTextReaders) {
//            SocketTextReaderModule reader = new SocketTextReaderModule(hub, socketTestReaderConfiguration.getPort());
//            reader.setEndString(socketTestReaderConfiguration.getMessageEnd());
//            reader.setStartString(socketTestReaderConfiguration.getMessageStart());
//            reader.setLevel(Level.parse(socketTestReaderConfiguration.getLevel()).intValue());
//            logger.info(String.format("Starting socket text reader on port %d with format '%s{}%s'",
//                                      reader.getPort(),
//                                      reader.getStartString(),
//                                      reader.getEndString()));
//            reader.start();
//        }

        if (EnvironmentProperties.getBoolean("loggingHub.enableInternalLogging", false)) {
            setupInternalLogging(hub);
        }

//        TransactionMonitorConfiguration stateEngineConfiguration = configuration.getStateEngineConfiguration();
//        if (stateEngineConfiguration != null) {
//            StateEngineModel model = new StateEngineModel();
//            TransactionMonitor stateEngineController = new TransactionMonitor(model);
//            stateEngineController.configure(stateEngineConfiguration, null);
//            stateEngineController.start();
//        }

        LegacyRunHub wrapper = new LegacyRunHub();
        wrapper.hub = hub;

//        wrapper.setupBridges(configuration, hub);
        wrapper.setupAggregatedFileLogger(configuration, hub);
        wrapper.setupTimestampAggregatedFileLogger(configuration, hub);

        hub.start();

        return wrapper;
    }

    private static void setupInternalLogging(final SocketHub hub) {

        final String sourceApplication = System.getProperty("vllogging.sourceApplication", "LoggingHub");

        InetAddress localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }

        final int finalPid = ProcessUtils.getPid();
        final InetAddress finalHost = localHost;

        Logger root = com.logginghub.utils.logging.Logger.root();

        final InternalConnection internal = new InternalConnection("LegacyHubInternalLogging", SocketConnection.CONNECTION_TYPE_NORMAL);

        root.addStream(new LoggerStream() {
            @Override public void onNewLogEvent(com.logginghub.utils.logging.LogEvent event) {
                VLLogEvent vlevent = new VLLogEvent(event, finalPid, sourceApplication, finalHost);
                vlevent.setChannel("private/hubinternal");
                hub.processLogEvent(new LogEventMessage(vlevent), internal);
            }
        });
    }

//    private static void setupInternalTelemetry(final SocketHub hub) {
//        KryoTelemetryClient telemetryClient = new KryoTelemetryClient(new TelemetryInterface() {
//            @Override public void publishTelemetry(DataStructure dataStructure) {
//                Messaging3TelemetryHub messaging3TelemetryHub = hub.getMessaging3TelemetryHub();
//                if (messaging3TelemetryHub != null) {
//                    messaging3TelemetryHub.broadcastInternal("telemetry2", dataStructure);
//                }
//            }
//        });
//        telemetryClient.startMachineTelemetryGenerator();
//        telemetryClient.startProcessTelemetryGenerator(System.getProperty("applicationName", "hub"));
//    }

//    private void setupBridges(LegacySocketHubConfiguration configuration, final SocketHub hub) {
//        List<LoggingBridgeConfiguration> exportBridges = configuration.getExportBridges();
//        for (LoggingBridgeConfiguration bridgeConfiguration : exportBridges) {
//            InetSocketAddress connectionPoint = new InetSocketAddress(bridgeConfiguration.getHost(), bridgeConfiguration.getPort());
//            LoggingBridgeModule bridge = new LoggingBridgeModule(connectionPoint);
//            bridge.startExport(hub);
//        }
//
//        List<LoggingBridgeConfiguration> importBridges = configuration.getImportBridges();
//        for (LoggingBridgeConfiguration bridgeConfiguration : importBridges) {
//            InetSocketAddress connectionPoint = new InetSocketAddress(bridgeConfiguration.getHost(), bridgeConfiguration.getPort());
//            LoggingBridgeModule bridge = new LoggingBridgeModule(connectionPoint);
//            bridge.startImport(new LogEventListener() {
//                @Override public void onNewLogEvent(LogEvent event) {
//                    hub.processInternalLogEvent(event);
//                }
//            });
//        }
//    }

    private void setupTimestampAggregatedFileLogger(final LegacySocketHubConfiguration configuration, SocketHub hub) {
        final TimestampVariableRollingFileLoggerConfiguration logConfiguration = configuration.getTimeStampAggregatedFileLogConfiguration();
        if (logConfiguration != null) {
            // timestampAggregatedFileLogger =
            // TimestampFixedRollingFileLogger.fromConfiguration(logConfiguration);
            timestampAggregatedFileLogger = new TimestampFixedRollingFileLogger();
//            ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
//            discovery.bind(Source.class, LogEvent.class, hub);
            timestampAggregatedFileLogger.configure(logConfiguration, new ProxyServiceDiscovery());
            timestampAggregatedFileLoggerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
            setupLocalLogAggregator(logConfiguration, timestampAggregatedFileLogger, timestampAggregatedFileLoggerPool);
        }
    }

    private void setupAggregatedFileLogger(final LegacySocketHubConfiguration configuration, SocketHub hub) {
        final RollingFileLoggerConfiguration logConfiguration = configuration.getAggregatedFileLogConfiguration();
        if (logConfiguration != null) {
            // aggregatedFileLogger = RollingFileLogger.fromConfiguration(logConfiguration);

            aggregatedFileLogger = new RollingFileLogger();
//            ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
//            discovery.bind(Source.class, LogEvent.class, hub);
            aggregatedFileLogger.configure(logConfiguration, new ProxyServiceDiscovery());

            aggregatedFileLoggerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
            setupLocalLogAggregator(logConfiguration, aggregatedFileLogger, aggregatedFileLoggerPool);
        }
    }

    public void setupLocalLogAggregator(final FileLogConfiguration logConfiguration,
                                        final AggregatedFileLogger aggregatedFileLogger,
                                        final ThreadPoolExecutor pool) {

        aggregatedFileLogger.setForceFlush(logConfiguration.getForceFlush());
        setupFormatter(logConfiguration, aggregatedFileLogger);

        if (logConfiguration.getWriteAsynchronously()) {

            final BlockingQueue<Runnable> queue = pool.getQueue();
            hub.addAndSubscribeLocalListener(new FilteredMessageSender() {
                Throttler throttler = new Throttler(10, TimeUnit.SECONDS);
                Throttler discardThrottler = new Throttler(10, TimeUnit.SECONDS);
                Throttler diskSpaceThrottler = new Throttler(10, TimeUnit.SECONDS);

                public void send(final LoggingMessage message) throws LoggingMessageSenderException {

                    if (queue.size() < logConfiguration.getAsynchronousQueueDiscardSize()) {
                        pool.execute(new Runnable() {
                            public void run() {
                                try {
                                    aggregatedFileLogger.send(message);
                                }
                                catch (LoggingMessageSenderException e) {
                                    if (e.getCause() != null &&
                                        e.getCause() instanceof IOException &&
                                        e.getCause().getMessage().contains("There is not enough space on the disk")) {
                                        if (diskSpaceThrottler.isOkToFire()) {
                                            logger.warn("Failed to write log event to the timestamp aggregated log file : you've run out of disk space (this message will repeat every 10 seconds until the problem is resolved)");
                                        }
                                    }
                                    else {
                                        logger.warn(e, "Failed to write log event to the timestamp aggregated log file : {}", e.getMessage());
                                    }
                                }
                            }
                        });
                    }
                    else {
                        if (discardThrottler.isOkToFire()) {
                            logger.warn("The asynchronous queue writing to the aggregated log file has reached {} elements; we are discarding all new entries until the issue is resolved so the queue doesn't kill the hub",
                                        queue.size());
                        }
                    }

                    if (queue.size() > logConfiguration.getAsynchronousQueueWarningSize() && throttler.isOkToFire()) {
                        logger.warn("The asynchronous queue writing to the timestamp aggregated log file has reached " +
                                    queue.size() +
                                    " elements; it doesn't look like your IO subsystem is keeping up");
                    }
                }

                @Override public int getLevelFilter() {
                    return Level.ALL.intValue();
                }

                @Override
                public int getConnectionType() {
                    return 0;
                }

                @Override public void send(LogEvent t) {
                    try {
                        send(new LogEventMessage(t));
                    }
                    catch (LoggingMessageSenderException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else {
            hub.addAndSubscribeLocalListener(aggregatedFileLogger);
        }

        hub.addCloseable(aggregatedFileLogger);
    }

    /**
     * @deprecated This should be configured using the module configuration approach
     * @param logConfiguration
     * @param aggregatedFileLogger
     */
    private void setupFormatter(FileLogConfiguration logConfiguration, AggregatedFileLogger aggregatedFileLogger) {
        if (logConfiguration.getFormatter() != null && logConfiguration.getFormatter().length() > 0) {
            LogEventFormatter formatter = ReflectionUtils.instantiate(logConfiguration.getFormatter());
            ReflectionUtils.invokeIfMethodExists("setPattern", formatter, logConfiguration.getPattern());
            aggregatedFileLogger.setFormatter(formatter);
        }

        aggregatedFileLogger.setAutoNewline(logConfiguration.getAutoNewline());
    }

    public ThreadPoolExecutor getAggregatedFileLoggerPool() {
        return aggregatedFileLoggerPool;
    }

    public ThreadPoolExecutor getTimestampAggregatedFileLoggerPool() {
        return timestampAggregatedFileLoggerPool;
    }

    public RollingFileLogger getAggregatedFileLogger() {
        return aggregatedFileLogger;
    }

    public SocketHub getHub() {
        return hub;
    }

    public TimestampFixedRollingFileLogger getTimestampAggregatedFileLogger() {
        return timestampAggregatedFileLogger;
    }

    public void flush() {
        if (aggregatedFileLoggerPool != null) {
            flushPool(aggregatedFileLoggerPool);
        }

        if (timestampAggregatedFileLoggerPool != null) {
            flushPool(timestampAggregatedFileLoggerPool);
        }
    }

    private void flushPool(ThreadPoolExecutor pool) {
        final BlockingQueue<Runnable> queue = pool.getQueue();
        ThreadUtils.untilTrue(10, TimeUnit.SECONDS, new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return queue.isEmpty();
            }
        });
    }

    public void waitUntilBound() {
        hub.waitUntilBound();
    }

    @Override public void close() throws IOException {
        logger.info("Closing logging hub...");
        FileUtils.closeQuietly(timestampAggregatedFileLogger, aggregatedFileLogger, hub);
    }
}
