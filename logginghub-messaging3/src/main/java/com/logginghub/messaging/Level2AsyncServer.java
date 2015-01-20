package com.logginghub.messaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.directives.ResponseMessage;
import com.logginghub.messaging.directives.SubscribeMessage;
import com.logginghub.messaging.directives.UnsubscribeMessage;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.messaging.netty.Level2Messaging;
import com.logginghub.messaging.netty.RequestContext;
import com.logginghub.messaging.netty.RequestContextServerMessageListener;
import com.logginghub.messaging.netty.RequestMessageContext;
import com.logginghub.messaging.netty.ServerHandler;
import com.logginghub.messaging.netty.ServerMessageListener;
import com.logginghub.messaging.netty.WrappedMessageListener;
import com.logginghub.utils.FactoryMapDecorator;
import com.logginghub.utils.logging.Logger;

public class Level2AsyncServer extends Level1AsyncServer implements ServerMessageListener, Level2ResponseSender, Level2Messaging {

    private static final Logger logger = Logger.getLoggerFor(Level2AsyncServer.class);
    private ConcurrentHashMap<String, Set<ServerHandler>> subscribedChannels = new ConcurrentHashMap<String, Set<ServerHandler>>();
    private FactoryMapDecorator<String, List<WrappedMessageListener>> localChannelListeners = new FactoryMapDecorator<String, List<WrappedMessageListener>>(new HashMap<String, List<WrappedMessageListener>>()) {
        @Override protected List<WrappedMessageListener> createNewValue(String key) {
            return new CopyOnWriteArrayList<WrappedMessageListener>();
        }
    };

    public Level2AsyncServer() {
        super.addMessageListener(this);
    }

    public Level2AsyncServer(String string) {
        setName(string);
        super.addMessageListener(this);
    }

    public Level2AsyncServer(int port, String string) {
        super(port);
        setName(string);
        super.addMessageListener(this);
    }

    public void bindToChannel(String localChannel, WrappedMessageListener wrappedMessageListener) {
        addLocalChannelListener(localChannel, wrappedMessageListener);
    }

    public void addLocalChannelListener(String localChannel, WrappedMessageListener wrappedMessageListener) {
        localChannelListeners.get(localChannel).add(wrappedMessageListener);
    }

    public void removeLocalChannelListener(String localChannel, WrappedMessageListener wrappedMessageListener) {
        localChannelListeners.get(localChannel).remove(wrappedMessageListener);
    }

    @Override protected void handleDisconnection(ServerHandler serverHandler) {
        super.handleDisconnection(serverHandler);

        logger.trace("Handler {} has been disconnected; removing subscriptions", serverHandler);
        // Remove this handler from all subscriptions
        Collection<Entry<String, Set<ServerHandler>>> values = subscribedChannels.entrySet();
        for (Entry<String, Set<ServerHandler>> entry : values) {
            boolean removed = entry.getValue().remove(serverHandler);
            if (removed) {
                logger.trace("Removed from group {}", entry.getKey());
            }
        }
    }

    public boolean isDirective(Object message) {
        boolean isDirective = (message instanceof SubscribeMessage) || (message instanceof UnsubscribeMessage);
        return isDirective;
    }

    public boolean isWrappedMessageIntededForUs(MessageWrapper wrapper) {
        return wrapper.getDeliverToChannel() == null;
    }

    public boolean isWrapped(Object message) {
        return message instanceof MessageWrapper;
    }

    protected Object getActualMessage(Object message) {
        Object actualMessage;
        if (message instanceof MessageWrapper) {
            MessageWrapper messageWrapper = (MessageWrapper) message;
            actualMessage = messageWrapper.getPayload();
        }
        else {
            actualMessage = message;
        }

        return actualMessage;

    }

