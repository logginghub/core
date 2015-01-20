package com.logginghub.logging.modules;

import java.text.NumberFormat;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.modules.configuration.SigarMachineTelemetryConfiguration;
import com.logginghub.logging.telemetry.MachineTelemetryGenerator;
import com.logginghub.logging.telemetry.SigarHelper;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Destination;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Values;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class SigarMachineTelemetryModule implements Module<SigarMachineTelemetryConfiguration>, Asynchronous {

    private SigarMachineTelemetryConfiguration configuration;
    private boolean sigar;
    private static final Logger logger = Logger.getLoggerFor(SigarProcessTelemetryModule.class);
    private Destination<LogEvent> eventDestination;
    private MachineTelemetryGenerator generator;

    @Override public void configure(SigarMachineTelemetryConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
        eventDestination = discovery.findService(Destination.class, LogEvent.class, configuration.getDestination());
    }

    @Override public void start() {
        stop();

        logger.info("SIGAR process telemetry configuration enabled : {}", configuration);
        if (!sigar) {
            SigarHelper.loadLibrary();
            sigar = true;
        }

        final NumberFormat nf1pd = NumberFormat.getInstance();
        nf1pd.setMaximumFractionDigits(1);

        generator = new MachineTelemetryGenerator();
        generator.getDataStructureMultiplexer().addDestination(new Destination<DataStructure>() {
            @Override public void send(DataStructure t) {

                StringBuilder line = new StringBuilder();

                line.append("mfp=").append(nf1pd.format(t.getDoubleValue(Values.SIGAR_OS_Memory_Free_Percent)));
                line.append(" mup=").append(nf1pd.format(t.getDoubleValue(Values.SIGAR_OS_Memory_Used_Percent)));
                line.append(" mt=").append(t.getDoubleValue(Values.SIGAR_OS_Memory_Total) / 1024f);
                line.append(" mu=").append(t.getDoubleValue(Values.SIGAR_OS_Memory_Used) / 1024f);

                if (t.containsValue(Values.SIGAR_OS_Network_Bytes_Received)) {
                    line.append(" neti=").append(nf1pd.format(t.getDoubleValue(Values.SIGAR_OS_Network_Bytes_Received) / 1024f));
                    line.append(" neto=").append(nf1pd.format(t.getDoubleValue(Values.SIGAR_OS_Network_Bytes_Sent) / 1024f));
                }

                line.append(" cpu=").append(nf1pd.format(100f - t.getDoubleValue(Values.SIGAR_OS_Cpu_Idle_Time)));
                line.append(" us=").append(nf1pd.format(t.getDoubleValue(Values.SIGAR_OS_Cpu_User_Time)));
                line.append(" sy=").append(nf1pd.format(t.getDoubleValue(Values.SIGAR_OS_Cpu_System_Time)));
                line.append(" id=").append(nf1pd.format(t.getDoubleValue(Values.SIGAR_OS_Cpu_Idle_Time)));
                line.append(" wa=").append(nf1pd.format(t.getDoubleValue(Values.SIGAR_OS_Cpu_Wait_Time)));

                eventDestination.send(LogEventBuilder.start()
                                                     .setLevel(Logger.info)
                                                     .setSourceApplication("TelemetryAgent")
                                                     .setMessage(configuration.getPrefix() + line.toString())
                                                     .setChannel(configuration.getChannel())
                                                     .toLogEvent());
            }
        });

        generator.setInterval(configuration.getInterval());

        generator.start();

    }

    @Override public void stop() {
        if (generator != null) {
            generator.stop();
            generator = null;
        }
    }

}
