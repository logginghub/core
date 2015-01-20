package com.logginghub.logging.modules;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.modules.configuration.BinaryWriterConfiguration;
import com.logginghub.logging.repository.LocalDiskRepository;
import com.logginghub.logging.repository.config.LocalDiskRepositoryConfiguration;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Source;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class BinaryWriterModule implements Module<BinaryWriterConfiguration>, Destination<LogEvent> {

    private LocalDiskRepository binaryExporter;
    private BinaryWriterConfiguration configuration;

    @Override public void configure(BinaryWriterConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
        
        Source<LogEvent> eventSource = discovery.findService(Source.class, LogEvent.class, configuration.getEventSourceRef());
        eventSource.addDestination(this);        
    }

    @Override public void start() {
        stop();
        
        LocalDiskRepositoryConfiguration config = new LocalDiskRepositoryConfiguration();
        config.setDataFolder(configuration.getFolder());
        config.setPrefix(configuration.getFilename() + ".");
        config.setFileDurationMilliseconds(TimeUtils.parseInterval(configuration.getFileDuration()));

        binaryExporter = new LocalDiskRepository(config);
    }

    @Override public void stop() {
        if (binaryExporter != null) {
            binaryExporter.close();
            binaryExporter = null;
        }
    }

    @Override public void send(LogEvent t) {
        binaryExporter.onNewLogEvent(t);
    }

    public void flush() {
        binaryExporter.flushAndCloseCurrentFile();
    }

}