    public void addListener(final RequestContextServerMessageListener requestContextServerMessageListener) {
        super.addMessageListener(new ServerMessageListener() {
            public <T> void onNewMessage(Object message, final ServerHandler sender) {
                if (message instanceof MessageWrapper) {
                    final MessageWrapper incommingMessageWrapper = (MessageWrapper) message;
                    Object payload = incommingMessageWrapper.getPayload();
                    RequestMessageContext context = new RequestMessageContext(incommingMessageWrapper, Level2AsyncServer.this, sender);
                    requestContextServerMessageListener.onNewMessage(payload, sender, context);
                }
            }
        });
    }

    public void addListener(final ServerMessageListener listener) {

        // Decorate the basic message listener with one that understands message
        // wrappers
        super.addMessageListener(new ServerMessageListener() {
            public <T> void onNewMessage(Object message, final ServerHandler sender) {
                if (message instanceof MessageWrapper) {
                    final MessageWrapper incommingMessageWrapper = (MessageWrapper) message;

                    Object payload = incommingMessageWrapper.getPayload();

                    Level1MessageSender decoratingSender = new Level1MessageSender() {
                        public void send(String deliverToChannel, String replyToChannel, Object message) {
                            MessageWrapper outgoingMessageWrapper = new MessageWrapper(deliverToChannel, replyToChannel, message);
                            outgoingMessageWrapper.setResponseID(incommingMessageWrapper.getResponseID());
                            sender.send(outgoingMessageWrapper);
                        }

                        public void send(Object message) {
                            if (message instanceof MessageWrapper) {
                                sender.send(message);
                            }
                            else {
                                MessageWrapper outgoingMessageWrapper = new MessageWrapper();
                                outgoingMessageWrapper.setPayload(message);
                                outgoingMessageWrapper.setResponseID(incommingMessageWrapper.getResponseID());
                                sender.send(outgoingMessageWrapper);
                            }
                        }
                    };

                    listener.onNewMessage(payload, sender);

                }
                else {
                    listener.onNewMessage(message, sender);
                }
            }
        });
    }

