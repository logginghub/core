package com.logginghub.logging.sample;

import java.net.InetSocketAddress;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.utils.Destination;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.sof.SerialisableObject;

public class HistoricalIndexUpdates {

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

        // Subscribe to the history updates channels
        client.subscribe(Channels.historyUpdates, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                SerialisableObject payload = t.getPayload();

                // There shouldn't be anything else published on this channel, but it always pays to
                // double check...
                if (payload instanceof HistoricalIndexResponse) {
                    HistoricalIndexResponse historicalIndexResponse = (HistoricalIndexResponse) payload;

                    // The period updates will only ever contain a single index element
                    HistoricalIndexElement[] elements = historicalIndexResponse.getElements();
                    for (HistoricalIndexElement element : elements) {
                        System.out.println(element);
                        
                        // Acccessing the metadata
                        long time = element.getTime();
                        int totalCount = element.getTotalCount();
                        int infoCount = element.getInfoCount();
                        int warningCount = element.getWarningCount();
                        int severeCount = element.getSevereCount();
                    }
                }
            }
        });

    }

}
