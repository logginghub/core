package com.logginghub.messaging;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.directives.ResponseMessage;
import com.logginghub.messaging.directives.SubscribeMessage;
import com.logginghub.messaging.directives.UnsubscribeMessage;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.messaging.netty.ServerHandler;
import com.logginghub.messaging.netty.ServerMessageListener;
import com.logginghub.utils.logging.Logger;

public class Level2BlockingServer extends Level1BlockingServer {

    private static final Logger logger = Logger.getLoggerFor(Level2BlockingServer.class);
    private ConcurrentHashMap<String, Set<Level1MessageSender>> subscribedChannels = new ConcurrentHashMap<String, Set<Level1MessageSender>>();

    public Level2BlockingServer() {
        addListener(new ServerMessageListener() {
            public <T> void onNewMessage(Object message, ServerHandler receivedFrom) {
                logger.trace("Processing message '{}' from channel '{}'...", message, receivedFrom);
                if (message instanceof SubscribeMessage) {
                    SubscribeMessage subscribeMessage = (SubscribeMessage) message;
                    subscribe(subscribeMessage, receivedFrom);
                }
                else if (message instanceof UnsubscribeMessage) {
                    UnsubscribeMessage unsubscribeMessage = (UnsubscribeMessage) message;
                    unsubscribe(unsubscribeMessage, receivedFrom);
                }
                else if (message instanceof MessageWrapper) {
                    MessageWrapper messageWrapper = (MessageWrapper) message;
                    routeMessage(messageWrapper);
                }
                else {
                    logger.trace("... message was not a level 2 directive message, ignoring it");
                }
            }

        });
    }

    protected void routeMessage(MessageWrapper messageWrapper) {
        String channelName = messageWrapper.getDeliverToChannel();
        Set<Level1MessageSender> set = subscribedChannels.get(channelName);
        if (set != null) {
            synchronized (set) {
                for (Level1MessageSender outputChannel : set) {
                    outputChannel.send(messageWrapper);
                }
            }

            logger.debug("[{}] Received and routed message to {} subscribed channels for '{}'", name, set.size(), channelName);
        }
        else {
            logger.debug("No channels were subscribed to '{}'", channelName);
        }
    }

    protected void unsubscribe(UnsubscribeMessage unsubscribeMessage, Level1MessageSender sender) {
        String channelName = unsubscribeMessage.getChannel();
        Set<Level1MessageSender> set = subscribedChannels.get(channelName);
        if (set != null) {
            synchronized (set) {
                set.remove(channelName);
                if (set.isEmpty()) {
                    logger.trace("Subscription set for '{}' is empty, removing it", channelName);
                    subscribedChannels.remove(set);
                }
            }

            logger.debug("Succesfully unsubscribed channel {} from group '{}'", sender, channelName);
            sender.send(new ResponseMessage(true));
        }
    }

    protected void subscribe(SubscribeMessage subscribeMessage, Level1MessageSender sender) {

        String channelName = subscribeMessage.getChannel();
        Set<Level1MessageSender> set;
        synchronized (subscribedChannels) {
            set = subscribedChannels.get(channelName);
            if (set == null) {
                set = new HashSet<Level1MessageSender>();
                subscribedChannels.put(channelName, set);
                logger.trace("First subscription set for '{}', creating", channelName);
            }
        }

        synchronized (set) {
            set.add(sender);
        }

        logger.debug("Succesfully subscribed channel {} to group '{}'", sender, channelName);
        sender.send(new ResponseMessage(true));
    }

}
