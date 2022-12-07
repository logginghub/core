package com.logginghub.logging.modules;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.modules.configuration.SigarMachineTelemetryConfiguration;
import com.logginghub.logging.modules.configuration.SigarProcessTelemetryConfiguration;
import com.logginghub.logging.modules.configuration.TelemetryConfiguration;
import com.logginghub.logging.telemetry.SigarMachineTelemetryGenerator;
import com.logginghub.logging.telemetry.SigarProcessTelemetryGenerator;
import com.logginghub.logging.telemetry.SigarHelper;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Destination;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class TelemetryModule implements Module<TelemetryConfiguration> {

    private static final Logger logger = Logger.getLoggerFor(TelemetryModule.class);
    private List<Asynchronous> generators = new CopyOnWriteArrayList<Asynchronous>();

    @Override public void start() {
        for (Asynchronous telemetryGenerator : generators) {
            try {
                telemetryGenerator.start();
            }
            catch (RuntimeException e) {
                logger.warn(e, "Failed to start a telemetry generator - will carry on anyway");
            }
        }
    }

    @Override public void stop() {}

    @Override public void configure(TelemetryConfiguration config, ServiceDiscovery discovery) {

        final LoggingMessageSender messageSender = discovery.findService(LoggingMessageSender.class);

        String processName = config.getProcessName();

        Destination<DataStructure> dataStructureDestination = new Destination<DataStructure>() {
            @Override public void send(DataStructure t) {
                ChannelMessage message = new ChannelMessage(Channels.telemetryUpdates, t);
                try {
                    messageSender.send(message);
                }
                catch (LoggingMessageSenderException e) {
                    logger.info(e, "Failed to send message '{}'", message);
                }
            }
        };

        Destination<LogEvent> logEventDestination = new Destination<LogEvent>() {
            @Override public void send(LogEvent t) {
                LogEventMessage message = new LogEventMessage(t);
                try {
                    messageSender.send(message);
                }
                catch (LoggingMessageSenderException e) {
                    logger.info(e, "Failed to send message '{}'", message);
                }
            }
        };

//        VMStatMonitorConfiguration vmStatConfiguration = config.getVmStatConfiguration();
//        if (vmStatConfiguration != null) {
//            logger.info("vmstat configuration enabled : {}", vmStatConfiguration);
//            VMStatMonitorModule vmStatTelemetryGenerator = new VMStatMonitorModule(processName, vmStatConfiguration);
//            vmStatTelemetryGenerator.getDataStructureMultiplexer().addDestination(dataStructureDestination);
//            vmStatTelemetryGenerator.getLogEventMultiplexer().addDestination(logEventDestination);
//            generators.add(vmStatTelemetryGenerator);
//        }
//
//        IOStatMonitorConfiguration ioStatConfiguration = config.getIoStatConfiguration();
//        if (ioStatConfiguration != null) {
//            logger.info("iostat configuration enabled : {}", ioStatConfiguration);
//            IOStatMonitorModule ioStatTelemetryGenerator = new IOStatMonitorModule(processName, ioStatConfiguration);
//            ioStatTelemetryGenerator.getDataStructureMultiplexer().addDestination(dataStructureDestination);
//            ioStatTelemetryGenerator.getLogEventMultiplexer().addDestination(logEventDestination);
//            generators.add(ioStatTelemetryGenerator);
//        }

        boolean sigar = false;

        SigarProcessTelemetryConfiguration processTelemetryConfiguration = config.getProcessTelemetryConfiguration();
        if (processTelemetryConfiguration != null) {
            logger.info("SIGAR process telemetry configuration enabled : {}", processTelemetryConfiguration);
            if (!sigar) {
                SigarHelper.loadLibrary();
                sigar = true;
            }
            SigarProcessTelemetryGenerator generator = new SigarProcessTelemetryGenerator(processName);
            generator.getDataStructureMultiplexer().addDestination(dataStructureDestination);
            generator.setInterval(processTelemetryConfiguration.getInterval());
            generators.add(generator);
        }

        SigarMachineTelemetryConfiguration machineTelemetryConfiguration = config.getMachineTelemetryConfiguration();
        if (machineTelemetryConfiguration != null) {
            logger.info("SIGAR machine telemetry configuration enabled : {}", machineTelemetryConfiguration);
            if (!sigar) {
                SigarHelper.loadLibrary();
                sigar = true;
            }
            SigarMachineTelemetryGenerator generator = new SigarMachineTelemetryGenerator();
            generator.setInterval(machineTelemetryConfiguration.getInterval());
            generator.getDataStructureMultiplexer().addDestination(dataStructureDestination);
            generators.add(generator);
        }

//        List<ExternalProcessMonitorConfiguration> externalProcesses = config.getExternalProcesses();
//        if (externalProcesses != null) {
//            for (ExternalProcessMonitorConfiguration externalProcessConfiguration : externalProcesses) {
//                logger.info("External process configuration enabled : {}", externalProcessConfiguration);
//                ExternalProcessMonitorModule generator = new ExternalProcessMonitorModule(externalProcessConfiguration);
//                generators.add(generator);
//            }
//        }
//
//        List<ExternalFileMonitorConfiguration> externalFiles = config.getExternalFiles();
//        if (externalFiles != null) {
//            for (ExternalFileMonitorConfiguration externalFileConfiguration : externalFiles) {
//                logger.info("External file configuration enabled : {}", externalFileConfiguration);
//                ExternalFileMonitorModule generator = new ExternalFileMonitorModule(externalFileConfiguration);
//                generators.add(generator);
//            }
//        }

    }
}
