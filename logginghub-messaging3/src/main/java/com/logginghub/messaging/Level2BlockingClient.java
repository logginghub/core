package com.logginghub.messaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.netty.channel.Channel;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.directives.ResponseMessage;
import com.logginghub.messaging.directives.SubscribeMessage;
import com.logginghub.messaging.directives.UnsubscribeMessage;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.messaging.netty.MessageListener;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.FactoryMapDecorator;
import com.logginghub.utils.logging.Logger;

public class Level2BlockingClient extends Level1BlockingClient implements Level1MessageSender {

    private final Logger logger = Logger.getNewLoggerFor(Level2BlockingClient.class);

    public Level2BlockingClient() {

    }

    @Override public void setName(String name) {
        super.setName(name);
        logger.setThreadContextOverride(name);
    }

    protected Level1MessageSender getSender(final Channel channel) {
        return new Level1MessageSender() {
            public void send(String deliverToChannel, String replyToChannel, Object message) {
                Level2BlockingClient.this.send(deliverToChannel, replyToChannel, message);
            }

            public void send(Object message) {
                Level2BlockingClient.this.send(message);
            }
        };
    }

    private Map<String, Bucket<Object>> receivedMessages = new FactoryMapDecorator<String, Bucket<Object>>(new HashMap<String, Bucket<Object>>()) {
        @Override protected Bucket<Object> createNewValue(String key) {
            return new Bucket<Object>();
        }
    };

    private FactoryMapDecorator<String, List<MessageListener>> channelListeners = new FactoryMapDecorator<String, List<MessageListener>>(new HashMap<String, List<MessageListener>>()) {
        @Override protected List<MessageListener> createNewValue(String key) {
            return new CopyOnWriteArrayList<MessageListener>();
        }
    };

    @Override protected void handleMessage(Object message) {
        if (message instanceof MessageWrapper) {
            MessageWrapper messageWrapper = (MessageWrapper) message;

            String destinationChannel = messageWrapper.getDeliverToChannel();

            List<MessageListener> listeners = channelListeners.getOnlyIfExists(destinationChannel);
            Object payload = messageWrapper.getPayload();
            if (listeners != null && listeners.size() > 0) {
                logger.debug("Message received for channel '{}', dispatching payload class '{}' to {} listeners ", destinationChannel, payload.getClass().getSimpleName(), listeners.size());
                for (MessageListener messageListener : listeners) {
                    messageListener.onNewMessage(payload, null);
                }
            }
            else {
                logger.debug("Message received for channel '{}', adding to message with payload class '{}' to bucket", destinationChannel, payload.getClass().getSimpleName());
                receivedMessages.get(destinationChannel).add(payload);
            }

        }
        else {
            addMessage(message);
        }
    }

    public void subscribe(String channel) {
        send(new SubscribeMessage(channel));
        ResponseMessage response = receiveNext();
        if (response.isSuccess()) {
            logger.debug("Client has successfully subscribed to channel '{}'", channel);
        }
        else {
            // TODO : this needs a reason and to throw?
            logger.warning("Client has failed to successfully subscribe to channel '{}'", channel);
        }

    }

    public void unsubscribe(String channel) {
        send(new UnsubscribeMessage(channel));
        receiveNext();
    }

    public void send(String deliverToChannel, String replyToChannel, Object object) {
        send(new MessageWrapper(deliverToChannel, replyToChannel, object));
    }

    public <T> T receiveNext(String channel) {
        if (channel == null || channel.length() == 0) {
            return receiveNext();
        }
        else {
            Bucket<Object> bucket = receivedMessages.get(channel);
            bucket.waitForMessages(1);
            @SuppressWarnings("unchecked") T t = (T) bucket.popFirst();
            t = (T) unwrap(t);
            return t;
        }
    }

    public void addChannelMessageHandler(String channel, MessageListener messageListener) {
        channelListeners.get(channel).add(messageListener);
    }

    public void removeChannelMessageHandler(String channel, MessageListener messageListener) {
        channelListeners.get(channel).remove(messageListener);
    }

    public static Object unwrap(Object message) {
        Object payload;
        if (message instanceof MessageWrapper) {
            MessageWrapper messageWrapper = (MessageWrapper) message;
            payload = messageWrapper.getPayload();
        }
        else {
            payload = message;
        }
        return payload;

    }

}
