package com.logginghub.messaging;

import com.logginghub.messaging.netty.*;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.ConnectionPointManager;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Level1AsyncClient implements Closeable {
    private final Logger logger = Logger.getNewLoggerFor(Level1AsyncClient.class);

    private long connectionRetrySleep = 2000;
    private ConnectionPointManager connectionPointManager = new ConnectionPointManager();
    private ClientHandler clientHandler = new ClientHandler(getSender());
    private ChannelFuture connect;
    private Channel channel;

    private Bucket<Object> receivedMessages = new Bucket<Object>();
    private String name = "Client";

    private TimeUnit timeoutUnits;

    private long timeoutTime;

    private ClientBootstrap bootstrap;

    private WorkerThread connectorThread;
    private ExecutorService bossExecutor;
    private ExecutorService workerExecutor;
    private ChannelFactory factory;

    public Level1AsyncClient() {
        clientHandler.addMessageListener(new MessageListener() {
            public void onNewMessage(Object message, Level1MessageSender sender) {
                logger.debug("[{}] Recv : {} : {}", name, message.getClass().getSimpleName(), message);
                handleMessage(message);
            }
        });
    }

    protected Level1MessageSender getSender() {
        return new Level1MessageSender() {
            public void send(String deliverToChannel, String replyToChannel, Object message) {
                Level1AsyncClient.this.send(message, new Notification());
            }

            public void send(Object message) {
                Level1AsyncClient.this.send(message, new Notification());
            }
        };
    }

    public void addMessageListener(MessageListener clientMessageListener) {
        clientHandler.addMessageListener(clientMessageListener);
    }

    public void removeMessageListener(MessageListener clientMessageListener) {
        clientHandler.removeMessageListener(clientMessageListener);
    }

    public void setName(String name) {
        this.name = name;
        clientHandler.setName(name);
        logger.setThreadContextOverride(name);
    }

    protected void handleMessage(Object message) {
        addMessage(message);
    }

    protected void addMessage(Object message) {
        receivedMessages.add(message);
    }

    public void addConnectionPoint(Level1AsyncServer server) {
        connectionPointManager.addConnectionPoint(new InetSocketAddress("localhost", server.getPort()));
    }

    public void connect(AsycNotification asycNotification) {
        connect(Long.MAX_VALUE, TimeUnit.SECONDS, asycNotification);
    }

    public void connect(long time, TimeUnit units, final AsycNotification asycNotification) {

        bossExecutor = Executors.newCachedThreadPool();
        workerExecutor = Executors.newCachedThreadPool();
        factory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor);

        bootstrap = new ClientBootstrap(factory);

        ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(new IntegerHeaderFrameDecoder(),
                                         new ObjectDecoder(),
                                         new IntegerHeaderFrameEncoder(),
                                         new ObjectEncoder(),
                                         clientHandler);
            }
        };

        bootstrap.setPipelineFactory(pipelineFactory);

        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);

        final long startTime = System.currentTimeMillis();
        final long maxElapsed = units.toMillis(time);

        startConnectionThread(asycNotification, startTime, maxElapsed);
    }

    private void startConnectionThread(final AsycNotification asycNotification,
                                       final long startTime,
                                       final long maxElapsed) {
        connectorThread = new WorkerThread("ConnectorThread") {
            @Override protected void onRun() throws Throwable {
                final InetSocketAddress nextConnectionPoint = connectionPointManager.getNextConnectionPoint();
                logger.trace("Attempting connection to address {}...", nextConnectionPoint);
                connect = bootstrap.connect(nextConnectionPoint);
                connect.awaitUninterruptibly();

                if (connect.isSuccess()) {
                    logger.debug("[{}] Connection established to {}",
                                 name,
                                 connectionPointManager.getCurrentConnectionPoint());
                    channel = connect.getChannel();
                    asycNotification.onSuccess();
                    finished();

                    // TODO : jshaw : I think this code was incorrectly starting multiple connector threads. Thats a crappy way to do reconnections.
                    //                    channel.getCloseFuture().addListener(new ChannelFutureListener() {
                    //                        public void operationComplete(ChannelFuture paramChannelFuture) throws Exception {
                    //                            startConnectionThread(asycNotification, startTime, maxElapsed);
                    //                        }
                    //                    });
                } else {

                    logger.info("Connection attempt to '{}' failed, will retry...", nextConnectionPoint);
                    final long timeNow = System.currentTimeMillis();
                    final long elapsed = timeNow - startTime;
                    if (elapsed >= maxElapsed) {
                        asycNotification.onTimeout();
                        finished();
                    }
                }
            }
        };

        connectorThread.setIterationDelay(connectionRetrySleep);
        connectorThread.start();
    }

    public boolean isConnected() {
        return channel != null && channel.isConnected();
    }

    public void setDefaultTimeout(long timeoutTime, TimeUnit timeoutUnits) {
        this.timeoutTime = timeoutTime;
        this.timeoutUnits = timeoutUnits;
    }

    public void send(final Object object, final AsycNotification asycNotification) {
        logger.trace("[{}] Sending : {}", name, object.getClass().getSimpleName());
        if (channel != null && channel.isOpen()) {
            ChannelFuture write = channel.write(object);
            write.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture paramChannelFuture) throws Exception {
                    if (paramChannelFuture.isSuccess()) {
                        logger.debug("[{}] Sent : {}", name, object);
                        asycNotification.onSuccess();
                    } else {
                        logger.warning(paramChannelFuture.getCause(), "[{}] failed to send : {}", name, object);
                        asycNotification.onFailure(paramChannelFuture.getCause());
                    }
                }
            });
        } else {
            logger.warning("Object wasn't sent because the channel wasn't open  : {}", object);
        }
    }

    public void close() {

        if (connectorThread != null) {
            connectorThread.stop();
        }

        closeAsyc().await();

        if(bootstrap != null) {
            bootstrap.shutdown();
            bootstrap = null;
        }

        if(factory != null) {
            factory.shutdown();
            factory.releaseExternalResources();
            factory = null;
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

    public Notification closeAsyc() {
        Notification notification = new Notification();
        close(notification);
        return notification;
    }

    private void close(final AsycNotification notification) {
        if (connect != null) {
            ChannelFuture closeFuture = connect.getChannel().close();
            closeFuture.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture complete) throws Exception {
                    if (complete.isSuccess()) {
                        logger.debug("[{}] Connection closed", name);
                        notification.onSuccess();
                    } else {
                        logger.warning(complete.getCause(), "[{}] connection failed to close ", name);
                        notification.onFailure(complete.getCause());
                    }
                }
            });
        }
    }

    public <T> T receiveNext() {
        receivedMessages.waitForMessages(1);
        @SuppressWarnings("unchecked") T t = (T) receivedMessages.popFirst();
        return t;
    }

    public void addConnectionPoint(InetSocketAddress inetSocketAddress) {
        connectionPointManager.addConnectionPoint(inetSocketAddress);
    }

    public void addConnectionPoints(List<InetSocketAddress> connectionPoints) {
        for (InetSocketAddress inetSocketAddress : connectionPoints) {
            addConnectionPoint(inetSocketAddress);
        }
    }

    public void addConnectionPoints(String connectionPointString, int port) {
        List<InetSocketAddress> inetSocketAddressList = NetUtils.toInetSocketAddressList(connectionPointString, port);
        addConnectionPoints(inetSocketAddressList);
    }

    public void setConnectionRetrySleep(long millis) {
        this.connectionRetrySleep = millis;
    }
}
