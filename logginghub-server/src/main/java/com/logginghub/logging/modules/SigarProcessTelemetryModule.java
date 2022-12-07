package com.logginghub.logging.modules;

import java.text.NumberFormat;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.modules.configuration.SigarProcessTelemetryConfiguration;
import com.logginghub.logging.telemetry.SigarProcessTelemetryGenerator;
import com.logginghub.logging.telemetry.SigarHelper;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Destination;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Values;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class SigarProcessTelemetryModule implements Module<SigarProcessTelemetryConfiguration>, Asynchronous {

    private SigarProcessTelemetryConfiguration configuration;
    private boolean sigar;
    private static final Logger logger = Logger.getLoggerFor(SigarProcessTelemetryModule.class);
    private Destination<LogEvent> eventDestination;
    private SigarProcessTelemetryGenerator generator;

    @SuppressWarnings("unchecked") @Override public void configure(SigarProcessTelemetryConfiguration configuration, ServiceDiscovery discovery) {
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
        
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);


        generator = new SigarProcessTelemetryGenerator("processName");
        generator.getDataStructureMultiplexer().addDestination(new Destination<DataStructure>() {
            @Override public void send(DataStructure t) {
             
                StringBuilder line = new StringBuilder();
              
                line.append("memmax=").append(nf.format(t.getDoubleValue(Values.JVM_Process_Memory_Maximum)/1024d));
                line.append(" memtot=").append(nf.format(t.getDoubleValue(Values.JVM_Process_Memory_Total)/1024d));
                line.append(" memusd=").append(nf.format(t.getDoubleValue(Values.JVM_Process_Memory_Used)/1024d));
                
                line.append(" cpu=").append(nf.format(t.getDoubleValue(Values.SIGAR_OS_Process_Cpu_Percentage)));
                line.append(" us=").append(nf.format(t.getDoubleValue(Values.SIGAR_OS_Process_Cpu_User_Time)));
                line.append(" sy=").append(nf.format(t.getDoubleValue(Values.SIGAR_OS_Process_Cpu_System_Time)));
                
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
        if(generator != null)  {
            generator.stop();
            generator = null;
        }
    }

}
