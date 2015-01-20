package com.logginghub.messaging.netty;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.logginghub.utils.Bucket;
import com.logginghub.utils.logging.Logger;

public class ServerHandler extends SimpleChannelHandler implements Level1MessageSender {

    private static final Logger logger = Logger.getLoggerFor(ServerHandler.class);
    private int openConnections = 0;
    private Bucket<Object> receivedMessages = new Bucket<Object>();
    private List<ServerMessageListener> listeners = new CopyOnWriteArrayList<ServerMessageListener>();
    private List<ServerConnectionListener> connectionListeners = new CopyOnWriteArrayList<ServerConnectionListener>();
    private String name = "NettyServerHandler";
    private Channel channel;

    @Override public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        logger.setThreadContext(name);
        channel = e.getChannel();
        logger.debug("[{}] New channel connected remote {} local {}", name, channel.getRemoteAddress(), channel.getLocalAddress());
        openConnections++;
        for (ServerConnectionListener connectionListener : connectionListeners) {
            connectionListener.onNewConnection(this);
        }
    }

    @Override public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        logger.setThreadContext(name);
        logger.debug("[{}] Channel disconnected remote {} local {}", name, e.getChannel().getRemoteAddress(), e.getChannel().getLocalAddress());
        super.channelDisconnected(ctx, e);
        openConnections--;
        for (ServerConnectionListener connectionListener : connectionListeners) {
            connectionListener.onDisconnection(this);
        }
    }

    public int getOpenConnections() {
        return openConnections;
    }

    @Override public String toString() {
        if (channel != null) {
            return channel.toString();
        }
        else {
            return "[channel not set!]";
        }

    }

    @Override public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        logger.setThreadContext(name);
        logger.debug("[{}] Recv from {} : {} : {}", name, e.getRemoteAddress(), e.getMessage().getClass().getSimpleName(), e.getMessage());
        //
        // MessageSender sender = new MessageSender() {
        // public void send(Object message) {
        // logger.debug("[{}] Sent : {}", name, message);
        // e.getChannel().write(message);
        // }
        //
        // public void send(String deliverToChannel, String replyToChannel, Object message) {
        // send(new MessageWrapper(deliverToChannel,replyToChannel, message));
        // }
        //
        // @Override public String toString() {
        // return e.getChannel().getRemoteAddress().toString();
        // }
        // };

        for (ServerMessageListener serverMessageListener : listeners) {
            serverMessageListener.onNewMessage(e.getMessage(), this);
        }

        if (listeners.size() == 0) {
            receivedMessages.add(e.getMessage());
        }
    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {

        boolean downgrade = false;

        if (e.getCause() instanceof IOException && e.getCause().getMessage().contains("An existing connection was forcibly closed by the remote host")) {
            downgrade = true;
        }

        if (downgrade) {
            logger.debug(e.getCause(), "Exception caught ctx={} e={}, closing the connection", ctx);
        }
        else {
            logger.warning(e.getCause(), "Exception caught ctx={} e={}, closing the connection", ctx);
        }
        e.getChannel().close();
    }

    public <T> T receiveNext() {
        receivedMessages.waitForMessages(1);
        @SuppressWarnings("unchecked") T t = (T) receivedMessages.popFirst();
        return t;
    }

    public void addConnectionListener(ServerConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    public void removeConnectionListener(ServerConnectionListener connectionListener) {
        connectionListeners.remove(connectionListener);
    }

    public void addListener(ServerMessageListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ServerMessageListener listener) {
        listeners.add(listener);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void send(final Object message) {
        logger.debug("[{}] Sending to   {} : {} : {}", name, channel.getRemoteAddress(), message.getClass().getSimpleName(), message);
        channel.write(message);

        if (channel.isOpen()) {
            ChannelFuture write = channel.write(message);
            write.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture paramChannelFuture) throws Exception {
                    if (paramChannelFuture.isSuccess()) {
                        logger.debug("[{}] Sent : {}", name, message);
                    }
                    else {
                        logger.warning(paramChannelFuture.getCause(), "[{}] failed to send : {}", name, message);
                    }
                }
            });
        }
        else {
            logger.warning("Object wasn't sent because the channel wasn't open  : {}", message);
        }

    }

    public void send(String deliverToChannel, String replyToChannel, Object message) {
        // TODO : this method needs to be removed from the message sender!!
        throw new NoSuchMethodError("This method shouldn't be here");
    }
}