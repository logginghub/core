package com.logginghub.integrationtests.logging;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.hub.configuration.SocketHubConfiguration;
import com.logginghub.logging.hub.configuration.TimestampVariableRollingFileLoggerConfiguration;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.modules.*;
import com.logginghub.logging.modules.configuration.*;
import com.logginghub.logging.servers.ServerSubscriptionsService;
import com.logginghub.logging.servers.SocketHub;
import com.logginghub.logging.servers.SocketHubInterface;
import com.logginghub.utils.*;
import com.logginghub.utils.ExceptionPolicy.Policy;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;
import com.logginghub.utils.module.Module;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class HubTestFixture {

    public enum Features {
        SocketTextReader,
        Patterniser,
        PatternManager,
        Aggregator,
        RestfulListener,
        ChannelSubscriptions,
        TelemetryOutput,
        Bridge,
        BinaryWriter,
        BinaryProcessor,
        DiskHistory,
        PatternDiskHistory,
        AggregatedDiskHistory,
        TimestampVariableRollingFileLogger
    }

    private HubFixture hubFixtureA = new HubFixture(EnumSet.allOf(Features.class));
    private SocketHub socketHubA;

    // private SocketClientManager clientManagerA;
    // private SocketClientManager clientManagerB;
    // private SocketClientManager clientManagerC;
    private SocketHub socketHubB;
    private List<SocketClient> clients = new ArrayList<SocketClient>();
    private List<HubFixture> hubs = new ArrayList<HubFixture>();

    public SocketTextReaderConfiguration getSocketTextReaderConfiguration() {
        return hubFixtureA.socketTextReaderConfiguration;
    }

    public SocketTextReaderModule getSocketTextReaderModule() {
        return hubFixtureA.socketTextReaderModule;
    }

    public ChannelSubscriptionsModule getChannelSubscriptionsModuleA() {
        return hubFixtureA.channelSubscriptionsModule;
    }

    public PatterniserConfiguration getPatterniserConfiguration() {
        return hubFixtureA.patterniserConfiguration;
    }

    public AggregatorConfiguration getAggregatorConfiguration() {
        return hubFixtureA.aggregatorConfiguration;
    }

    public SocketHubConfiguration getSocketHubConfiguration() {
        return hubFixtureA.socketHubConfiguration;
    }

    // public ChannelSubscriptionsModule getChannelSubscriptionsModuleB() {
    // return channelSubscriptionsModuleB;
    // }

    public HubFixture createSocketHub() {
        return createSocketHub(EnumSet.noneOf(Features.class));
    }

    public HubFixture createSocketHub(Features... features) {
        EnumSet<Features> featureSet = EnumSet.noneOf(Features.class);
        for (Features feature : features) {
            featureSet.add(feature);
        }

        HubFixture hubFixture = new HubFixture(featureSet);
        hubs.add(hubFixture);
        return hubFixture;
    }

    public HubFixture createSocketHub(EnumSet<Features> features) {
        HubFixture hubFixture = new HubFixture(features);
        hubs.add(hubFixture);
        return hubFixture;
    }

    public SocketHub getSocketHubA() {
        socketHubA = setupHub(socketHubA);
        return socketHubA;
    }

    public SocketHub getSocketHubB() {
        socketHubB = setupHub(socketHubB);
        return socketHubB;
    }

    public SocketClient createClientAutoSubscribe(String name, SocketHub hub) throws ConnectorException {
        SocketClient client = new SocketClient(name);
        client.addConnectionPoint(new InetSocketAddress(hub.getPort()));
        client.setAutoSubscribe(true);
        client.connect();
        clients.add(client);
        return client;
    }

    public SocketClient createClient(String name, SocketHub hub) throws ConnectorException {
        SocketClient client = new SocketClient(name);
        client.addConnectionPoint(new InetSocketAddress(hub.getPort()));
        client.connect();
        clients.add(client);
        return client;
    }

    public SocketClient createDisconnectedClient(String name, SocketHub hub) throws ConnectorException {
        SocketClient client = new SocketClient(name);
        client.addConnectionPoint(new InetSocketAddress(hub.getPort()));
        clients.add(client);
        return client;
    }

    @Deprecated
    /**
     * @deprecated Use the features enum approach now
     * @param hub
     * @return
     */ private SocketHub setupHub(SocketHub hub) {
        if (hub == null) {
            hub = new SocketHub();
            hub.configure(hubFixtureA.socketHubConfiguration, null);
            hub.useRandomPort();

            ConfigurableServiceDiscovery disco = new ConfigurableServiceDiscovery();

            disco.setExceptionPolicy(new ExceptionPolicy(Policy.RethrowOnAny));

            disco.bind(SocketHubInterface.class, hub);
            disco.bind(Destination.class, LogEvent.class, hub);
            disco.bind(Source.class, LogEvent.class, hub);
            disco.bind(LoggingMessageSender.class, hub);

            hubFixtureA.channelSubscriptionsModule = new ChannelSubscriptionsModule();
            hubFixtureA.channelSubscriptionsModule.configure(new ChannelSubscriptionsConfiguration(), disco);
            hubFixtureA.channelSubscriptionsModule.start();

            disco.bind(ChannelMessagingService.class, hubFixtureA.channelSubscriptionsModule);
            disco.bind(ServerSubscriptionsService.class, hubFixtureA.channelSubscriptionsModule);

            hubFixtureA.telemetryOutputConfiguration = new TelemetryOutputConfiguration();
            hubFixtureA.telemetryOutputModule = new TelemetryOutputModule();
            hubFixtureA.telemetryOutputModule.configure(hubFixtureA.telemetryOutputConfiguration, disco);
            hubFixtureA.telemetryOutputModule.start();

            hubFixtureA.patternManagerModule = new PatternManagerModule();
            hubFixtureA.patternManagerModule.configure(hubFixtureA.patternManagerConfiguration, disco);
            disco.bind(PatternManagerService.class, hubFixtureA.patternManagerModule);

            hubFixtureA.patterniserModule = new PatterniserModule();
            hubFixtureA.patterniserModule.configure(hubFixtureA.patterniserConfiguration, disco);

            disco.bind(Source.class, PatternisedLogEvent.class, hubFixtureA.patterniserModule);

            hubFixtureA.aggregatorModule = new AggregatorModule();
            hubFixtureA.aggregatorModule.configure(hubFixtureA.aggregatorConfiguration, disco);

            hubFixtureA.restfulListenerConfiguration.setPort(NetUtils.findFreePort());
            hubFixtureA.restfulListenerModule = new RestfulListenerModule();
            hubFixtureA.restfulListenerModule.configure(hubFixtureA.restfulListenerConfiguration, disco);

            hubFixtureA.socketTextReaderConfiguration.setPort(NetUtils.findFreePort());
            hubFixtureA.socketTextReaderModule = new SocketTextReaderModule();
            hubFixtureA.socketTextReaderModule.configure(hubFixtureA.socketTextReaderConfiguration, disco);

            hub.start();
            hubFixtureA.channelSubscriptionsModule.start();
            hubFixtureA.telemetryOutputModule.start();
            hubFixtureA.patterniserModule.start();
            hubFixtureA.aggregatorModule.start();
            hubFixtureA.restfulListenerModule.start();
            hubFixtureA.socketTextReaderModule.start();
        }

        return hub;
    }

    public void stop() throws IOException {
        if (socketHubA != null) {
            socketHubA.close();
        }

        if (socketHubB != null) {
            socketHubB.close();
        }

        for (HubFixture hubFixture : hubs) {
            hubFixture.stop();
        }

        // if (clientManagerA != null) {
        // clientManagerA.stop();
        // }
        //
        // if (clientManagerB != null) {
        // clientManagerB.stop();
        // }
        //
        // if (clientManagerC != null) {
        // clientManagerC.stop();
        // }

        for (SocketClient socketClient : clients) {
            socketClient.close();
        }
        clients.clear();

        if (hubFixtureA.aggregatorModule != null) {
            hubFixtureA.aggregatorModule.stop();
        }

        if (hubFixtureA.patterniserModule != null) {
            hubFixtureA.patterniserModule.stop();
        }

        if (hubFixtureA.restfulListenerModule != null) {
            hubFixtureA.restfulListenerModule.stop();
        }

        if (hubFixtureA.socketTextReaderModule != null) {
            hubFixtureA.socketTextReaderModule.stop();
        }
    }

    public Bucket<LoggingMessage> createMessageBucketFor(SocketClient client) {
        final Bucket<LoggingMessage> bucket = new Bucket<LoggingMessage>();
        client.addLoggingMessageListener(new LoggingMessageListener() {
            @Override
            public void onNewLoggingMessage(LoggingMessage message) {
                bucket.add(message);
            }
        });
        return bucket;
    }

    public Bucket<LogEvent> createEventBucketFor(SocketClient client) {
        final Bucket<LogEvent> bucket = new Bucket<LogEvent>();
        client.addLogEventListener(new LogEventListener() {
            @Override
            public void onNewLogEvent(LogEvent event) {
                bucket.add(event);
            }
        });
        return bucket;
    }

    public Bucket<ChannelMessage> getChannelBucketFor(String channel, SocketClient client) {
        final Bucket<ChannelMessage> bucket = new Bucket<ChannelMessage>();
        client.subscribe(channel, bucket);
        return bucket;
    }

    public void debugChannelOutput(final SocketClient client) {
        client.addSubscription("", new Destination<ChannelMessage>() {
            @Override
            public void send(ChannelMessage t) {
                Out.out("{} | (debug channel) {}", client.getName(), t);
            }
        });
    }

    public RestfulListenerConfiguration getRestfulListenerConfiguration() {
        return hubFixtureA.restfulListenerConfiguration;
    }

    public RestfulListenerModule getRestfulListenerModule() {
        return hubFixtureA.restfulListenerModule;
    }

    public void sendEvent(SocketClient client, String message) throws LoggingMessageSenderException {
        DefaultLogEvent defaultLogEvent = LogEventBuilder.start().setMessage(message).toLogEvent();
        LogEventMessage logEventMessage = new LogEventMessage(defaultLogEvent);
        client.send(logEventMessage);
    }

    public static class HubFixture {
        private SocketHubConfiguration socketHubConfiguration = new SocketHubConfiguration();

        private SocketTextReaderConfiguration socketTextReaderConfiguration = new SocketTextReaderConfiguration();
        private PatterniserConfiguration patterniserConfiguration = new PatterniserConfiguration();
        private AggregatorConfiguration aggregatorConfiguration = new AggregatorConfiguration();
        private RestfulListenerConfiguration restfulListenerConfiguration = new RestfulListenerConfiguration();
        private TelemetryOutputConfiguration telemetryOutputConfiguration = new TelemetryOutputConfiguration();
        private LoggingBridgeConfiguration loggingBridgeConfiguration = new LoggingBridgeConfiguration();
        private BinaryWriterConfiguration binaryWriterConfiguration = new BinaryWriterConfiguration();
        private BinaryProcessorConfiguration binaryProcessorConfiguration = new BinaryProcessorConfiguration();
        private DiskHistoryConfiguration diskHistoryConfiguration = new DiskHistoryConfiguration();
        private PatternManagerConfiguration patternManagerConfiguration = new PatternManagerConfiguration();
        private PatternisedDiskHistoryConfiguration patternisedDiskHistoryConfiguration = new PatternisedDiskHistoryConfiguration();
        private AggregatedDiskHistoryConfiguration aggregatedDiskHistoryConfiguration = new AggregatedDiskHistoryConfiguration();
        private TimestampVariableRollingFileLoggerConfiguration timestampVariableRollingFileLoggerConfiguration = new TimestampVariableRollingFileLoggerConfiguration();

        private ChannelSubscriptionsModule channelSubscriptionsModule;
        private TelemetryOutputModule telemetryOutputModule;
        private PatterniserModule patterniserModule;
        private AggregatorModule aggregatorModule;
        private RestfulListenerModule restfulListenerModule;
        private SocketTextReaderModule socketTextReaderModule;
        private LoggingBridgeModule bridgeModule;
        private BinaryWriterModule binaryWriterModule;
        private BinaryProcessorModule binaryProcessorModule;
        private DiskHistoryModule diskHistoryModule;
        private PatternManagerModule patternManagerModule;
        private AggregatedDiskHistoryModule aggregatedDiskHistoryModule;
        private PatternisedDiskHistoryModule patternisedDiskHistoryModule;
        private TimestampVariableRollingFileLogger timestampVariableRollingFileLoggerModule;

        private SocketHub hub;

        private EnumSet<Features> features;

        private List<Module<?>> modules;

        public HubFixture(EnumSet<Features> features) {
            this.features = features;
            socketHubConfiguration.setPort(NetUtils.findFreePort());
            loggingBridgeConfiguration.setHost("localhost");
        }

        public SocketHub start() {
            hub = new SocketHub();
            hub.configure(socketHubConfiguration, null);
            // hub.useRandomPort();

            ConfigurableServiceDiscovery disco = new ConfigurableServiceDiscovery();

            disco.setExceptionPolicy(new ExceptionPolicy(Policy.RethrowOnAny));

            disco.bind(SocketHubInterface.class, hub);
            disco.bind(Destination.class, LogEvent.class, hub);
            disco.bind(Source.class, LogEvent.class, hub);
            disco.bind(LoggingMessageSender.class, hub);

            modules = new ArrayList<Module<?>>();
            modules.add(hub);

            if (features.contains(Features.ChannelSubscriptions)) {
                channelSubscriptionsModule = new ChannelSubscriptionsModule();
                channelSubscriptionsModule.configure(new ChannelSubscriptionsConfiguration(), disco);
                channelSubscriptionsModule.start();
                modules.add(channelSubscriptionsModule);

                disco.bind(ChannelMessagingService.class, channelSubscriptionsModule);
                disco.bind(ServerSubscriptionsService.class, channelSubscriptionsModule);
                disco.bind(Destination.class, ChannelMessage.class, channelSubscriptionsModule);
            }

            if (features.contains(Features.TelemetryOutput)) {
                telemetryOutputConfiguration = new TelemetryOutputConfiguration();
                telemetryOutputModule = new TelemetryOutputModule();
                telemetryOutputModule.configure(telemetryOutputConfiguration, disco);
                modules.add(telemetryOutputModule);
            }

            if (features.contains(Features.PatternManager)) {
                File file = FileUtils.createRandomTestFileForClass(getClass());
                patternManagerConfiguration.setDataFile(file.getAbsolutePath());
                patternManagerModule = new PatternManagerModule();
                patternManagerModule.configure(patternManagerConfiguration, disco);

                disco.bind(PatternManagerService.class, patternManagerModule);

                modules.add(patternManagerModule);
            }

            if (features.contains(Features.Patterniser)) {
                patterniserModule = new PatterniserModule();
                patterniserModule.configure(patterniserConfiguration, disco);

                disco.bind(Source.class, PatternisedLogEvent.class, patterniserModule);

                modules.add(patterniserModule);
            }

            if (features.contains(Features.Aggregator)) {
                aggregatorModule = new AggregatorModule();
                aggregatorModule.configure(aggregatorConfiguration, disco);

                disco.bind(Source.class, AggregatedLogEvent.class, aggregatorModule);

                modules.add(aggregatorModule);
            }

            if (features.contains(Features.RestfulListener)) {
                restfulListenerConfiguration.setPort(NetUtils.findFreePort());
                restfulListenerModule = new RestfulListenerModule();
                restfulListenerModule.configure(restfulListenerConfiguration, disco);
                modules.add(restfulListenerModule);
            }

            if (features.contains(Features.SocketTextReader)) {
                socketTextReaderConfiguration.setPort(NetUtils.findFreePort());
                socketTextReaderModule = new SocketTextReaderModule();
                socketTextReaderModule.configure(socketTextReaderConfiguration, disco);
                modules.add(socketTextReaderModule);
            }

            if (features.contains(Features.Bridge)) {
                bridgeModule = new LoggingBridgeModule();
                bridgeModule.configure(loggingBridgeConfiguration, disco);
                modules.add(bridgeModule);
            }

            if (features.contains(Features.BinaryWriter)) {
                binaryWriterModule = new BinaryWriterModule();
                binaryWriterModule.configure(binaryWriterConfiguration, disco);
                modules.add(binaryWriterModule);
            }

            if (features.contains(Features.BinaryProcessor)) {
                binaryProcessorModule = new BinaryProcessorModule();
                binaryProcessorModule.configure(binaryProcessorConfiguration, disco);
                modules.add(binaryProcessorModule);
            }

            File folder = FileUtils.createRandomTestFolderForClass(getClass());

            diskHistoryConfiguration.setFolder(folder.getAbsolutePath());
            patternisedDiskHistoryConfiguration.setFolder(folder.getAbsolutePath());
            aggregatedDiskHistoryConfiguration.setFolder(folder.getAbsolutePath());

            if (features.contains(Features.DiskHistory)) {
                diskHistoryModule = new DiskHistoryModule();
                diskHistoryModule.configure(diskHistoryConfiguration, disco);
                modules.add(diskHistoryModule);
            }

            if (features.contains(Features.PatternDiskHistory)) {
                patternisedDiskHistoryModule = new PatternisedDiskHistoryModule();
                patternisedDiskHistoryModule.configure(patternisedDiskHistoryConfiguration, disco);
                modules.add(patternisedDiskHistoryModule);
            }

            if (features.contains(Features.AggregatedDiskHistory)) {
                aggregatedDiskHistoryModule = new AggregatedDiskHistoryModule();
                aggregatedDiskHistoryModule.configure(aggregatedDiskHistoryConfiguration, disco);
                modules.add(aggregatedDiskHistoryModule);
            }

            if (features.contains(Features.TimestampVariableRollingFileLogger)) {
                timestampVariableRollingFileLoggerModule = new TimestampVariableRollingFileLogger();
                timestampVariableRollingFileLoggerModule.configure(timestampVariableRollingFileLoggerConfiguration, disco);
                modules.add(timestampVariableRollingFileLoggerModule);
            }

            for (Module<?> module : modules) {
                module.start();
            }

            hub.waitUntilBound();
            return hub;
        }

        public void stop() {
            for (Module<?> module : modules) {
                module.stop();
            }
        }

        public SocketHubConfiguration getSocketHubConfiguration() {
            return socketHubConfiguration;
        }

        public SocketTextReaderConfiguration getSocketTextReaderConfiguration() {
            return socketTextReaderConfiguration;
        }

        public BinaryProcessorConfiguration getBinaryProcessorConfiguration() {
            return binaryProcessorConfiguration;
        }

        public BinaryProcessorModule getBinaryProcessorModule() {
            return binaryProcessorModule;
        }

        public PatterniserConfiguration getPatterniserConfiguration() {
            return patterniserConfiguration;
        }

        public AggregatorConfiguration getAggregatorConfiguration() {
            return aggregatorConfiguration;
        }

        public DiskHistoryConfiguration getDiskHistoryConfiguration() {
            return diskHistoryConfiguration;
        }

        public AggregatedDiskHistoryConfiguration getAggregatedDiskHistoryConfiguration() {
            return aggregatedDiskHistoryConfiguration;
        }

        public PatternisedDiskHistoryConfiguration getPatternisedDiskHistoryConfiguration() {
            return patternisedDiskHistoryConfiguration;
        }

        public TimestampVariableRollingFileLoggerConfiguration getTimestampVariableRollingFileLoggerConfiguration() {
            return timestampVariableRollingFileLoggerConfiguration;
        }

        public AggregatedDiskHistoryModule getAggregatedDiskHistoryModule() {
            return aggregatedDiskHistoryModule;
        }

        public PatternisedDiskHistoryModule getPatternisedDiskHistoryModule() {
            return patternisedDiskHistoryModule;
        }

        public PatternManagerConfiguration getPatternManagerConfiguration() {
            return patternManagerConfiguration;
        }

        public PatternManagerModule getPatternManagerModule() {
            return patternManagerModule;
        }

        public DiskHistoryModule getDiskHistoryModule() {
            return diskHistoryModule;
        }

        public RestfulListenerConfiguration getRestfulListenerConfiguration() {
            return restfulListenerConfiguration;
        }

        public TelemetryOutputConfiguration getTelemetryOutputConfiguration() {
            return telemetryOutputConfiguration;
        }

        public LoggingBridgeConfiguration getLoggingBridgeConfiguration() {
            return loggingBridgeConfiguration;
        }

        public ChannelSubscriptionsModule getChannelSubscriptionsModule() {
            return channelSubscriptionsModule;
        }

        public TelemetryOutputModule getTelemetryOutputModule() {
            return telemetryOutputModule;
        }

        public PatterniserModule getPatterniserModule() {
            return patterniserModule;
        }

        public AggregatorModule getAggregatorModule() {
            return aggregatorModule;
        }

        public RestfulListenerModule getRestfulListenerModule() {
            return restfulListenerModule;
        }

        public SocketTextReaderModule getSocketTextReaderModule() {
            return socketTextReaderModule;
        }

        public LoggingBridgeModule getBridgeModule() {
            return bridgeModule;
        }

        public SocketHub getHub() {
            return hub;
        }

        public EnumSet<Features> getFeatures() {
            return features;
        }

        public List<Module<?>> getModules() {
            return modules;
        }

        public BinaryWriterConfiguration getBinaryWriterConfiguration() {
            return binaryWriterConfiguration;
        }

        public BinaryWriterModule getBinaryWriterModule() {
            return binaryWriterModule;
        }

    }


}
