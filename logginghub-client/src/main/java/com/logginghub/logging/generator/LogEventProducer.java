package com.logginghub.logging.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;

public class LogEventProducer {

    private WeightedStringProducer hostProducer;
    private WeightedStringProducer instanceProducer;
    private WeightedStringProducer threadProducer;

    private Map<String, Integer> pids = new HashMap<String, Integer>();
    private Map<String, String> addresses = new HashMap<String, String>();

    private MessageProducer messageProducer;

    public LogEventProducer(String instanceName, int instances, MessageProducer messageProducer) {
        this.messageProducer = messageProducer;

        int hosts = 4;
        int threads = 5;

        String[] hostNames = new String[hosts];
        double[] hostWeights = new double[hosts];

        for (int i = 0; i < hosts; i++) {
            hostNames[i] = "ldn_pdn_" + (i + 1);
            hostWeights[i] = 1;

            addresses.put(hostNames[i], "123.123.123." + i);
        }

        hostWeights[2] = 2;

        // Instances next
        String[] instanceNames = new String[instances];
        double[] instanceWeights = new double[instances];

        for (int i = 0; i < instances; i++) {
            instanceNames[i] = instanceName + (i + 1);
            instanceWeights[i] = 1;
        }

        if (instances >= 2) {
            instanceWeights[2] = 2;
        }

        // Threads last
        String[] threadNames = new String[threads];
        double[] threadWeights = new double[threads];

        for (int i = 0; i < threads; i++) {
            threadNames[i] = "thread-" + i;
            threadWeights[i] = 1;
        }

        threadWeights[2] = 2;

        hostProducer = new WeightedStringProducer(hostNames, hostWeights);
        instanceProducer = new WeightedStringProducer(instanceNames, instanceWeights);
        threadProducer = new WeightedStringProducer(threadNames, threadWeights);

        // Create pids for each combination
        Random random = new Random(0);
        for (int i = 0; i < hostNames.length; i++) {
            for (int j = 0; j < instanceNames.length; j++) {
                String key = createInstanceKey(hostNames[i], instanceNames[j]);
                pids.put(key, Math.abs(random.nextInt(65000)));
            }
        }

    }

    private String createInstanceKey(String hostName, String instanceName) {
        return hostName + ":" + instanceName;
    }

    public DefaultLogEvent produce() {

        String instance = instanceProducer.produce();
        String host = hostProducer.produce();

        DefaultLogEvent logEvent = LogEventBuilder.start()
                                                  .setLevel(Level.INFO.intValue())
                                                  .setMessage(messageProducer.produce())
                                                  .setSourceApplication(instance)
                                                  .setSourceHost(host)
                                                  .setSourceAddress(addresses.get(host))
                                                  .setPid(pids.get(createInstanceKey(host, instance)))
                                                  .setSourceClassName("TradingService")
                                                  .setSourceMethodName("placeTrade")
                                                  .setThreadName(threadProducer.produce())
                                                  .toLogEvent();

        if (logEvent.getSourceHost().endsWith("3")) {
            if (logEvent.getSourceApplication().endsWith("2")) {
                // HACKS!
                String startString = "TradeEntryProcessor completed successfully in ";
                String message = logEvent.getMessage();
                if (message.startsWith(startString)) {
                    int index = startString.length();
                    int endIndex = message.indexOf(" ms", index);

                    String time = message.substring(index, endIndex);
                    double parsed = Double.parseDouble(time);
                    parsed *= 3;

                    String newMessage = String.format("%s%.3f ms%s", startString, parsed, message.substring(endIndex + 3));
//                    System.out.println(newMessage + " **************");
                    logEvent.setMessage(newMessage);

                }
            }
        }

        return logEvent;
    }
}
