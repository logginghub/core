package com.logginghub.logging.generator.nextgen;

import java.net.InetSocketAddress;

import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.utils.StreamListener;

// TODO : delete me once the hub can generate its own test data
public class TwoHubs {

    public static void main(String[] args) throws ConnectorException {
        final SocketClient clientA = new SocketClient("A");
        clientA.addConnectionPoint(new InetSocketAddress("localhost", 58770));
        clientA.setAutoSubscribe(false);
        clientA.setAutoGlobalSubscription(false);
        clientA.connect();
        
        final SocketClient clientB = new SocketClient("A");
        clientB.addConnectionPoint(new InetSocketAddress("localhost", 58772));
        clientB.setAutoSubscribe(false);
        clientB.setAutoGlobalSubscription(false);
        clientB.connect();

        SimulatorEventSource sourceA = new SimulatorEventSource(false, 1, 20);
        SimulatorEventSource sourceB = new SimulatorEventSource(false, 1, 20);

        sourceA.getEventStream().addListener(new StreamListener<Long>() {
            public void onNewItem(Long t) {
                clientA.onNewLogEvent(LogEventBuilder.start().setMessage("Source A event event " + t).toLogEvent());
            }
        });
        
        sourceB.getEventStream().addListener(new StreamListener<Long>() {
            public void onNewItem(Long t) {
                clientB.onNewLogEvent(LogEventBuilder.start().setMessage("Source B event event " + t).toLogEvent());
            }
        });
        
        sourceA.start();
        sourceB.start();

    }
    
}
