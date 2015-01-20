package com.logginghub.logging.utils;

import java.net.InetSocketAddress;

import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.utils.Destination;
import com.logginghub.utils.VLPorts;

public class RootHubChannelConsoleListener {

    public static void main(String[] args) {

        String host = "localhost";
        int port = VLPorts.getSocketHubDefaultPort();
        String channel = "";
        
        if(args.length > 0) {
            host = args[0];
            
            if(args.length > 1) {
                port = Integer.parseInt(args[1]);
                
                if(args.length > 2) {
                    channel = args[3];
                }
            }
            
        }else{
            System.out.println("Usage : ConsoleListener <host> <port> <channel>");
            System.out.println("No values provided - falling back to defaults : " + host + ":" + port + " channels='" + channel + "'");
        }


        SocketClient client = new SocketClient();
        client.addConnectionPoint(new InetSocketAddress(host, port));

        client.subscribe("", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                System.out.println(t);
            }
        });

        SocketClientManager manager = new SocketClientManager(client);
        manager.start();

    }

}
