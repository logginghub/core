package com.logginghub.messaging;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.directives.ResponseMessage;
import com.logginghub.messaging.directives.SubscribeMessage;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.messaging.netty.Level2Messaging;
import com.logginghub.messaging.netty.MessageListener;
import com.logginghub.messaging.netty.WrappedMessageListener;
import com.logginghub.utils.FactoryMapDecorator;
import com.logginghub.utils.Timeout;
import com.logginghub.utils.logging.Logger;

public class Level2AsyncClient extends Level1AsyncClient implements Level2Messaging {

    private static final Logger logger = Logger.getLoggerFor(Level2AsyncClient.class);

    private Level2Helper helper;

    private RequestResponseController requestResponseController = new RequestResponseController();

    private List<WrappedMessageListener> wrappedMessageListeners = new CopyOnWriteArrayList<WrappedMessageListener>();

    private FactoryMapDecorator<String, List<MessageListener>> channelListeners = new FactoryMapDecorator<String, List<MessageListener>>(new HashMap<String, List<MessageListener>>()) {
        @Override protected List<MessageListener> createNewValue(String key) {
            return new CopyOnWriteArrayList<MessageListener>();
        }
    };

    public Level2AsyncClient() {
        super.addMessageListener(requestResponseController);

        // Add a message listener to service our per-channel message listeners
        super.addMessageListener(new MessageListener() {
            public <T> void onNewMessage(Object message, Level1MessageSender sender) {

                if (message instanceof MessageWrapper) {
                    MessageWrapper messageWrapper = (MessageWrapper) message;
                    String destinationChannel = messageWrapper.getDeliverToChannel();

                    ClientRequestContext requestContext = new ClientRequestContext(messageWrapper, sender);

                    for (WrappedMessageListener wrappedMessageListener : wrappedMessageListeners) {
                        wrappedMessageListener.onNewMessage(messageWrapper, requestContext, sender);
                    }

                    List<MessageListener> listeners = channelListeners.getOnlyIfExists(destinationChannel);
                    Object payload = messageWrapper.getPayload();
                    if (listeners != null && listeners.size() > 0) {
                        logger.debug("Message received for channel '{}', dispatching payload class '{}' to {} listeners ", destinationChannel, payload.getClass().getSimpleName(), listeners.size());
                        for (MessageListener messageListener : listeners) {
                            messageListener.onNewMessage(payload, sender);
                        }
                    }

                    helper.dispatchToLocalListeners(requestContext, sender, messageWrapper);
                }
            }
        });

        helper = new Level2Helper(this);
    }

    public Level2AsyncClient(String string) {
        this();
        setName(string);
    }

    @Override protected void addMessage(Object message) {
        // Disable the default level 1 option of just adding the event into a never ending bucket.
    }

    @Override public void setDefaultTimeout(long timeoutTime, TimeUnit timeoutUnits) {
        super.setDefaultTimeout(timeoutTime, timeoutUnits);
        requestResponseController.setTimeout(new Timeout(timeoutTime, timeoutUnits));
    }

    public void addWrappedMessageListener(WrappedMessageListener wrappedMessageListener) {
        wrappedMessageListeners.add(wrappedMessageListener);
    }

    public void removeWrappedMessageListener(WrappedMessageListener wrappedMessageListener) {
        wrappedMessageListeners.remove(wrappedMessageListener);
    }

