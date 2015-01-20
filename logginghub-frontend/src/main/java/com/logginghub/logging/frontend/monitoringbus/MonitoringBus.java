package com.logginghub.logging.frontend.monitoringbus;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.logginghub.logging.DemoLogEventProducer;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.Closer;
import com.logginghub.utils.ExceptionHandler;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.IntegerStat;
import com.logginghub.utils.StatBundle;
import com.logginghub.utils.Stream;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.Xml;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;

// TODO : move to the client jar, this is useful
public class MonitoringBus {

    private static Logger logger = Logger.getLoggerFor(MonitoringBus.class);

    private final Stream<LogEvent> eventStream = new Stream<LogEvent>();
    private final Stream<PatternisedLogEvent> patternisedStream = new Stream<PatternisedLogEvent>();
    private final Stream<DataStructure> telemetryStream = new Stream<DataStructure>();

    private List<InetSocketAddress> hubs = new ArrayList<InetSocketAddress>();
    private List<InetSocketAddress> telemetryHubs = new ArrayList<InetSocketAddress>();
    private Patterniser patterniser = new Patterniser();

    private Closer closer = new Closer();

    public Stream<LogEvent> getEventStream() {
        return eventStream;
    }

    public Stream<PatternisedLogEvent> getPatternisedStream() {
        return patternisedStream;
    }

    public Stream<DataStructure> getTelemetryStream() {
        return telemetryStream;
    }

    public void start() {

        startLogEventListeners(eventStream);

        // final StatBundle statBundle = new StatBundle();
        // final IntegerStat events = statBundle.createStat("Events");
        // events.setIncremental(true);

        // final List<ValueStripper2> strippers = new
        // ArrayList<ValueStripper2>();
        // populateStrippers(newChartingModel, strippers);

        bindPatterniser();

        // statBundle.startPerSecond(Logger.root());

        // final MonitoringBus bus = new MonitoringBus();

        // patternisedStream.addListener(new StreamListener<PatternisedEvent>()
        // {
        // public void onNewItem(PatternisedEvent t) {
        // if (t.getPatternName().equals("load")) {
        // bus.aggregator.add(Double.parseDouble(t.getData().get("time")));
        // }
        // }
        // });

        // try {
        // JettyLauncher.launchBlocking(bus, 8080);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
    }

    private void bindPatterniser() {
        eventStream.addListener(new StreamListener<LogEvent>() {
            public void onNewItem(LogEvent t) {
                PatternisedLogEvent patternised = patterniser.patternise(t);
                if (patternised != null) {
                    patternisedStream.send(patternised);
                }
            }
        });
    }

    private void startLogEventListeners(final Stream<LogEvent> eventStream) {
        // TODO : implement stop and close the clients
        final SocketClient client = new SocketClient();
        for (InetSocketAddress inetSocketAddress : hubs) {
            client.addConnectionPoint(new InetSocketAddress(inetSocketAddress.getHostName(), inetSocketAddress.getPort()));
        }

        client.addLogEventListener(new LogEventListener() {
            public void onNewLogEvent(final LogEvent event) {
                eventStream.send(event);
            }
        });

        final SocketClientManager manager = new SocketClientManager(client);
        manager.setExceptionHandler(new ExceptionHandler() {
            @Override public void handleException(String action, Throwable t) {}
        });
        manager.start();

        closer.register(new Closeable() {
            @Override public void close() throws IOException {
                manager.stop();
                client.close();
            }
        });

    }

    // public String index() {
    // return aggregator.dumpStats();
    // }

  

    public void close() {
        closer.closeQuietly();
    }

    // private void populateStrippers(final NewChartingModel newChartingModel, List<ValueStripper2>
    // strippers) {
    // final File config = new File("C:\\dev\\vlcore\\vl-logging-frontend\\charting.xml");
    // if (config.exists()) {
    // logger.info("Loading charting configuration from '{}'", config.getAbsoluteFile());
    // Xml xml = new Xml(FileUtils.read(config));
    // newChartingModel.fromXml(xml.getRoot());
    //
    // ObservableList<PatternModel> patternModels = newChartingModel.getPatternModels();
    //
    // for (PatternModel patternModel : patternModels) {
    // final ValueStripper2 stripper = new ValueStripper2();
    // logger.info("Building stripper for {}", patternModel.getName().get());
    // stripper.setPatternName(patternModel.getName().get());
    // stripper.setPattern(patternModel.getPattern().get());
    // strippers.add(stripper);
    // }
    // }
    // }

    public void addHub(String host, int port) {
        hubs.add(new InetSocketAddress(host, port));
    }

    public void addTelemetry(String host, int port) {
        telemetryHubs.add(new InetSocketAddress(host, port));
    }

    public void addChartingConfig(File config) {
        if (config.exists()) {
            logger.debug("Loading charting configuration from '{}'", config.getAbsoluteFile());
            Xml xml = new Xml(FileUtils.read(config));
            NewChartingModel newChartingModel = new NewChartingModel();
            newChartingModel.fromXml(xml.getRoot());

            ObservableList<PatternModel> patternModels = newChartingModel.getPatternModels();

            for (PatternModel patternModel : patternModels) {
                final ValueStripper2 stripper = new ValueStripper2();
                logger.info("Building stripper for {}", patternModel.getName().get());
                stripper.setPatternName(patternModel.getName().get());
                stripper.setPattern(patternModel.getPattern().get());
                patterniser.add(stripper);
            }
        }
        else {
            throw new RuntimeException("Config file not found : " + config.getAbsolutePath());
        }
    }

    public void startStatsLogging() {
        final StatBundle statBundle = new StatBundle();
        final IntegerStat events = statBundle.createStat("Events");
        final IntegerStat paternised = statBundle.createStat("Paternised");
        final IntegerStat telemetry = statBundle.createStat("Telemetry");

        paternised.setIncremental(true);
        telemetry.setIncremental(true);
        events.setIncremental(true);

        getEventStream().addListener(new StreamListener<LogEvent>() {
            public void onNewItem(LogEvent t) {
                events.increment();
            }
        });

        getTelemetryStream().addListener(new StreamListener<DataStructure>() {
            public void onNewItem(DataStructure t) {
                telemetry.increment();
            }
        });

        getPatternisedStream().addListener(new StreamListener<PatternisedLogEvent>() {
            public void onNewItem(PatternisedLogEvent t) {
                paternised.increment();
            }
        });

        statBundle.startPerSecond(Logger.root());
    }

    public void dumpPatterns(Logger logger) {
        patterniser.dumpPatterns(logger);
    }

    public void addPattern(String name, String pattern) {
        final ValueStripper2 stripper = new ValueStripper2();
        stripper.setPatternName(name);
        stripper.setPattern(pattern);
        patterniser.add(stripper);
    }

    public void addDemoSource() {
        DemoLogEventProducer producer = new DemoLogEventProducer();
        producer.addLogEventListener(new LogEventListener() {
            @Override public void onNewLogEvent(LogEvent event) {
                eventStream.send(event);
            }
        });
        producer.produceEventsOnTimer(1, 100, 1);
    }

    public void stop() {
        closer.closeQuietly();
    }
}
