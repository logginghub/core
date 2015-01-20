package com.logginghub.logging.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.modules.configuration.VMStatMonitorConfiguration;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Destination;
import com.logginghub.utils.InputStreamReaderThread;
import com.logginghub.utils.InputStreamReaderThreadListener;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.OSUtils;
import com.logginghub.utils.ProcessWrapper;
import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Values;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class VMStatMonitorModule implements Module<VMStatMonitorConfiguration>, Asynchronous {

    private String processName;
    private WorkerThread thread;

    private static final Logger logger = Logger.getLoggerFor(VMStatMonitorModule.class);
    private List<Values> orderedValues;

    private Map<String, Values> knownHeaders = new HashMap<String, Values>();
    private Map<Values, String> tagLookup = new HashMap<Values, String>();

    private boolean forceSimulator = false;
    private boolean isSimulating = false;
    private volatile boolean keepRunning = true;
    private Process process;
    private VMStatMonitorConfiguration configuration;

    private String host = NetUtils.getLocalHostname();
    private String ip = NetUtils.getLocalIP();

    private Multiplexer<LogEvent> logEventMultiplexer = new Multiplexer<LogEvent>();
    private Multiplexer<DataStructure> dataStructureMultiplexer = new Multiplexer<DataStructure>();
    private long retryDelay = 5000;

    @Override public void configure(VMStatMonitorConfiguration configuration, ServiceDiscovery discovery) {
        // this.processName = processName;
        this.configuration = configuration;
        setupKnownHeaders();

        @SuppressWarnings("unchecked") Destination<LogEvent> eventDestination = discovery.findService(Destination.class,
                                                                                                      LogEvent.class,
                                                                                                      configuration.getDestination());
        logEventMultiplexer.addDestination(eventDestination);

    }

    protected void executeVMStat() {

        InputStream stream = null;
        if (!forceSimulator && OSUtils.isNixVariant()) {
            stream = startSubProcess();
        }
        else {
            stream = startSimulator();
        }

        InputStreamReader reader = new InputStreamReader(stream);

        BufferedReader bufferedReader = new BufferedReader(reader);

        try {
            int count = 0;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                parseLine(count, line);
                count++;

                if (isSimulating) {
                    ThreadUtils.sleep(1000);
                }

            }
        }
        catch (IOException e) {
            if (keepRunning) {
                logger.warn(e,
                            "Failed to read vmstat input stream, will sleep for {} before trying again",
                            TimeUtils.formatIntervalMilliseconds(retryDelay));
                ThreadUtils.sleep(retryDelay);
            }
        }

    }

    public Multiplexer<LogEvent> getLogEventMultiplexer() {
        return logEventMultiplexer;
    }

    public Multiplexer<DataStructure> getDataStructureMultiplexer() {
        return dataStructureMultiplexer;
    }

    public void start() {
        keepRunning = true;
        thread = new WorkerThread("LoggingHub-vmstat-reader") {
            @Override protected void onRun() throws Throwable {
                executeVMStat();
            }
        };
        thread.start();
    }

    private InputStream startSubProcess() {
        String command = configuration.getCommand();

        ProcessWrapper wrapper;
        try {
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
            // attachReaderThread(inputStream);
            return inputStream;

        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to start process '%s'", command), e);
        }
    }

    private InputStream startSimulator() {
        String simulationResource = configuration.getSimulationResource();
        if (simulationResource == null) {
            throw new IllegalArgumentException("No vmstat simulator resource has been provided");
        }
        InputStream openStream = ResourceUtils.openStream(simulationResource);
        // attachReaderThread(openStream);
        isSimulating = true;
        return openStream;
    }

    protected void parseLine(int count, String line) {
        logger.debug("Parsing line '{}'", line);
        switch (count) {
            case 0: {
                /* ignore */
                break;
            }
            case 1: {
                parseHeaders(line);
                break;
            }
            case 2: {
                /* ignore first line of data */
                break;
            }
            default: {
                parseData(line);
            }
        }
    }

    private void parseData(String line) {

        // TODO : get rid of this
        // TelemetryData telemetry = new TelemetryData();
        // telemetry.readHostDetails();

        DataStructure dataStructure = new DataStructure(DataStructure.Types.Telemetry);
        dataStructure.addKey(DataStructure.Keys.host, host);
        dataStructure.addKey(DataStructure.Keys.ip, ip);
        dataStructure.addKey(DataStructure.Keys.processName, processName);

        StringBuilder keyValueLine = new StringBuilder();
        String div = "";

        List<String> list = StringUtils.toList(line);
        for (int i = 0; i < list.size(); i++) {
            String valueString = list.get(i);

            int value = Integer.parseInt(valueString);
            Values valueKey = orderedValues.get(i);

            if (valueKey != null) {
                logger.debug("Setting value '{}' to '{}'", valueKey, value);
                dataStructure.addValue(valueKey, value);

                keyValueLine.append(div).append(tagLookup.get(valueKey)).append("=").append(value);
                div = " ";
            }
        }

        dataStructureMultiplexer.send(dataStructure);

        if (configuration.isLogRawEvents()) {
            logEventMultiplexer.send(LogEventBuilder.start()
                                                    .setLevel(configuration.getLevelForRawEvents())
                                                    .setSourceApplication("TelemetryAgent")
                                                    .setMessage(configuration.getPrefix() + keyValueLine.toString())
                                                    .setChannel(configuration.getChannel())
                                                    .toLogEvent());
        }
    }

    private void parseHeaders(String line) {
        List<String> list = StringUtils.toList(line);

        orderedValues = new ArrayList<Values>();

        logger.debug("Parsing header '{}' to list '{}'", line, list);

        for (String string : list) {

            Values values = knownHeaders.get(string);
            orderedValues.add(values);

            if (values == null) {
                logger.warn("We've come across an unknown header entry '{}' - we'll carry on, but this value wont be included in the output. Please check your configuration if this is a non-standard column",
                            string);
            }

            logger.debug("Added header '{}'", values);

        }

    }

    private void setupKnownHeaders() {
        addLookup("r", Values.VMSTAT_Processes_Run_Queue);
        addLookup("b", Values.VMSTAT_Processes_Blocking);

        addLookup("swpd", Values.VMSTAT_Memory_Swap);
        addLookup("free", Values.VMSTAT_Memory_Free);
        addLookup("buff", Values.VMSTAT_Memory_Buffers);
        addLookup("cache", Values.VMSTAT_Memory_Cache);

        addLookup("inact", Values.VMSTAT_Memory_Inactive);
        addLookup("active", Values.VMSTAT_Memory_Active);

        addLookup("si", Values.VMSTAT_Swap_In);
        addLookup("so", Values.VMSTAT_Swap_Out);

        addLookup("bi", Values.VMSTAT_IO_Blocks_In);
        addLookup("bo", Values.VMSTAT_IO_Blocks_Out);

        addLookup("in", Values.VMSTAT_System_Interupts);
        addLookup("cs", Values.VMSTAT_System_Context_Switches);

        addLookup("us", Values.VMSTAT_CPU_User);
        addLookup("sy", Values.VMSTAT_CPU_System);
        addLookup("id", Values.VMSTAT_CPU_Idle);
        addLookup("wa", Values.VMSTAT_CPU_Waiting);

        addLookup("st", Values.VMSTAT_CPU_Stolen);
    }

    private void addLookup(String name, Values value) {
        knownHeaders.put(name, value);
        tagLookup.put(value, name);
    }

    public void setProcessName(String sourceApplication) {
        this.processName = sourceApplication;
    }

    public void stop() {
        keepRunning = false;

        if (thread != null) {
            thread.dontRunAgain();
        }

        if (process != null) {
            process.destroy();
        }

        if (thread != null) {
            thread.stop();
        }
    }

    public void setSimulator(boolean forceSimulator) {
        this.forceSimulator = forceSimulator;
    }

}