    public void addMessageListener(final MessageListener listener) {

        // Decorate the basic message listener with one that understands message
        // wrappers
        super.addMessageListener(new MessageListener() {
            public <T> void onNewMessage(Object message, final Level1MessageSender sender) {

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
                                outgoingMessageWrapper.setDeliverToChannel(incommingMessageWrapper.getReplyToChannel());
                                outgoingMessageWrapper.setReplyToChannel(incommingMessageWrapper.getDeliverToChannel());
                                sender.send(outgoingMessageWrapper);
                            }
                        }
                    };

                    listener.onNewMessage(payload, decoratingSender);

                }
                else {
                    listener.onNewMessage(message, sender);
                }

            }
        });
    }

    /**
     * Send a request-response message to the server and be notified of when the message was
     * actually sent, and then when the corresponding response arrives.
     * 
     * @param object
     * @param notification
     * @param responseListener
     */
    public <T> void sendRequest(Object object, AsycNotification notification, ResponseListener<T> responseListener) {
        int responseID = requestResponseController.registerRequest(responseListener);
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setResponseID(responseID);
        wrapper.setPayload(object);
        send(wrapper, notification);
    }

    public String getHomeChannel() {
        return helper.getHomeChannel();
    }

    public RequestNotification<ResponseMessage> setHomeChannel(String homeChannel) {
        helper.setHomeChannel(homeChannel);
        return subscribe(homeChannel);
    }

    public <T> RequestNotification<T> sendRequest(Object payload, Class<T> responseClass) {
        RequestNotification<T> notification = new RequestNotification<T>(responseClass);
        int responseID = requestResponseController.registerRequest(notification);
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setResponseID(responseID);
        wrapper.setPayload(payload);
        wrapper.setReplyToChannel(getHomeChannel());
        send(wrapper, notification);
        return notification;
    }

    public <T> RequestNotification<T> sendRequest(String remoteNode, String remoteChannel, String localNode, String localChannel, Object payload, Class<T> responseClass) {
        RequestNotification<T> notification = new RequestNotification<T>(responseClass);
        int responseID = requestResponseController.registerRequest(notification);
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setResponseID(responseID);
        wrapper.setPayload(payload);

        wrapper.setDeliverToChannel(remoteNode);
        wrapper.setDeliverToLocalChannel(remoteChannel);

        wrapper.setReplyToChannel(localNode);
        wrapper.setReplyToLocalChannel(localChannel);

        send(wrapper, notification);
        return notification;
    }

    public <T> RequestNotification<T> sendRequest(String channel, String localChannel, Object payload, Class<T> c) {
        RequestNotification<T> notification = new RequestNotification<T>(c);
        int responseID = requestResponseController.registerRequest(notification);
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setResponseID(responseID);
        wrapper.setPayload(payload);
        wrapper.setDeliverToChannel(channel);
        wrapper.setReplyToChannel(getHomeChannel());
        wrapper.setDeliverToLocalChannel(localChannel);
        send(wrapper, notification);
        return notification;
    }

    public <T> void sendRequest(String deliverToChannel, Object object, AsycNotification notification, ResponseListener<T> responseListener) {
        int responseID = requestResponseController.registerRequest(responseListener);
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setDeliverToChannel(deliverToChannel);
        wrapper.setReplyToChannel(getHomeChannel());
        wrapper.setResponseID(responseID);
        wrapper.setPayload(object);
        send(wrapper, notification);
    }

    public <T> RequestNotification<T> sendRequest(String channel, Object payload) {
        RequestNotification<T> notification = new RequestNotification<T>();
        int responseID = requestResponseController.registerRequest(notification);
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setResponseID(responseID);
        wrapper.setPayload(payload);
        wrapper.setDeliverToChannel(channel);
        wrapper.setReplyToChannel(getHomeChannel());
        send(wrapper, notification);
        return notification;
    }

    public <T> RequestNotification<T> sendRequest(Object payload) {
        RequestNotification<T> notification = new RequestNotification<T>();
        int responseID = requestResponseController.registerRequest(notification);
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setResponseID(responseID);
        wrapper.setPayload(payload);
        send(wrapper, notification);
        return notification;
    }

    public void subscribe(String channel, AsycNotification notification, ResponseListener<ResponseMessage> responseListener) {
        int responseID = requestResponseController.registerRequest(responseListener);
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setResponseID(responseID);
        wrapper.setPayload(new SubscribeMessage(channel));
        send(wrapper, notification);
    }

    public void send(String channel, Object payload, AsycNotification notification) {
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setDeliverToChannel(channel);
        wrapper.setPayload(payload);
        send(wrapper, notification);
    }

    public Notification connect() {
        Notification notification = new Notification();
        connect(notification);
        return notification;
    }

    public RequestNotification<ResponseMessage> subscribe(String channel) {
        RequestNotification<ResponseMessage> notification = new RequestNotification<ResponseMessage>();
        subscribe(channel, notification, notification);
        return notification;
    }

    public Notification send(Object message) {
        Notification notification = new Notification();
        send(message, notification);
        return notification;
    }

    public Notification send(String deliverToChannel, Object message) {
        Notification notification = new Notification();
        send(deliverToChannel, message, notification);
        return notification;
    }

    public void removeMessageListener(String channel, MessageListener listener) {
        channelListeners.get(channel).remove(listener);
    }

    public void addMessageListener(String channel, MessageListener listener) {
        channelListeners.get(channel).add(listener);
    }

    /**
     * Sends an object payload and automatically wraps it in a response wrapper if the original
     * message was wrapped itself.
     * 
     * @param originalMessage
     * @param responseMessage
     * @param sender
     */
    public void sendResponse(Object originalMessage, Object responseMessage) {
        if (originalMessage instanceof MessageWrapper) {
            if (responseMessage instanceof MessageWrapper) {
                // Its already wrapped
                send(responseMessage);
            }
            else {
                MessageWrapper incommingMessageWrapper = (MessageWrapper) originalMessage;
                MessageWrapper outgoingMessageWrapper = new MessageWrapper(incommingMessageWrapper.getReplyToChannel(), incommingMessageWrapper.getDeliverToChannel(), responseMessage);
                outgoingMessageWrapper.setResponseID(incommingMessageWrapper.getResponseID());
                send(outgoingMessageWrapper);
            }
        }
        else {
            send(responseMessage);
        }
    }

    /**
     * Nicer name for add local channel listener? Use this to bind listeners to specific local
     * channels - ie to provide services
     * 
     * @param nodeChannel
     * @param localChannel
     * @param wrappedMessageListener
     */
    public void bindToChannel(String nodeChannel, String localChannel, WrappedMessageListener wrappedMessageListener) {
        addLocalChannelListener(nodeChannel, localChannel, wrappedMessageListener);
    }

    public void addLocalChannelListener(String localNode, String localChannel, WrappedMessageListener wrappedMessageListener) {
        helper.addLocalChannelListener(localNode, localChannel, wrappedMessageListener);
    }

    public void removeLocalChannelListener(String localNode, String localChannel, WrappedMessageListener wrappedMessageListener) {
        helper.addLocalChannelListener(localNode, localChannel, wrappedMessageListener);
    }

    public Level2Helper getHelper() {
        return helper;
    }

}
