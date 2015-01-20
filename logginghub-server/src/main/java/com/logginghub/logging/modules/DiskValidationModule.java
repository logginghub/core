package com.logginghub.logging.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.modules.configuration.DiskValidationConfiguration;
import com.logginghub.logging.repository.RotatingHelper;
import com.logginghub.logging.repository.SofBlockPointer;
import com.logginghub.logging.repository.SofBlockStreamReader;
import com.logginghub.utils.Destination;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;

public class DiskValidationModule implements Module<DiskValidationConfiguration> {

    private static final Logger logger = Logger.getLoggerFor(DiskValidationModule.class);

    private DiskValidationConfiguration configuration;

    private WorkerThread thread;

    @SuppressWarnings("unchecked") @Override public void configure(DiskValidationConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;

        // File folder = new File(configuration.getFolder());
        // String prefix = "hub.log.";
        // String postfix = ".binary";

        // SofConfiguration sofConfiguration = new SofConfiguration();
        // sofConfiguration.registerType(DefaultLogEvent.class, 0);

        // reader = new SofBlockStreamRotatingReader(folder, prefix, postfix, sofConfiguration);
        //
        //
        // SocketHubInterface socketHub = discovery.findService(SocketHubInterface.class);
        //
        // socketHub.addMessageListener(HistoricalDataRequest.class, new SocketHubMessageHandler() {
        // @Override public void handle(final LoggingMessage message, final SocketConnection source)
        // {
        // threadPool.execute(new Runnable() {
        // @Override public void run() {
        // handleDataRequest((HistoricalDataRequest) message, source);
        // }
        // });
        // }
        // });
        //
        // if (!configuration.isReadOnly()) {
        // @SuppressWarnings("unchecked") Source<LogEvent> eventSource =
        // discovery.findService(Source.class,
        // LogEvent.class,
        // configuration.getLogEventSourceRef());
        // eventSource.addDestination(new Destination<LogEvent>() {
        // @Override public void send(LogEvent t) {
        // DiskValidationModule.this.send(t);
        // }
        // });
        // }
        //

    }

    @Override public void start() {
        stop();

        thread = WorkerThread.execute("LoggingHub-DiskValidator", new Runnable() {
            @Override public void run() {
                validate();
            }
        });
    }

    protected void validate() {

        File folder = new File(configuration.getFolder());

        logger.info("Validating files in folder '{}'", folder.getAbsolutePath());

        SofConfiguration sofConfiguration = new SofConfiguration();
        sofConfiguration.registerType(DefaultLogEvent.class, 0);
        sofConfiguration.registerType(HistoricalIndexElement.class, 1);

        String prefix = "hub.log.";
        String postfix = ".index";

        File[] sortedFileList = RotatingHelper.getSortedFileList(folder, prefix, postfix);
        for (File file : sortedFileList) {
            logger.info("Checking blocks in file : '{}'", file.getAbsolutePath());
            SofBlockStreamReader reader = new SofBlockStreamReader(sofConfiguration);
            try {
                List<SofBlockPointer> loadPointers = reader.loadPointers(file);
                for (SofBlockPointer sofBlockPointer : loadPointers) {
                    logger.info("Block pointer : '{}'", sofBlockPointer);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (SofException e) {
                e.printStackTrace();
            }

            logger.info("Dumping file : '{}'", file.getAbsolutePath());
            try {
                reader.visit(new FileInputStream(file), new Destination<SerialisableObject>() {
                    @Override public void send(SerialisableObject t) {
                        logger.info("Item : '{}'", t);
                    }
                });
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (SofException e) {
                e.printStackTrace();
            }
        }

    }

    @Override public void stop() {
        if (thread != null) {
            thread.stop();
        }
    }

}
