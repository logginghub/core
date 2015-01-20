package com.logginghub.logging.generator.nextgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.modules.configuration.SimulatorConfiguration;
import com.logginghub.logging.telemetry.configuration.HubConfiguration;
import com.logginghub.logging.transaction.HubConnector;
import com.logginghub.logging.transaction.configuration.HubConnectorConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Is;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.RandomUtils;
import com.logginghub.utils.Source;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class Simulator implements Module<SimulatorConfiguration>, Source<LogEvent> {

    private AtomicLong nextTransactionID = new AtomicLong(RandomUtils.between(1000, 2000));
    private Destination<LogEvent> eventDestination;
    private SimulatorConfiguration configuration;

    private SimulatorEventSource source;

    private Multiplexer<LogEvent> multiplexer = new Multiplexer<LogEvent>();

    @SuppressWarnings("unchecked") public void configure(SimulatorConfiguration configuration, ServiceDiscovery serviceDiscovery) {
        this.configuration = configuration;

        eventDestination = serviceDiscovery.findService(Destination.class, LogEvent.class, configuration.getEventDestination());
        multiplexer.addDestination(eventDestination);
    }

    public void start() {
        Is.notNull(eventDestination, "eventDestination is null; have you called configure() ?");
        // final SocketClient client = new SocketClient("Generator");
        // client.addConnectionPoint(new InetSocketAddress("localhost",
        // VLPorts.getSocketHubDefaultPort()));
        // client.setAutoSubscribe(false);
        // client.setAutoGlobalSubscription(false);

        // SocketClientManager manager = new SocketClientManager(client);
        // manager.start();

        source = new SimulatorEventSource(false, 1, 20);
        source.setCount(RandomUtils.between(20000, 50000));

        final List<ProcessingUnit> startingUnits = new ArrayList<ProcessingUnit>();

        int hosts = 2;
        int instances = 4;

        for (int j = 0; j < instances; j++) {
            for (int i = 0; i < hosts; i++) {
                final List<ProcessingUnit> units = new ArrayList<ProcessingUnit>();
                String host = "ldn_pdn_" + (i + 1);
                units.add(createReceivingProcessor(host, "TradeReceiver-" + (j + 1)));
                units.add(createStoringProcessor(host, "TradeStorage-" + (j + 1)));
                units.add(createValidationProcessor(host, "TradeValidation-" + (j + 1)));
                units.add(createEnrichedProcessor(host, "TradeEnrichment-" + (j + 1)));
                units.add(createReportedProcessor(host, "TradeReporter-" + (j + 1)));

                wireUpToSocketClient(units);
                wireUpToEachOther(units);
                start(units);
                startingUnits.add(units.get(0));
            }
        }

        // Wire up the source to the start of the chain
        source.getEventStream().addListener(new StreamListener<Long>() {
            int counter = 0;

            public void onNewItem(Long t) {
                Map<String, String> context = new HashMap<String, String>();
                context.put("tradeID", t.toString());
                context.put("transactionID", Long.toString(nextTransactionID.getAndIncrement()));

                int index = counter++ % startingUnits.size();
                startingUnits.get(index).processNonBlocking(context);
            }
        });

        source.start();
    }

    private void start(List<ProcessingUnit> units) {
        for (ProcessingUnit processingUnit : units) {
            processingUnit.start();
        }
    }

    private void wireUpToEachOther(List<ProcessingUnit> units) {
        // Wire up all but the last unit to its neighbour
        for (int i = 0; i < units.size() - 1; i++) {
            units.get(i).addDownstream(units.get(i + 1));
        }
    }

    private void wireUpToSocketClient(List<ProcessingUnit> units) {
        for (ProcessingUnit processingUnit : units) {
            processingUnit.getLogEventStream().addListener((StreamListener<LogEvent>)multiplexer);
        }
    }

    public ProcessingUnit createReceivingProcessor(final String host, final String applicationName) {

        ProcessingUnit processingUnit = new ProcessingUnit() {
            @Override protected void buildEvent(LogEventBuilder builder, Map<String, String> context, Stopwatch sw) {
                builder.setMessage("Trade received successfully in {} ms - Trade ID '{}' and transaction ID '{}'",
                                   sw.getDurationMillis(),
                                   context.get("tradeID"),
                                   context.get("transactionID"))
                       .setSourceApplication(applicationName)
                       .setSourceHost(host);
            }
        };

        processingUnit.getConfiguration().name = applicationName;
        processingUnit.getConfiguration().threads = 10;
        processingUnit.getConfiguration().processingTimeRange.setRange(5, 10);

        return processingUnit;
    }

    public ProcessingUnit createStoringProcessor(final String host, final String applicationName) {
        ProcessingUnit processingUnit = new ProcessingUnit() {
            @Override protected void buildEvent(LogEventBuilder builder, Map<String, String> context, Stopwatch sw) {
                builder.setMessage("Trade stored successfully in {} ms - Trade ID '{}' and transaction ID '{}'",
                                   sw.getDurationMillis(),
                                   context.get("tradeID"),
                                   context.get("transactionID"))
                       .setSourceApplication(applicationName)
                       .setSourceHost(host);
            }
        };

        processingUnit.getConfiguration().name = "TradeStorage";
        processingUnit.getConfiguration().threads = 10;
        processingUnit.getConfiguration().processingTimeRange.setRange(5, 10);

        return processingUnit;
    }

    public ProcessingUnit createValidationProcessor(final String host, final String applicationName) {
        ProcessingUnit processingUnit = new ProcessingUnit() {
            @Override protected void buildEvent(LogEventBuilder builder, Map<String, String> context, Stopwatch sw) {
                builder.setMessage("Trade validated successfully in {} ms - Trade ID '{}' and transaction ID '{}'",
                                   sw.getDurationMillis(),
                                   context.get("tradeID"),
                                   context.get("transactionID"))
                       .setSourceApplication(applicationName)
                       .setSourceHost(host);
            }
        };

        processingUnit.getConfiguration().name = "TradeValidation";
        processingUnit.getConfiguration().threads = 10;
        processingUnit.getConfiguration().processingTimeRange.setRange(5, 100);

        return processingUnit;
    }

    public ProcessingUnit createEnrichedProcessor(final String host, final String applicationName) {
        ProcessingUnit processingUnit = new ProcessingUnit() {
            @Override protected long getProcessingTime(Map<String, String> context) {

                // Make every 20th trade take super long
                long processingTime = super.getProcessingTime(context);
                long tradeID = Long.parseLong(context.get("tradeID"));
                if (tradeID % 20 == 0) {
                    processingTime *= 1534.5;
                }

                return processingTime;
            }

            @Override protected void buildEvent(LogEventBuilder builder, Map<String, String> context, Stopwatch sw) {
                builder.setMessage("Trade enriched successfully - Trade ID '{}' and transaction ID '{}'",
                /* sw.getDurationMillis(), */
                context.get("tradeID"), context.get("transactionID")).setSourceApplication(applicationName).setSourceHost(host);
            }
        };

        processingUnit.getConfiguration().name = "TradeEnrichment";
        processingUnit.getConfiguration().threads = 10;
        processingUnit.getConfiguration().processingTimeRange.setRange(5, 10);

        return processingUnit;
    }

    public ProcessingUnit createReportedProcessor(final String host, final String applicationName) {
        ProcessingUnit processingUnit = new ProcessingUnit() {
            @Override protected void buildEvent(LogEventBuilder builder, Map<String, String> context, Stopwatch sw) {
                builder.setMessage("Trade reported to regulator successfully in {} ms - Trade ID '{}' and transaction ID '{}'",
                                   sw.getDurationMillis(),
                                   context.get("tradeID"),
                                   context.get("transactionID"))
                       .setSourceApplication(applicationName)
                       .setSourceHost(host);
            }
        };

        processingUnit.getConfiguration().name = "TradeReporting";
        processingUnit.getConfiguration().threads = 10;
        processingUnit.getConfiguration().processingTimeRange.setRange(5, 10);

        return processingUnit;
    }

    public void stop() {
        source.stop();
    }

    @Override public void addDestination(Destination<LogEvent> listener) {
        multiplexer.addDestination(listener);
    }

    @Override public void removeDestination(Destination<LogEvent> listener) {
        multiplexer.removeDestination(listener);
    }

    public static void main(String[] args) {
        Simulator generator = new Simulator();
        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        HubConnector connector = new HubConnector();
        HubConnectorConfiguration configuration = new HubConnectorConfiguration();
        configuration.getHubs().add(new HubConfiguration("localhost", 58770));
        connector.configure(configuration, discovery);
        discovery.bind(Destination.class, LogEvent.class, connector);
        generator.configure(new SimulatorConfiguration(), discovery);
        generator.start();
    }
}
