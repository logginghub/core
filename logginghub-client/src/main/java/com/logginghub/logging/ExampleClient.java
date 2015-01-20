package com.logginghub.logging;

import java.net.InetSocketAddress;

import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.logeventformatters.SingleLineLogEventTextFormatter;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;

public class ExampleClient {

    public static void main(String[] args) {
        
        SocketClient socketClient = new SocketClient();
        socketClient.addConnectionPoint(new InetSocketAddress("localhost", 58770));

        final SingleLineLogEventTextFormatter formatter = new SingleLineLogEventTextFormatter();
        socketClient.setAutoSubscribe(true);
        socketClient.addLogEventListener(new LogEventListener() {           
            public void onNewLogEvent(LogEvent event) {
                System.out.println(formatter.format(event));
            }
        });
        
        SocketClientManager manager = new SocketClientManager(socketClient);
        manager.start();
        
    }
    
}
