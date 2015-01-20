package com.logginghub.logging.repository;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.QueueAwareLoggingMessageSender;
import com.logginghub.logging.messages.HistoricalDataRequest;
import com.logginghub.logging.messages.HistoricalDataResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.modules.DiskHistoryIndexModule;
import com.logginghub.logging.modules.DiskHistoryModule;
import com.logginghub.logging.modules.configuration.DiskHistoryConfiguration;
import com.logginghub.logging.modules.configuration.DiskHistoryIndexConfiguration;
import com.logginghub.logging.simulator.TimeBasedGenerator;
import com.logginghub.utils.Destination;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.module.ProxyServiceDiscovery;
import com.logginghub.utils.sof.SofException;

public class SofSandbox {

    public static void main(String[] args) throws IOException, SofException {
        SofSandbox sandbox = new SofSandbox();

        File folder = new File("x:\\temp\\generator");
        // FileUtils.deleteContents(folder);
        sandbox.generate(folder);
        // sandbox.validateReadPerformance(folder);
        // sandbox.validateFullScanPerformance(folder);

    }

    private void validateFullScanPerformance(File folder) throws IOException, SofException {

        DiskHistoryConfiguration configuration = new DiskHistoryConfiguration();
        configuration.setFolder(folder.getAbsolutePath());

        final DiskHistoryModule module = new DiskHistoryModule();
        module.configure(configuration, new ProxyServiceDiscovery());

        final MutableInt counter = new MutableInt(0);
        QueueAwareLoggingMessageSender source = new QueueAwareLoggingMessageSender() {

            @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
                HistoricalDataResponse response = (HistoricalDataResponse) message;
                counter.increment(response.getEvents().length);
            }

            @Override public boolean isSendQueueEmpty() {
                return true;
            }
        };

        WorkerThread.everySecondDaemon("Stats", new Runnable() {
            @Override public void run() {
                System.out.println(NumberFormat.getInstance().format(counter.value));
            }
        });

        Stopwatch start = Stopwatch.start("Full scan");
        module.handleDataRequestStreaming(new HistoricalDataRequest(TimeUtils.parseTime("1/3/2014 00:00:00"),
                                                                    TimeUtils.parseTime("28/3/2014 00:00:00")),
                                          source);
        start.stopAndDump();
        System.out.println(counter);

    }

    private void validateReadPerformance(File folder) throws IOException, SofException {
        DiskHistoryConfiguration configuration = new DiskHistoryConfiguration();
        configuration.setFolder(folder.getAbsolutePath());

        final DiskHistoryModule module = new DiskHistoryModule();
        module.configure(configuration, new ProxyServiceDiscovery());

        module.dumpIndex();

        Stopwatch start = Stopwatch.start("1 minute - start");
        HistoricalDataResponse response = module.handleDataRequest(new HistoricalDataRequest(TimeUtils.parseTime("1/3/2014 00:00:00"),
                                                                                             TimeUtils.parseTime("1/3/2014 00:01:00")));
        System.out.println(response.getEvents().length);
        start.stopAndDump();

        start = Stopwatch.start("1 minute - middle");
        response = module.handleDataRequest(new HistoricalDataRequest(TimeUtils.parseTime("15/3/2014 00:00:00"),
                                                                      TimeUtils.parseTime("15/3/2014 00:01:00")));
        System.out.println(response.getEvents().length);
        start.stopAndDump();

        start = Stopwatch.start("1 minute - end");
        response = module.handleDataRequest(new HistoricalDataRequest(TimeUtils.parseTime("27/3/2014 23:00:00"),
                                                                      TimeUtils.parseTime("27/3/2014 23:01:00")));
        System.out.println(response.getEvents().length);
        start.stopAndDump();

        start = Stopwatch.start("30 minutes - middle");
        response = module.handleDataRequest(new HistoricalDataRequest(TimeUtils.parseTime("27/3/2014 23:00:00"),
                                                                      TimeUtils.parseTime("27/3/2014 23:30:00")));
        System.out.println(response.getEvents().length);
        start.stopAndDump();

    }

    private void generate(File folder) {

        DiskHistoryConfiguration configuration = new DiskHistoryConfiguration();
        configuration.setFolder(folder.getAbsolutePath());
        configuration.setBlockSize("10 MB");
        configuration.setFileSizeLimit("128 MB");
        configuration.setTotalFileSizeLimit("1 GB");
        configuration.setMaximumFlushInterval("1 seconds");
        configuration.setUseEventTimes(true);
        
        final DiskHistoryModule dataModule = new DiskHistoryModule();
        dataModule.configure(configuration, new ProxyServiceDiscovery());

        DiskHistoryIndexConfiguration indexConfiguration = new DiskHistoryIndexConfiguration();
        indexConfiguration.setFolder(folder.getAbsolutePath());
        indexConfiguration.setBlockSize("100 K");
        indexConfiguration.setFileSizeLimit("1 MB");
        indexConfiguration.setTotalFileSizeLimit("100 MB");
        indexConfiguration.setMaximumFlushInterval("1 seconds");
        indexConfiguration.setTriggerFromEventTimes(true);
        final DiskHistoryIndexModule indexModule = new DiskHistoryIndexModule();
        indexModule.configure(indexConfiguration, new ProxyServiceDiscovery());

        dataModule.start();
        indexModule.start();

        TimeBasedGenerator generator = new TimeBasedGenerator();
        generator.getEventMultiplexer().addDestination(new Destination<LogEvent>() {
            @Override public void send(LogEvent t) {
                indexModule.send(t);
                dataModule.send(t);
            }
        });

        generator.startStatsThread();
        // generator.generate(TimeUtils.parseTime("15/2/2014 00:00:00"),
        // TimeUtils.parseTime("28/3/2014 00:00:00"));

        generator.generate(TimeUtils.parseTime("1/3/2014 00:00:00"), TimeUtils.parseTime("2/3/2014 00:00:00"));

        indexModule.stop();

    }
}