    protected void routeMessage(MessageWrapper messageWrapper) {
        String channelName = messageWrapper.getDeliverToChannel();
        Set<ServerHandler> set = subscribedChannels.get(channelName);
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

    protected void unsubscribe(MessageWrapper originalMessage, UnsubscribeMessage unsubscribeMessage, ServerHandler sender) {
        String channelName = unsubscribeMessage.getChannel();
        Set<ServerHandler> set = subscribedChannels.get(channelName);
        if (set != null) {
            synchronized (set) {
                set.remove(sender);
                if (set.isEmpty()) {
                    logger.trace("Subscription set for '{}' is empty, removing it", channelName);
                    subscribedChannels.remove(set);
                }
            }

            logger.debug("Succesfully unsubscribed channel {} from group '{}'", sender, channelName);

            sendResponse(originalMessage, new ResponseMessage(true), sender);
        }
    }

    protected void subscribe(MessageWrapper originalMessage, SubscribeMessage subscribeMessage, ServerHandler sender) {
        String channelName = subscribeMessage.getChannel();
        subscribe(sender, channelName);
        sendResponse(originalMessage, new ResponseMessage(true), sender);
    }

    public void sendResponse(MessageWrapper originalMessage, Object responseMessage, ServerHandler sender) {
        if (originalMessage instanceof MessageWrapper) {
            if (responseMessage instanceof MessageWrapper) {
                // Its already wrapped
                sender.send(responseMessage);
            }
            else {
                MessageWrapper incommingMessageWrapper = (MessageWrapper) originalMessage;
                MessageWrapper outgoingMessageWrapper = new MessageWrapper(incommingMessageWrapper.getReplyToChannel(), incommingMessageWrapper.getDeliverToChannel(), responseMessage);

                if (incommingMessageWrapper.getReplyToLocalChannel() != null) {
                    outgoingMessageWrapper.setDeliverToLocalChannel(incommingMessageWrapper.getReplyToLocalChannel());
                }

                outgoingMessageWrapper.setResponseID(incommingMessageWrapper.getResponseID());
                sender.send(outgoingMessageWrapper);
            }
        }
        else {
            sender.send(responseMessage);
        }
    }

    public void subscribe(ServerHandler sender, String channelName) {
        Set<ServerHandler> set;
        synchronized (subscribedChannels) {
            set = subscribedChannels.get(channelName);
            if (set == null) {
                set = new HashSet<ServerHandler>();
                subscribedChannels.put(channelName, set);
                logger.trace("First subscription set for '{}', creating", channelName);
            }
        }

        synchronized (set) {
            set.add(sender);
        }

        logger.debug("Succesfully subscribed channel {} to group '{}'", sender, channelName);
    }

    public <T> void onNewMessage(Object message, ServerHandler receivedFrom) {
        if (isWrapped(message)) {
            MessageWrapper messageWrapper = (MessageWrapper) message;
            Object payload = getActualMessage(message);

            if (messageWrapper.getDeliverToLocalChannel() != null) {
                List<WrappedMessageListener> list = localChannelListeners.get(messageWrapper.getDeliverToLocalChannel());
                for (WrappedMessageListener wrappedMessageListener : list) {
                    RequestContext requestContext= new RequestMessageContext(messageWrapper, this, receivedFrom);
                    wrappedMessageListener.onNewMessage(messageWrapper, requestContext, receivedFrom);
                }
            }            
            if (isDirective(payload)) {
                processDirective(messageWrapper, payload, receivedFrom);
            }
            else {
                if (isWrappedMessageIntededForUs(messageWrapper)) {
                    // This is a message to the server itself, hopefully someone
                    // has a listener attached to deal with it...
                }
                else {
                    routeMessage(messageWrapper);
                }
            }
        }
        else {
            if (isDirective(message)) {
                processDirective(null, message, receivedFrom);
            }
            else {
                // Hopefully a message listener will know what to do with this one...
            }
        }
    }

    private void processDirective(MessageWrapper originalMessage, Object payload, ServerHandler sender) {
        logger.trace("Processing message '{}' from channel '{}'...", payload, sender);
        if (payload instanceof SubscribeMessage) {
            SubscribeMessage subscribeMessage = (SubscribeMessage) payload;
            subscribe(originalMessage, subscribeMessage, sender);
        }
        else if (payload instanceof UnsubscribeMessage) {
            UnsubscribeMessage unsubscribeMessage = (UnsubscribeMessage) payload;
            unsubscribe(originalMessage, unsubscribeMessage, sender);
        }
        else if (payload instanceof MessageWrapper) {
            MessageWrapper messageWrapper = (MessageWrapper) payload;
            routeMessage(messageWrapper);
        }
        else {
            logger.trace("... message was not a level 2 directive message, ignoring it");
        }
    }

    public void start() {
        bind();
    }

    public Notification bind() {
        Notification notification = new Notification();
        bind(notification);
        return notification;
    }

    public void broadcast(String channel, Object message) {
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setDeliverToChannel(channel);
        wrapper.setPayload(message);
        routeMessage(wrapper);
    }

    public <T> RequestNotification<T> sendRequest(String remoteNode, String remoteChannel, String localNode, String localChannel, Object payload, Class<T> responseClass) {

        // TODO : this is such a crap implementation - the request ID isn't wired, and the notification object will never fire
        RequestNotification<T> notification = new RequestNotification<T>(responseClass);
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setPayload(payload);
        
        wrapper.setDeliverToChannel(remoteNode);
        wrapper.setDeliverToLocalChannel(remoteChannel);
        
        wrapper.setReplyToChannel(localNode);
        wrapper.setReplyToLocalChannel(localChannel);
        
        routeMessage(wrapper);
        return notification;
    }

    public void addLocalChannelListener(String localNode, String localChannel, WrappedMessageListener wrappedMessageListener) {
        addLocalChannelListener(localChannel, wrappedMessageListener);
    }

    public void removeLocalChannelListener(String localNode, String localChannel, WrappedMessageListener wrappedMessageListener) {
        removeLocalChannelListener(localChannel, wrappedMessageListener);
    }

}
