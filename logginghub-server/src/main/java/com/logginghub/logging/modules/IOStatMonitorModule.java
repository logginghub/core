package com.logginghub.logging.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.modules.configuration.IOStatMonitorConfiguration;
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
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Values;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class IOStatMonitorModule implements Module<IOStatMonitorConfiguration>, Asynchronous {

    private StringUtilsBuilder multilineBuilder = new StringUtils.StringUtilsBuilder();
    private BufferedReader bufferedReader;
    private String processName;
    private WorkerThread thread;

    private static final Logger logger = Logger.getLoggerFor(IOStatMonitorModule.class);
    private List<Values> orderedValues = new ArrayList<Values>();

    private Map<String, Values> knownHeaders = new HashMap<String, Values>();
    private Map<Values, String> tagLookup = new HashMap<Values, String>();

    private Set<String> ignoredHeaders = new HashSet<String>();
    private Process process;
    private int blankLines;
    private IOStatMonitorConfiguration configuration;

    private String host = NetUtils.getLocalHostname();
    private String ip = NetUtils.getLocalIP();

    private Multiplexer<DataStructure> dataStructureMultiplexer = new Multiplexer<DataStructure>();
    private Multiplexer<LogEvent> logEventMultiplexer = new Multiplexer<LogEvent>();

    @Override public void configure(IOStatMonitorConfiguration configuration, ServiceDiscovery discovery) {

        // this.processName = processName;
        this.configuration = configuration;
        setupKnownHeaders();

        @SuppressWarnings("unchecked") Destination<LogEvent> eventDestination = discovery.findService(Destination.class,
                                                                                                      LogEvent.class,
                                                                                                      configuration.getDestination());
        logEventMultiplexer.addDestination(eventDestination);

    }

    public void start() {
        if (configuration.isSimulating()) {
            startSimulator();
        }
        else {
            startSubProcess();
        }
    }

    private void startSubProcess() {
        String command = configuration.getCommand();

        logger.info("Executing {}", command);

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
            attachReaderThread(inputStream);

        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to start process '%s'", command), e);
        }
    }

    private void startSimulator() {
        InputStream openStream = ResourceUtils.openStream(configuration.getSimulationResource());
        attachReaderThread(openStream);
    }

    private void attachReaderThread(InputStream openStream) {
        InputStreamReader reader = new InputStreamReader(openStream);

        bufferedReader = new BufferedReader(reader);

        thread = WorkerThread.executeOngoing("iostat-reader", new Runnable() {
            public void run() {
                try {
                    String line = bufferedReader.readLine();

                    // jshaw - this is a bit of a hacky way of looping the simulator
                    if (line == null && configuration.isSimulating()) {
                        bufferedReader.close();
                        InputStream openStream = ResourceUtils.openStream(configuration.getSimulationResource());
                        bufferedReader = new BufferedReader(new InputStreamReader(openStream));
                        blankLines = 0;
                        line = bufferedReader.readLine();
                    }

                    if (line.trim().length() == 0) {
                        blankLines++;
                    }

                    parseLine(line);
                }
                catch (IOException e) {
                    logger.info(e);
                }

                if (configuration.isSimulating()) {
                    if (blankLines % 5 == 0) {
                        ThreadUtils.sleep(1000);
                    }
                }

            }
        });
    }

    protected void parseLine(String line) {
        logger.debug("Parsing line '{}'", line);

        String trim = line.trim();
        // We ignore the first line (os and cpu summary), second is blank, next
        // three are the system averages and are ignored - so start decoding when we have passed two
        // blank lines
        if (blankLines >= 2) {

            if (trim.startsWith("Device")) {
                if (orderedValues.isEmpty()) {
                    parseHeaders(line);
                }
            }
            else if (trim.length() > 0) {
                parseData(line);
            }
        }

        if (configuration.isLogRawEvents()) {
            multilineBuilder.appendLine(line);
            if (trim.length() == 0 && multilineBuilder.toString().trim().length() > 0) {
                logEventMultiplexer.send(LogEventBuilder.start()
                                                        .setLevel(configuration.getLevelForRawEvents())
                                                        .setSourceApplication("TelemetryAgent")
                                                        .setMessage(configuration.getPrefix() + multilineBuilder.toString())
                                                        .setChannel(configuration.getChannel())
                                                        .toLogEvent());
                multilineBuilder = new StringUtilsBuilder();
            }
        }

    }

    private void parseData(String line) {

        // TODO : get rid of this

        DataStructure dataStructure = new DataStructure(DataStructure.Types.Telemetry);
        dataStructure.addKey(DataStructure.Keys.host, host);
        dataStructure.addKey(DataStructure.Keys.ip, ip);
        dataStructure.addKey(DataStructure.Keys.processName, processName);

        StringBuilder deviceLine = new StringBuilder();
        String div = "";

        List<String> list = StringUtils.toList(line);
        for (int i = 0; i < list.size(); i++) {
            String valueString = list.get(i);

            if (i == 0) {
                // This is the device name
                dataStructure.addKey(DataStructure.Keys.device, valueString);

                deviceLine.append("dev=").append(valueString);
                div = " ";

            }
            else {
                int offset = i - 1;
                double value = Double.parseDouble(valueString);
                Values valueKey = orderedValues.get(offset);

                if (valueKey != null) {
                    logger.finer("Setting value '{}' to '{}'", valueKey, value);
                    dataStructure.addValue(valueKey, value);

                    deviceLine.append(div).append(tagLookup.get(valueKey)).append("=").append(value);
                }
            }
        }

        if (configuration.isLogRawEvents()) {
            logEventMultiplexer.send(LogEventBuilder.start()
                                                    .setLevel(configuration.getLevelForRawEvents())
                                                    .setSourceApplication("TelemetryAgent")
                                                    .setMessage(configuration.getPrefix() + deviceLine.toString())
                                                    .setChannel(configuration.getChannel())
                                                    .toLogEvent());
        }

        logger.debug("Sending telemetry : {}", dataStructure.toString());
        dataStructureMultiplexer.send(dataStructure);
    }

    public Multiplexer<DataStructure> getDataStructureMultiplexer() {
        return dataStructureMultiplexer;
    }

    public Multiplexer<LogEvent> getLogEventMultiplexer() {
        return logEventMultiplexer;
    }

    private void parseHeaders(String line) {
        List<String> list = StringUtils.toList(line);

        orderedValues = new ArrayList<Values>();

        logger.debug("Parsing header '{}' to list '{}'", line, list);

        for (String string : list) {

            if (ignoredHeaders.contains(string)) {
                // Ignore this
            }
            else {

                Values values = knownHeaders.get(string);
                orderedValues.add(values);

                if (values == null) {
                    logger.warn("We've come across an unknown header entry '{}' - we'll carry on, but this value wont be included in the output. Please check your configuration if this is a non-standard column",
                                string);
                }

                logger.finer("Added header '{}'", values);
            }
        }

    }

    private void setupKnownHeaders() {

        ignoredHeaders.add("Device:");

        knownHeaders("rrqm/s", Values.IOSTAT_Reads_Requested);
        knownHeaders("wrqm/s", Values.IOSTAT_Writes_Requested);

        knownHeaders("r/s", Values.IOSTAT_Reads_Completed);
        knownHeaders("w/s", Values.IOSTAT_Writes_Completed);
        knownHeaders("rkB/s", Values.IOSTAT_Read_Amount);
        knownHeaders("wkB/s", Values.IOSTAT_Write_Amount);

        knownHeaders("avgrq-sz", Values.IOSTAT_Average_Request_Size);
        knownHeaders("avgqu-sz", Values.IOSTAT_Average_Request_Queue_Length);

        knownHeaders("await", Values.IOSTAT_Request_Served_Time);
        knownHeaders("r_await", Values.IOSTAT_Read_Requests_Served_Time);
        knownHeaders("w_await", Values.IOSTAT_Write_Requests_Served_Time);

        knownHeaders("svctm", Values.IOSTAT_Service_Time);

        knownHeaders("%util", Values.IOSTAT_Device_Utilisation);
    }

    private void knownHeaders(String tag, Values value) {
        knownHeaders.put(tag, value);
        tagLookup.put(value, tag);
    }

    public void setProcessName(String sourceApplication) {
        this.processName = sourceApplication;
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
        }

        if (process != null) {
            process.destroy();
        }
    }

}
