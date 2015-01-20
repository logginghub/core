package com.logginghub.logging.modules;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.logginghub.logging.modules.configuration.BadEventsReportConfiguration;
import com.logginghub.logging.modules.configuration.BinaryProcessorConfiguration;
import com.logginghub.logging.modules.configuration.EventCountingProcessorConfiguration;
import com.logginghub.logging.modules.configuration.RegexExtractingProcessorConfiguration;
import com.logginghub.logging.repository.BinaryLogFileReader;
import com.logginghub.logging.repository.DataFileNameFactory;
import com.logginghub.logging.repository.DataHandlerInterface;
import com.logginghub.logging.repository.HazelcastDataHandler;
import com.logginghub.logging.repository.LogDataProcessor;
import com.logginghub.logging.repository.processors.BadEventsReport;
import com.logginghub.logging.repository.processors.EventCountingProcessor;
import com.logginghub.logging.repository.processors.RegexExtractingProcessor;
import com.logginghub.utils.CollectionUtils;
import com.logginghub.utils.FileDateFormat;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Is;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.httpd.NanoHTTPD;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class BinaryProcessorModule implements Module<BinaryProcessorConfiguration> {

    public static final String filenameRegex = ".*?(\\d{8})\\.(\\d{6})\\..*";
    private static final Logger logger = Logger.getLoggerFor(BinaryProcessorModule.class);
    private BinaryProcessorConfiguration configuration;
    private DateFormat dateFormat = new FileDateFormat();
    private DataHandlerInterface dataHandler = new HazelcastDataHandler();
    private NanoHTTPD httpd;
    private WorkerThread thread;

    public List<LogDataProcessor> instantiateProcessors(BinaryProcessorConfiguration configuration) {
        List<LogDataProcessor> listeners = new ArrayList<LogDataProcessor>();

        List<EventCountingProcessorConfiguration> eventCounterProcessorsConfiguration = configuration.getEventCounterProcessorsConfiguration();
        for (EventCountingProcessorConfiguration config : eventCounterProcessorsConfiguration) {
            EventCountingProcessor eventCountingProcessor = new EventCountingProcessor(config.getAggregationPeriod());
            listeners.add(eventCountingProcessor);
        }

        List<RegexExtractingProcessorConfiguration> regexProcessorsConfiguration = configuration.getRegexProcessorsConfiguration();
        for (RegexExtractingProcessorConfiguration conf : regexProcessorsConfiguration) {

            Is.notNull(conf.getName(), "You must include the 'name' attribute to stop results overwriting other regex extractors");
            Is.notNull(conf.getExpression(), "You must include the 'expression' attribute so we know what to extract from the logging stream");

            RegexExtractingProcessor processor = new RegexExtractingProcessor(conf.getName(), conf.getAggregationPeriod());
            processor.setSimpleExpression(conf.getExpression());
            processor.setAllowNumericParseFailures(conf.getAllowNumericParseFailures());
            processor.setCountNameElements(conf.getCountNameElements());
            listeners.add(processor);
        }

        List<BadEventsReportConfiguration> badEventReportsConfigurations = configuration.getBadEventReportsConfiguration();
        for (BadEventsReportConfiguration badEventsReportConfiguration : badEventReportsConfigurations) {
            BadEventsReport report = new BadEventsReport();
            report.setName(badEventsReportConfiguration.getName());
            report.setRollupRegexs(badEventsReportConfiguration.getRollupRegexs());
        }

        populateCustom(configuration, listeners);

        return listeners;
    }

    private void populateCustom(BinaryProcessorConfiguration configuration, List<LogDataProcessor> listeners) {
        List<String> processors = configuration.getCustomProcessors();
        for (String string : processors) {

            Class<?> clazz;
            try {
                clazz = Class.forName(string);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(String.format("Class '{}' not found, please check your configuration file", string), e);
            }

            if (LogDataProcessor.class.isAssignableFrom(clazz)) {

                try {
                    LogDataProcessor listener = (LogDataProcessor) clazz.newInstance();
                    listeners.add(listener);
                }
                catch (InstantiationException e) {
                    throw new RuntimeException(String.format("Class '{}' couldn't be instantiated. Does it have a default constructor?", string), e);
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException(String.format("Class '{}' couldn't be instantiated due to the SecurityManager in your VM. You'll have to figure this one out yourself...",
                                                             string),
                                               e);
                }

            }
            else {
                throw new RuntimeException(String.format("Class '{}' does not implement the LogEventListener interface, so cannot be used a processor. Please check your configuration file.",
                                                         string));
            }

        }
    }

    public void processAllFiles() {

        File folder = configuration.getInputFolder();
        logger.fine("Starting file check for path '{}'", folder.getAbsolutePath());

        File file = processNextFile();
        while (file != null) {
            file = processNextFile();
        }
    }

    public File processNextFile() {
        File file;
        List<File> files = getOrderedFileList();
        if (!files.isEmpty()) {
            file = files.get(0);
            processFile(file);
        }
        else {
            file = null;
        }
        return file;
    }

    public List<File> getOrderedFileList() {
        File[] files = configuration.getInputFolder().listFiles(new FileFilter() {
            public boolean accept(File file) {
                // Match: .logdata .logdata.0
                // Dont match: .logdata.writing
                return StringUtils.matches(file.getName(), ".*\\.logdata(\\.\\d+)?");
            }
        });

        List<File> sortedCopy = new ArrayList<File>();

        // TESTME
        if (files != null) {
            CollectionUtils.addAll(files, sortedCopy);
            Collections.sort(sortedCopy, new Comparator<File>() {
                FileDateFormat fdf = new FileDateFormat();

                @Override public int compare(File left, File right) {
                    String leftName = left.getName();
                    String rightName = right.getName();

                    String leftDatePart = DataFileNameFactory.extractDatePart(configuration.getPrefix(), leftName);
                    String rightDatePart = DataFileNameFactory.extractDatePart(configuration.getPrefix(), rightName);

                    try {
                        return fdf.parse(rightDatePart).compareTo(fdf.parse(leftDatePart));
                    }
                    catch (ParseException e) {
                        throw new RuntimeException(String.format("Failed to parse filenames for '{}' and/or '{}'",
                                                                 left.getAbsolutePath(),
                                                                 right.getAbsolutePath()));
                    }
                }
            });
        }
        // TESTME

        logger.fine("Found {} files", sortedCopy.size());

        return sortedCopy;
    }

    private void processFile(File file) {
        logger.info("Processing '{}'", file.getAbsoluteFile());

        List<String> matchGroups = StringUtils.matchGroups(file.getName(), filenameRegex);
        if (matchGroups != null && matchGroups.size() == 2) {
            // Extract the start time
            // String name = file.getName();
            // String[] split = name.split("\\.");
            String day = matchGroups.get(0);
            String time = matchGroups.get(1);
            try {
                String source = day + "." + time;
                long timestamp = dateFormat.parse(source).getTime();
                processFile(timestamp, source, file);
            }
            catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            logger.warning("Failed to parse time from file name '{}'", file.getName());
        }

    }

    private void processFile(long timestamp, String source, File file) {
        logger.info("Processing file '" + file.getAbsolutePath() + "'");

        File specificResultsFolder = new File(configuration.getResultsFolder(), source);
        specificResultsFolder.mkdirs();
        FileUtils.deleteContents(specificResultsFolder);

        List<LogDataProcessor> processors = instantiateProcessors(configuration);

        BinaryLogFileReader reader = new BinaryLogFileReader();
        reader.readFile(file, specificResultsFolder, processors);

        File to = new File(configuration.getOutputFolder(), file.getName());
        FileUtils.moveRuntime(file, to);
        logger.info("Moved file '{}' to '{}", file.getAbsolutePath(), to.getAbsolutePath());
    }

    // public static void main(String[] args) {
    // // TODO: load from xml
    // String configPath = null;
    // if (args.length >= 1) {
    // configPath = args[0];
    // }
    // else {
    // logger.info("WARNING : no config file path passed in on the command line, using the sample configuration");
    // configPath =
    // "classpath:/com/logginghub/logging/repository/sampleDataFileProcessorConfiguration.xml";
    // }
    // DataFileProcessorConfiguration configuration =
    // DataFileProcessorConfiguration.load(configPath);
    // logger.info(configuration.toString());
    // BinaryProcessorModule processor = new BinaryProcessorModule(configuration);
    // processor.start();
    // }

    public DataHandlerInterface getDataHandlerInterface() {
        return dataHandler;
    }

    @Override public void start() {

        configuration.getInputFolder().mkdirs();
        configuration.getOutputFolder().mkdirs();
        configuration.getResultsFolder().mkdirs();

        try {
            httpd = new NanoHTTPD(configuration.getHttpPort(), configuration.getResultsFolder());
            httpd.serveFile("", null, configuration.getResultsFolder(), true);
        }
        catch (IOException e) {
            logger.warn(e, "Failed to create nanoHTTPD server");
        }

        thread = WorkerThread.every("LoggingHub-BinaryProcessorModule-CheckingTimer",
                                    configuration.getFileCheckIntervalMilliseconds(),
                                    TimeUnit.MILLISECONDS,
                                    new Runnable() {
                                        public void run() {
                                            processAllFiles();
                                        }
                                    });
    }

    @Override public synchronized void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }

        if (httpd != null) {
            httpd.stop();
            httpd = null;
        }
    }

    @Override public void configure(BinaryProcessorConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
    }
}
