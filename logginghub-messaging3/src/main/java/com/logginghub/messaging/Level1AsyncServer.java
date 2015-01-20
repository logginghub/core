package com.logginghub.messaging;

import com.logginghub.messaging.netty.*;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.io.Closeable;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Level1AsyncServer implements Closeable {
    private static final Logger logger = Logger.getLoggerFor(Level1AsyncServer.class);
    // private ServerHandler serverHandler = new ServerHandler();
    protected String name = "Messaging3-Level1AsyncServer";
    private int port;

    private List<ServerMessageListener> messageListeners = new CopyOnWriteArrayList<ServerMessageListener>();
    private List<ServerConnectionListener> connectionListeners = new CopyOnWriteArrayList<ServerConnectionListener>();

    private ServerBootstrap bootstrap;
    private Channel channel;
    private WorkerThread bindThread;
    private ChannelFactory channelFactory;
    private ExecutorService bossExecutor;
    private ExecutorService workerExecutor;

    public Level1AsyncServer() {
        randomisePort();
        setName("Messaging3-Level1AsyncServer");
    }

    public Level1AsyncServer(int port) {
        this.port = port;
    }

    public void randomisePort() {
        port = NetUtils.findFreePort();
    }

    public void setName(String name) {
        this.name = name;
        // serverHandler.setName(name);
        logger.setThreadContextOverride(name);
    }

    public void addMessageListener(ServerMessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(ServerMessageListener listener) {
        messageListeners.remove(listener);
    }

    public void addConnectionListener(ServerConnectionListener listener) {
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(ServerConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    public void bind(long time, TimeUnit units, final AsycNotification asycNotification) {
        bossExecutor = Executors.newCachedThreadPool();
        workerExecutor = Executors.newCachedThreadPool();
        channelFactory = new NioServerSocketChannelFactory(bossExecutor, workerExecutor);
        bootstrap = new ServerBootstrap(channelFactory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {

                // Create a link between the specific channel handler and the global server messaging listeners
                final ServerHandler serverHandler = new ServerHandler();
                serverHandler.addListener(new ServerMessageListener() {
                    public <T> void onNewMessage(Object message, ServerHandler receivedFrom) {
                        for (ServerMessageListener serverMessageListener : messageListeners) {
                            serverMessageListener.onNewMessage(message, receivedFrom);
                        }
                    }
                });

                // And also handle disconnection events
                serverHandler.addConnectionListener(new ServerConnectionListener() {
                    public void onNewConnection(ServerHandler serverHandler) {
                        for (ServerConnectionListener serverConnectionListener : connectionListeners) {
                            serverConnectionListener.onNewConnection(serverHandler);
                        }
                    }

                    public void onDisconnection(ServerHandler serverHandler) {
                        handleDisconnection(serverHandler);
                        for (ServerConnectionListener serverConnectionListener : connectionListeners) {
                            serverConnectionListener.onDisconnection(serverHandler);
                        }
                    }

                    public void onBound(InetSocketAddress address) {
                    }

                    public void onBindFailure(InetSocketAddress address, Exception e) {
                    }
                });

                return Channels.pipeline(new IntegerHeaderFrameDecoder(),
                                         new ObjectDecoder(),
                                         new IntegerHeaderFrameEncoder(),
                                         new ObjectEncoder(),
                                         serverHandler);
            }
        });

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        final InetSocketAddress address = new InetSocketAddress(port);

        final long startTime = System.currentTimeMillis();
        final long maxElapsed = units.toMillis(time);

        bindThread = new WorkerThread("ConnectorThread") {
            @Override protected void onRun() throws Throwable {
                logger.trace("Attmempting to bind on {}...", address);
                try {
                    channel = bootstrap.bind(address);
                    logger.debug("[{}] Socket bound to {}", name, address);
                    fireBound(address);
                    asycNotification.onSuccess();
                    finished();
                } catch (ChannelException e) {
                    fireBindFailure(address, e);
                    Throwable cause = e.getCause();
                    if (cause instanceof BindException) {
                        logger.warning("Socket {} is already bound, will retry...", address);
                    }

                    long timeNow = System.currentTimeMillis();
                    long elapsed = timeNow - startTime;
                    if (elapsed >= maxElapsed) {
                        asycNotification.onTimeout();
                        finished();
                    }
                } catch (Exception e) {
                    fireBindFailure(address, e);
                    asycNotification.onFailure(e);
                    finished();
                }
            }
        };

        bindThread.setIterationDelay(500);
        bindThread.start();
    }

    protected void fireBindFailure(InetSocketAddress address, Exception e) {
        for (ServerConnectionListener serverConnectionListener : connectionListeners) {
            serverConnectionListener.onBindFailure(address, e);
        }
    }

    protected void fireBound(InetSocketAddress address) {
        for (ServerConnectionListener serverConnectionListener : connectionListeners) {
            serverConnectionListener.onBound(address);
        }
    }

    protected void handleDisconnection(ServerHandler serverHandler) {

    }

    public void bind(final AsycNotification asycNotification) {
        bind(Long.MAX_VALUE, TimeUnit.SECONDS, asycNotification);
    }

    public void stop() {
        close();
    }

    // public ServerHandler getServerHandler() {
    // return serverHandler;
    // }

    // public <T> T receiveNext() {
    // return serverHandler.receiveNext();
    // }

    public void close() {

        // If the bind thread is still running, we need to kill it before closing the socket
        if (bindThread != null) {
            bindThread.stop();
            bindThread.join();
            bindThread = null;
        }

        if (channel != null) {
            channel.close().awaitUninterruptibly();
            logger.info("[{}] server channel closed {}", name, channel);
            channel = null;
        }

        if (bootstrap != null) {
            bootstrap.shutdown();
            bootstrap.getFactory().shutdown();
        }

        if (channelFactory != null) {
            channelFactory.shutdown();
            channelFactory = null;
        }

        if (bossExecutor != null) {
            bossExecutor.shutdownNow();
            bossExecutor = null;
        }

        if (workerExecutor != null) {
            workerExecutor.shutdownNow();
            workerExecutor = null;
        }
    }

    public Notification closeAysnc() {
        final Notification notification = new Notification();
        if (channel != null) {
            channel.close().addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture arg0) throws Exception {
                    notification.onSuccess();
                }
            });
            bootstrap.shutdown();
            logger.debug("[{}] server channel closed {}", name, channel);
        } else {
            notification.onSuccess();
        }
        return notification;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
