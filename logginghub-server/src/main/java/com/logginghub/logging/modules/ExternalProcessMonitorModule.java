package com.logginghub.logging.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.modules.configuration.ExternalProcessMonitorConfiguration;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Destination;
import com.logginghub.utils.InputStreamReaderThread;
import com.logginghub.utils.InputStreamReaderThreadListener;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.ProcessWrapper;
import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Values;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class ExternalProcessMonitorModule implements Module<ExternalProcessMonitorConfiguration>,Asynchronous {

    private final static Logger logger = Logger.getLoggerFor(ExternalProcessMonitorModule.class);
    private Timer timer;
    private Process process;
    private ExternalProcessMonitorConfiguration configuration;

    private String host = NetUtils.getLocalHostname();
    private String ip = NetUtils.getLocalIP();
    
    private Multiplexer<LogEvent> logEventMultiplexer = new Multiplexer<LogEvent>();
    private Multiplexer<DataStructure> dataStructureMultiplexer = new Multiplexer<DataStructure>();

    @Override public void configure(ExternalProcessMonitorConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
        
        @SuppressWarnings("unchecked") Destination<LogEvent> eventDestination = discovery.findService(Destination.class,
                                                                                                      LogEvent.class,
                                                                                                      configuration.getDestination());
        logEventMultiplexer.addDestination(eventDestination);
    }

    public Multiplexer<LogEvent> getLogEventMultiplexer() {
        return logEventMultiplexer;
    }

    public Multiplexer<DataStructure> getDataStructureMultiplexer() {
        return dataStructureMultiplexer;
    }

    public void start() {
        timer = TimerUtils.nowAndEvery(configuration.getName() + "-timer",
                                       TimeUtils.parseInterval(configuration.getInterval()),
                                       TimeUnit.MILLISECONDS,
                                       new Runnable() {
                                           public void run() {
                                               runProcess();
                                           }
                                       });
    }

    protected void runProcess() {
        try {

            String result;

            String simulationResource = configuration.getSimulationResource();
            if (simulationResource != null) {
                result = ResourceUtils.read(simulationResource);
            }
            else {
                result = runCommand(configuration.getCommand());
            }

            if (configuration.isSendTelemetryEvents()) {
                DataStructure dataStructure = new DataStructure(DataStructure.Types.Telemetry);
                dataStructure.addKey(DataStructure.Keys.host, host);
                dataStructure.addKey(DataStructure.Keys.ip, ip);

                int value;

                String valueEnum = configuration.getValueEnum();
                if (valueEnum != null && valueEnum.trim().length() > 0) {
                    value = Values.valueOf(valueEnum).ordinal();
                }
                else {
                    value = configuration.getValueCode();
                }

                dataStructure.addValue(value, result);
                dataStructureMultiplexer.send(dataStructure);
            }

            if (configuration.isLogRawEvents()) {
                logEventMultiplexer.send(LogEventBuilder.start()
                                                        .setLevel(configuration.getLevelForRawEvents())
                                                        .setSourceApplication("TelemetryAgent")
                                                        .setChannel(configuration.getChannel())
                                                        .setMessage(configuration.getPrefix() + result + configuration.getPostfix())
                                                        .toLogEvent());
            }

        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to start process '%s'", configuration.getCommand()), e);
        }
    }

    private String runCommand(String command) throws IOException {
        ProcessWrapper wrapper;
        wrapper = ProcessWrapper.execute(new File("."), command.split(" "));

        InputStreamReaderThread errorReader = new InputStreamReaderThread(wrapper.getErrorStream());
        errorReader.addListener(new InputStreamReaderThreadListener() {
            public void onLine(String line) {
                logger.warn(line);
            }

            public void onCharacter(char c) {

            }
        });
        errorReader.start();

        process = wrapper.getProcess();
        InputStream inputStream = process.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(reader);

        StringUtilsBuilder builder = StringUtils.builder();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            builder.appendLine(line);
        }

        wrapper.closeProcessCleanly();

        return builder.toString();
    }

    @Override public void stop() {
        process.destroy();
        timer.cancel();
    }



}
