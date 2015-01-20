package com.logginghub.logging.generator.nextgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.RandomRange;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.Stream;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class ProcessingUnit implements StreamListener<Map<String, String>>, Module<ProcessingUnit.ProcessingUnitConfiguration> {

    private ProcessingUnitConfiguration configuration = new ProcessingUnitConfiguration();
    private ExecutorService threadPool;
    private Stream<LogEvent> logEventStream = new Stream<LogEvent>();
    private List<StreamListener<Map<String, String>>> downstream = new ArrayList<StreamListener<Map<String, String>>>();
    private List<StreamListener<Map<String, String>>> dependent = new ArrayList<StreamListener<Map<String, String>>>();

    public static class ProcessingUnitConfiguration {
        public int threads = 5;
        public RandomRange processingTimeRange = new RandomRange(100, 100);
        public RandomRange interprocessDelayTimeRange = new RandomRange(1, 3);
        public String name = "";
    }

    public void onNewItem(Map<String, String> context) {
        processBlocking(context);
    }

    public void addDependent(StreamListener<Map<String, String>> downstream) {
        this.dependent.add(downstream);
    }

    public void addDownstream(StreamListener<Map<String, String>> downstream) {
        this.downstream.add(downstream);
    }

    public ProcessingUnitConfiguration getConfiguration() {
        return configuration;
    }

    public void removeDownstream(StreamListener<String> downstream) {
        this.downstream.remove(downstream);
    }

    public void processNonBlocking(final Map<String, String> context) {

        final Stopwatch sw = Stopwatch.start("");

        threadPool.execute(new Runnable() {
            public void run() {
                process(context);
                sw.stop();
//                DefaultLogEvent logEvent = LogEventBuilder.start()
//                                                          .setMessage("{} client completed successfull in {} ms",
//                                                                      configuration.name,
//                                                                      sw.getDurationMillis())
//                                                          .toLogEvent();
//                logEventStream.onNewItem(logEvent);
            }
        });
    }

    public void processBlocking(final Map<String, String> context) {
        Future<?> future = threadPool.submit(new Runnable() {
            public void run() {
                process(context);
            }
        });

        try {
            future.get();
        }
        catch (Exception e) {
            throw new FormattedRuntimeException("Failed to wait for the result of the future - no idea what state its in now", e);
        }
    }

    public void configure(ProcessingUnitConfiguration configuration, ServiceDiscovery serviceDiscovery) {
        this.configuration = configuration;
    }

    public void start() {
        int threads = configuration.threads;
        final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        threadPool = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS, queue);

//        TimerUtils.every("Stats", 500, TimeUnit.MILLISECONDS, new Runnable() {
//            public void run() {
//                DefaultLogEvent logEvent = LogEventBuilder.start()
//                                                          .setMessage("{} backlog queue size {}", configuration.name, queue.size())
//                                                          .toLogEvent();
//                logEventStream.onNewItem(logEvent);
//            }
//        });

    }

    protected void process(Map<String, String> context) {
        ThreadUtils.sleep((long) configuration.interprocessDelayTimeRange.getRandomValue());
        
        Stopwatch sw = Stopwatch.start("");
        
        long processingTime = getProcessingTime(context);
        
        ThreadUtils.sleep(processingTime);

        if (!downstream.isEmpty()) {
            Stopwatch downstreamTime = Stopwatch.start("Dependent");
            for (StreamListener<Map<String, String>> processingUnit : dependent) {
                processingUnit.onNewItem(context);
            }
            downstreamTime.stop();
            
//            DefaultLogEvent logEvent = LogEventBuilder.start()
//                                                      .setMessage("Time in dependent {} ms for '{}'",
//                                                                  downstreamTime.getDurationMillis(),
//                                                                  configuration.name)
//                                                      .toLogEvent();
//            logEventStream.onNewItem(logEvent);
        }

        sw.stop();

        // TODO : more elements of the message
        // TODO : more information about the request context

        LogEventBuilder builder = LogEventBuilder.start();
        buildEvent(builder, context, sw);
        
        DefaultLogEvent logEvent = builder.toLogEvent(); 
                        //LogEventBuilder.start().setMessage(buildMessage(context, sw)).toLogEvent();
        
        
        logEventStream.onNewItem(logEvent);

        for (StreamListener<Map<String, String>> processingUnit : downstream) {
            processingUnit.onNewItem(context);
        }
    }

    protected long getProcessingTime(Map<String, String> context) {
        return (long) configuration.processingTimeRange.getRandomValue();
    }

    protected void buildEvent(LogEventBuilder builder, Map<String, String> context, Stopwatch sw) {
        builder.setMessage("{} completed successfull in {} ms", context, sw.getDurationMillis());
    }

    public void stop() {}

    public Stream<LogEvent> getLogEventStream() {
        return logEventStream;
    }

}
