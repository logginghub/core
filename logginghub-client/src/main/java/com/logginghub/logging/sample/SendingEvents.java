package com.logginghub.logging.sample;

import java.net.InetSocketAddress;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.logging.Logger;

public class SendingEvents {

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

        // You can build events using the log event builder to default most of the fields for you
        DefaultLogEvent event = LogEventBuilder.start().setMessage("This is a log message").setLevel(Logger.warning).toLogEvent();

        // Send the events by wrapping them in a message and passing them into the client
        client.send(new LogEventMessage(event));       
    }

}
