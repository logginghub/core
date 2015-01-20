package com.logginghub.messaging;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.logginghub.messaging.netty.ClientHandler;
import com.logginghub.messaging.netty.IntegerHeaderFrameDecoder;
import com.logginghub.messaging.netty.IntegerHeaderFrameEncoder;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.messaging.netty.MessageListener;
import com.logginghub.messaging.netty.ObjectDecoder;
import com.logginghub.messaging.netty.ObjectEncoder;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.ConnectionPointManager;
import com.logginghub.utils.logging.Logger;

public class Level1BlockingClient {

    private final Logger logger = Logger.getNewLoggerFor(Level1BlockingClient.class);

    private ConnectionPointManager connectionPointManager = new ConnectionPointManager();
    private ClientHandler clientHandler = new ClientHandler(getSender());
    private ChannelFuture connect;

    private Bucket<Object> receivedMessages = new Bucket<Object>();
    private String name = "Client";

    public Level1BlockingClient() {
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
                Level1BlockingClient.this.send(message);
            }
            public void send(Object message) {
                Level1BlockingClient.this.send(message);
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

    public void addConnectionPoint(Level1BlockingServer server) {
        connectionPointManager.addConnectionPoint(new InetSocketAddress("localhost", server.getPort()));
    }

    public void connect() {

        ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

        ClientBootstrap bootstrap = new ClientBootstrap(factory);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(new IntegerHeaderFrameDecoder(), new ObjectDecoder(), new IntegerHeaderFrameEncoder(), new ObjectEncoder(), clientHandler);
            }
        });

        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);

        InetSocketAddress nextConnectionPoint = connectionPointManager.getNextConnectionPoint();
        logger.trace("Attempting connection to address {}...", nextConnectionPoint);
        connect = bootstrap.connect(nextConnectionPoint);
        ChannelFuture awaitUninterruptibly = connect.awaitUninterruptibly();
        awaitUninterruptibly.getChannel();
        if(!awaitUninterruptibly.isSuccess()){
            throw new RuntimeException(awaitUninterruptibly.getCause());
        }
        
        logger.debug("[{}] Connection established to {}", name, nextConnectionPoint);
    }

    public void setBlockingTimeout(int i, TimeUnit seconds) {}

    public void send(Object object) {
        Channel channel = connect.awaitUninterruptibly().getChannel();
        logger.trace("[{}] Sending : {}", name, object.getClass().getSimpleName());
        if (channel.isOpen()) {
            channel.write(object);
            logger.debug("[{}] Sent : {}", name, object);
        }else{
            logger.warning("Object wasn't sent because the channel wasn't open  : {}", object);   
        }
    }

    public void close() {
        connect.getChannel().getCloseFuture().awaitUninterruptibly();
    }

    public <T> T receiveNext() {
        receivedMessages.waitForMessages(1);
        @SuppressWarnings("unchecked") T t = (T) receivedMessages.popFirst();
        return t;
    }

    public void addConnectionPoint(InetSocketAddress inetSocketAddress) {
        connectionPointManager.addConnectionPoint(inetSocketAddress);
    }

}
