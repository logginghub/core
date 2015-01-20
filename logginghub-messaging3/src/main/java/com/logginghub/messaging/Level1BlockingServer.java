package com.logginghub.messaging;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.logginghub.messaging.netty.IntegerHeaderFrameDecoder;
import com.logginghub.messaging.netty.IntegerHeaderFrameEncoder;
import com.logginghub.messaging.netty.ObjectDecoder;
import com.logginghub.messaging.netty.ObjectEncoder;
import com.logginghub.messaging.netty.ServerHandler;
import com.logginghub.messaging.netty.ServerMessageListener;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.logging.Logger;

public class Level1BlockingServer implements Closeable {

    private static final Logger logger = Logger.getLoggerFor(Level1BlockingServer.class);
    private ServerHandler serverHandler = new ServerHandler();
    protected String name = "Messaging3-BlockingServer";
    private int port;

    private ServerBootstrap bootstrap;
    private Channel channel;

    public Level1BlockingServer() {
        randomisePort();
        setName("Messaging3-BlockingServer");
    }

    public Level1BlockingServer(int port) {
        this.port = port;
    }

    public void randomisePort() {
        port = NetUtils.findFreePort();
    }
    
    public void setName(String name) {
        this.name = name;
        serverHandler.setName(name);
        logger.setThreadContextOverride(name);
    }

    public void addListener(ServerMessageListener listener) {
        serverHandler.addListener(listener);
    }

    public void removeListener(ServerMessageListener listener) {
        serverHandler.removeListener(listener);
    }
    
    public void bind() {

        ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        bootstrap = new ServerBootstrap(factory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                return Channels.pipeline(new IntegerHeaderFrameDecoder(), new ObjectDecoder(), new IntegerHeaderFrameEncoder(), new ObjectEncoder(), serverHandler);
            }
        });

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);

        InetSocketAddress address = new InetSocketAddress(port);
        logger.trace("Attmempting to bind on {}...", address);
        channel = bootstrap.bind(address);
        logger.debug("[{}] Socket bound to {}", name, address);
    }

    public ServerHandler getServerHandler() {
        return serverHandler;
    }

    public <T> T receiveNext() {
        return serverHandler.receiveNext();
    }

    public void close() {
        channel.close();
    }

    public int getPort() {
        return port;
    }

}
