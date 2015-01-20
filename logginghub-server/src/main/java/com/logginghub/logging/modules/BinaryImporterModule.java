package com.logginghub.logging.modules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.modules.configuration.BinaryImportConfiguration;
import com.logginghub.logging.repository.BinaryLogFileReader;
import com.logginghub.logging.utils.KryoVersion1Decoder;
import com.logginghub.logging.utils.LogEventBlockElement;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.Source;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.Provides;
import com.logginghub.utils.module.ProxyServiceDiscovery;
import com.logginghub.utils.module.ServiceDiscovery;

@Provides(LogEvent.class) public class BinaryImporterModule implements Module<BinaryImportConfiguration>, Source<LogEvent> {

    private static final Logger logger = Logger.getLoggerFor(BinaryImporterModule.class);
    private BinaryImportConfiguration configuration;

    private Multiplexer<LogEvent> eventMultiplexer = new Multiplexer<LogEvent>();
    private WorkerThread thread;

    @Override public void configure(BinaryImportConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
        Destination<LogEvent> eventDestination = discovery.findService(Destination.class, LogEvent.class, configuration.getDestinationRef());
        eventMultiplexer.addDestination(eventDestination);
    }

    public Multiplexer<LogEvent> getEventMultiplexer() {
        return eventMultiplexer;
    }

    @Override public void start() {
        stop();

        String binaryFolder;

        final File file = new File(configuration.getFile());

        List<DecodeStrategy> decodeStrategies = new ArrayList<DecodeStrategy>();

        decodeStrategies.add(new DecodeStrategy() {
            final BinaryLogFileReader decoder = new BinaryLogFileReader();

            @Override public boolean canParse(File input) {
                return decoder.canParse(input);
            }

            @Override public void decode(File file, StreamListener<LogEventBlockElement> blockListener, StreamListener<LogEvent> eventListener)
                            throws IOException {
                decoder.readFileInternal(file, blockListener, eventListener);
            }

            @Override public String getStrategyName() {
                return "BinaryLogFileReader";
            }
        });

        decodeStrategies.add(new DecodeStrategy() {
            final KryoVersion1Decoder decoder = new KryoVersion1Decoder();

            @Override public boolean canParse(File input) {
                return decoder.canParse(input);
            }

            @Override public void decode(File file, StreamListener<LogEventBlockElement> blockListener, StreamListener<LogEvent> eventListener)
                            throws IOException {
                decoder.setOutputStats(configuration.isOutputStats());
                decoder.readFileInternal(file, blockListener, eventListener);
            }

            @Override public String getStrategyName() {
                return "KryoVersion1Decoder";
            }
        });

        boolean parsed = false;

        for (final DecodeStrategy decodeStrategy : decodeStrategies) {
            if (decodeStrategy.canParse(file)) {

                final StreamListener<LogEventBlockElement> blockHandler = new StreamListener<LogEventBlockElement>() {
                    @Override public void onNewItem(LogEventBlockElement t) {

                    }
                };

                thread = WorkerThread.execute("ImporterThread", new Runnable() {
                    @Override public void run() {
                        try {
                            decodeStrategy.decode(file, blockHandler, eventMultiplexer);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                parsed = true;
                break;
            }
        }

        if (!parsed) {
            logger.warn("Failed to parse binary file '{}' with any of the strategies we know about, giving up", file.getAbsolutePath());
        }

    }

    @Override public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }

    @Override public void addDestination(Destination<LogEvent> listener) {
        eventMultiplexer.addDestination(listener);
    }

    @Override public void removeDestination(Destination<LogEvent> listener) {
        eventMultiplexer.removeDestination(listener);
    }

    public static void importFileBlocking(File file, Destination<LogEvent> destination) {
        BinaryImporterModule module = new BinaryImporterModule();
        BinaryImportConfiguration configuration = new BinaryImportConfiguration();
        configuration.setFile(file.getAbsolutePath());
        module.configure(configuration, new ProxyServiceDiscovery());
        module.getEventMultiplexer().addDestination(destination);
        module.start();
        module.thread.join();        
    }

}
