package com.logginghub.logging.servers

import com.logginghub.logging.hub.configuration.SocketHubConfiguration
import com.logginghub.logging.listeners.LoggingMessageListener
import com.logginghub.logging.messages.ClearEventsMessage
import com.logginghub.logging.messages.LoggingMessage
import com.logginghub.logging.messaging.SocketClient
import com.logginghub.utils.Bucket
import com.logginghub.utils.NetUtils
import spock.lang.Specification

class TestClearMessage extends Specification {

    def "test clear events broadcast"() {

        when: "We configure a hub on a free port"
        int port = NetUtils.findFreePort();

        SocketHubConfiguration configuration= new SocketHubConfiguration();
        configuration.setAllowClearEvents(true);

        SocketHub hub = new SocketHub();
        hub.configure(configuration, null);
        hub.setPort(port);
        hub.start();
        hub.waitUntilBound();

        and: "Connect two clients to it"
        SocketClient clientA = new SocketClient("ClientA");
        clientA.addConnectionPoint(new InetSocketAddress(hub.getPort()));
        clientA.setAutoSubscribe(true);
        clientA.connect();

        SocketClient clientB = new SocketClient("ClientB");
        clientB.addConnectionPoint(new InetSocketAddress(hub.getPort()));
        clientB.setAutoSubscribe(true);
        clientB.connect();

        and: "We wire up two message buckets to the clients"
        final Bucket<LoggingMessage> bucketA = new Bucket<LoggingMessage>();
        clientA.getConnector().addLoggingMessageListener(new LoggingMessageListener() {
            @Override
            void onNewLoggingMessage(LoggingMessage message) {
                bucketA.add(message);
            }
        });

        final Bucket<LoggingMessage> bucketB = new Bucket<LoggingMessage>();
        clientB.getConnector().addLoggingMessageListener(new LoggingMessageListener() {
            @Override
            void onNewLoggingMessage(LoggingMessage message) {
                bucketB.add(message);
            }
        });

        and: "Client A sends a clear message to the hub"
        clientA.send(new ClearEventsMessage());

        then: "Client B receives the clear message"
        bucketB.waitForMessages(1);
        bucketB.get(0) instanceof ClearEventsMessage;

        and: "Client A also receives the clear message even though they sent it"
        bucketA.waitForMessages(1);
        bucketA.get(0) instanceof ClearEventsMessage;

        cleanup:
        clientA.close();
        clientB.close();
        hub.close();

    }

}
