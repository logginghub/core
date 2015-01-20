package com.logginghub.logging.servers;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.logginghub.messaging.Level2AsyncServer;
import com.logginghub.messaging.Notification;
import com.logginghub.messaging.netty.ServerConnectionListener;
import com.logginghub.messaging.netty.ServerHandler;
import com.logginghub.utils.Throttler;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.logging.Logger;

/**
 * As I'm going off Kryo pretty quickly, this is a messaging3 implementation of the telemetry hub
 * 
 * @author James
 * 
 */
public class Messaging3TelemetryHub {

    private int port = VLPorts.getTelemetryHubDefaultPort();

    private Level2AsyncServer server;

    private int connectionCount = 0;
    private Throttler throttler = new Throttler(10, TimeUnit.SECONDS);
    private static final Logger logger = Logger.getLoggerFor(Messaging3TelemetryHub.class);

    private Notification boundFuture;

    public Messaging3TelemetryHub() {

    }

    public Level2AsyncServer getServer() {
        return server;
    }

    public void start() {
        server = new Level2AsyncServer("Messaging3TelemetryHub");
        server.setPort(port);
        server.addConnectionListener(new ServerConnectionListener() {

            @Override public void onNewConnection(ServerHandler serverHandler) {
                logger.info("New telemetry connection established from {}", serverHandler);
                connectionCount++;
            }

            @Override public void onDisconnection(ServerHandler serverHandler) {
                connectionCount--;
            }

            @Override public void onBound(InetSocketAddress address) {
                logger.info("Telemetry hub has successfully bound to {}", address);
            }

            @Override public void onBindFailure(InetSocketAddress address, Exception e) {
                if (throttler.isOkToFire()) {
                    logger.info("Telemetry hub has failed to bind to {} : {} - we'll try to bind 5 times every second until it succeeds, but you'll only see this error message once every 10 seconds.",
                                address,
                                e.getMessage());
                }
            }
        });

        boundFuture = server.bind();
    }

    public void setPort(int telemetryPort) {
        this.port = telemetryPort;
    }

    public int getPort() {
        return port;
    }

    public void stop() {
        server.close();
    }

    public int getConnectedClientsCount() {
        return connectionCount;
    }

    public void waitUntilBound() {
        boundFuture.await();
    }

    public void broadcastInternal(String string, Object telemetryData) {
        if (server != null) {
            server.broadcast(string, telemetryData);
        }
    }

}
