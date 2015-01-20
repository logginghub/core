package com.logginghub.logging.sample;

import java.net.InetSocketAddress;
import java.util.concurrent.Exchanger;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.HistoricalIndexRequest;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.VLPorts;

public class HistoricalIndexRequests {

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

        // Construct the history index request message to send to the hub
        HistoricalIndexRequest request = new HistoricalIndexRequest();

        // We'll request the last 5 minutes of metadata
        long now = System.currentTimeMillis();
        request.setStart(TimeUtils.before(now, "5 minutes"));
        request.setEnd(now);

        // The request will execute asynchronously, so we can use an exchanger to coordinate between the threads
        final Exchanger<HistoricalIndexResponse> exchanger = new Exchanger<HistoricalIndexResponse>();
        
        // We need to bind to the message receiver to pick out the HistoricalIndexResponse messages
        client.addLoggingMessageListener(new LoggingMessageListener() {
            @Override public void onNewLoggingMessage(LoggingMessage message) {
                if (message instanceof HistoricalIndexResponse) {
                    HistoricalIndexResponse response = (HistoricalIndexResponse) message;
                    try {
                        exchanger.exchange(response);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Send the request - note that we always register the listener _before_ sending the request
        // to avoid race conditions.
        client.send(request);
        
        // Grab the result from the exchanger (once it arrives)
        HistoricalIndexResponse response = exchanger.exchange(null);
        
        // Iterate through the elements
        HistoricalIndexElement[] elements = response.getElements();
        for (HistoricalIndexElement historicalIndexElement : elements) {
            // Add your code here to process the metadata
            System.out.println(historicalIndexElement);
        }
        
        // Clean up
        manager.stop();
        client.close();
    }

}
