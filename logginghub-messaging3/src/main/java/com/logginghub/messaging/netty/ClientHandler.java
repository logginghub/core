package com.logginghub.messaging.netty;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.logginghub.utils.logging.Logger;

public class ClientHandler extends SimpleChannelHandler {

    private static final Logger logger = Logger.getLoggerFor(ClientHandler.class);
    private List<MessageListener> listeners = new CopyOnWriteArrayList<MessageListener>();

    private Level1MessageSender sender;
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public ClientHandler(Level1MessageSender sender) {
        this.sender = sender;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) {
        logger.setThreadContext(name);
        logger.trace("[{}] Recv : {} : {}", name, e.getMessage().getClass().getSimpleName(), e.getMessage());

        Object message = e.getMessage();

        for (MessageListener clientMessageListener : listeners) {
            clientMessageListener.onNewMessage(message, sender);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {

        boolean ignore = false;

        // Ignore connection refused messages, they are expected behaviour so we
        // handle them
        // elsewhere
        if (e.getCause() instanceof ConnectException) {
            if (e.getCause().getMessage().contains("Connection refused: no further information")) {
                ignore = true;
            }
        } else if (e.getCause() instanceof IOException) {
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                if (e.getCause().getMessage().contains("An existing connection was forcibly closed by the remote host")) {
                    logger.info("Connection lost, will retry");
                }
            }
        }

        if (!ignore) {
            logger.info(e.getCause());
        }

        e.getChannel().close();
    }

    public void addMessageListener(MessageListener messageListener) {
        listeners.add(messageListener);
    }

    public void removeMessageListener(MessageListener messageListener) {
        listeners.remove(messageListener);
    }

}