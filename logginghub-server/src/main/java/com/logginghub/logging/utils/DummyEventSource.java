package com.logginghub.logging.utils;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LoggingPorts;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.repository.processors.ProcessorTester;
import com.logginghub.utils.ThreadUtils;

public class DummyEventSource {

    public static void main(String[] args) {

        ExecutorService pool = Executors.newCachedThreadPool();

        int threads = 1;
        int delay = 1;
        
        if(args.length == 2){
            threads = Integer.parseInt(args[0]);
            delay = Integer.parseInt(args[1]);
        }

        final List<DefaultLogEvent> list = ProcessorTester.createLogEventsFromResource("/testevents/series1.csv");
        
        final int finalDelay = delay;
        
        for (int i = 0; i < threads; i++) {
            pool.execute(new Runnable() {
                public void run() {
                    
                    SocketClient client = new SocketClient();
                    client.addConnectionPoint(new InetSocketAddress(LoggingPorts.getSocketHubDefaultPort()));
                    client.setAutoSubscribe(false);
                    
                    int index = 0;
                    while(true){
                        try {
                            client.send(new LogEventMessage(list.get(index)));
                        }
                        catch (LoggingMessageSenderException e) {
                            e.printStackTrace();
                        }
                        
                        ThreadUtils.sleep(finalDelay);
                        index++;
                        if(index == list.size()){
                            index = 0;
                        }
                    }
                }
            });
        }
        
        System.out.println("Threads started...");

    }
}
