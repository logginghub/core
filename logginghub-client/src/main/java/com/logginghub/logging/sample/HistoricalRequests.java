package com.logginghub.logging.sample;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.HistoricalDataRequest;
import com.logginghub.logging.messages.HistoricalDataResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.Out;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.VLPorts;

public class HistoricalRequests {

    public static void main(String[] args) throws LoggingMessageSenderException, InterruptedException {

        // Giving the client a name helps if you have multiple clients in one jvm
        SocketClient client = new SocketClient("ExampleClient");

        // Add each hub you want to connect to as a connection point - takes hostnames or ip
        // addresses
        client.addConnectionPoint(new InetSocketAddress("localhost", VLPorts.getSocketHubDefaultPort()));

        // The manager deals with automatically reconnecting to the hub so you dont have to worry
        // about it
        SocketClientManager manager = new SocketClientManager(client);
        manager.start();

        // Construct the history data request message to send to the hub
        HistoricalDataRequest request = new HistoricalDataRequest();

        // We'll request the last 5 minutes of data
        long now = System.currentTimeMillis();
        request.setStart(TimeUtils.before(now, "5 minutes"));
        request.setEnd(now);

        // The request will execute asynchronously in one or more batch updates, so if we want to
        // wait for the response to complete we'll need to coordinate between the threads
        final CountDownLatch latch = new CountDownLatch(1);

        // We can use some counters to track the process of the request
        final MutableInt batches = new MutableInt(0);
        final MutableInt count = new MutableInt(0);

        // We need to bind to the message receiver to pick out the HistoricalDataResponse messages
        client.addLoggingMessageListener(new LoggingMessageListener() {

            @Override public void onNewLoggingMessage(LoggingMessage message) {

                if (message instanceof HistoricalDataResponse) {
                    HistoricalDataResponse response = (HistoricalDataResponse) message;

                    DefaultLogEvent[] events = response.getEvents();

                    for (DefaultLogEvent defaultLogEvent : events) {
                        // This is where you add your code to consume the historical events
                        System.out.println(defaultLogEvent);
                    }

                    count.value += events.length;
                    batches.value++;

                    // The isLastBatch field indidicates when all of the data has been received
                    if (!response.isLastBatch()) {
                        System.out.println("=== more to follow ===");
                    }
                    else {
                        System.out.println("======================");

                        // This is the final batch, so notify the main thread we are done
                        latch.countDown();
                    }
                }

            }

        });

        // Send the request - note that we always register the listener _before_ sending the request
        // to avoid race conditions.
        client.send(request);

        // Block the main thread and wait for the response to arrive
        latch.await();
                
        Out.out("Received {} events in {} batches", count, batches);

        // Clean up
        manager.stop();
        client.close();
    }

}
