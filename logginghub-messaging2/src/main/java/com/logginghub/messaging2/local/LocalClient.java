package com.logginghub.messaging2.local;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
import com.logginghub.messaging2.Hub;
import com.logginghub.messaging2.api.Message;
import com.logginghub.messaging2.api.MessageListener;
import com.logginghub.messaging2.api.MessagingInterface;
import com.logginghub.messaging2.kryo.ResponseHandler;
import com.logginghub.messaging2.messages.BasicMessage;
import com.logginghub.messaging2.messages.RequestMessage;
import com.logginghub.messaging2.messages.ResponseMessage;
import com.logginghub.utils.NamedThreadFactory;

public class LocalClient implements MessagingInterface {

    private ExecutorService localDispatchers = Executors.newCachedThreadPool(new NamedThreadFactory("LocalClient-worker-"));
    private CountDownLatch connectedLatch = new CountDownLatch(1);
    private int defaultRequestTime = Integer.getInteger("localClient.defaultTimeoutSeconds", 10);
    private TimeUnit defaultRequestUnits = TimeUnit.SECONDS;
    private final String destinationID;

    private List<MessageListener> messageListeners = new CopyOnWriteArrayList<MessageListener>();

    private AtomicInteger nextRequestID = new AtomicInteger();
    private int timeout = 5000;
    private Hub hub;

    public LocalClient(String destinationID) {
        this.destinationID = destinationID;
    }

    public void addConnectionPoint(Hub hub) {
        this.hub = hub;
    }

    public void addMessageListener(MessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    public int allocateRequestID() {
        final int requestID = nextRequestID.getAndIncrement();
        return requestID;
    }

    public void connect() {
        hub.connect(destinationID, new MessageListener() {
            public void onNewMessage(final Message message) {
                localDispatchers.execute(new Runnable() {
                    public void run() {
                        if (message instanceof BasicMessage) {
                            BasicMessage basicMessage = (BasicMessage) message;
                            for (MessageListener messageListener : messageListeners) {
                                messageListener.onNewMessage(basicMessage);
                            }
                        }
                    }
                });
            }
        });
    }

    public void stop() {
        localDispatchers.shutdownNow();
    }

    public void disconnect() {
        hub.disconnect(destinationID);
    }

    public String getDestinationID() {
        return destinationID;
    }

    public void receiveMessage(String sourceID, Message message) {
        if (message instanceof BasicMessage) {
            BasicMessage kryoMessage = (BasicMessage) message;

            for (MessageListener messageListener : messageListeners) {
                messageListener.onNewMessage(kryoMessage);
            }
        }
    }

    public void removeMessageListener(MessageListener messageListener) {
        messageListeners.remove(messageListener);
    }

    public void send(String destinationID, Object payload) {
        Message message;
        if (payload instanceof Message) {
            message = (Message) payload;
        }
        else {
            message = new BasicMessage(payload, this.destinationID, destinationID);
        }

        hub.sendMessage(destinationID, message);
    }

    @SuppressWarnings("unchecked") public <T> T sendRequest(String destinationID, Object payload) {
        return (T) sendRequest(destinationID, payload, defaultRequestTime, defaultRequestUnits);
    }

    public <T> T sendRequest(String destinationID, Object payload, int time, TimeUnit units) {

        int requestID;

        RequestMessage message;
        if (payload instanceof RequestMessage) {
            RequestMessage requestMessage = (RequestMessage) payload;
            message = requestMessage;
            requestID = requestMessage.getRequestID();
        }
        else {
            requestID = nextRequestID.getAndIncrement();
            message = new RequestMessage(requestID, payload);
        }

        final int thisRequestID = requestID;

        final Exchanger<Object> resultExchanger = new Exchanger<Object>();
        addMessageListener(new MessageListener() {

            public void onNewMessage(Message message) {
                if (message instanceof ResponseMessage) {

                    ResponseMessage responseMessage = (ResponseMessage) message;

                    int responseID = responseMessage.getRequestID();
                    if (responseID == thisRequestID) {
                        try {
                            resultExchanger.exchange(responseMessage.getPayload());
                        }
                        catch (InterruptedException e) {}
                    }
                }
            }
        });

        sendMessage(destinationID, message);

        Object result;
        try {
            result = resultExchanger.exchange(null, time, units);
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Interupted wating for response", e);
        }
        catch (TimeoutException e) {
            throw new RuntimeException("Timed out wating for response", e);
        }

        return (T) result;
    }

    public void sendResponse(String destinationID, int requestID, Object responsePayload) {
        ResponseMessage responseMessage = new ResponseMessage(requestID, responsePayload);
        sendMessage(destinationID, responseMessage);
    }

    public void sendResponseMessage(RequestMessage requestMessage, Object payload) {
        sendMessage(requestMessage.getSource(), new ResponseMessage(requestMessage.getRequestID(), payload));
    }

    public void start() {

    }

    private void sendMessage(String destinationID, Message message) {
        message.setSource(this.destinationID);
        message.setDestination(destinationID);
        hub.sendMessage(destinationID, message);
    }

    protected void onDisconnected(Connection connection) {
        connectedLatch = new CountDownLatch(1);
    }

    public <T> void sendRequest(String destination, Object payload, final ResponseHandler<T> responseHandler) {
        int requestID;

        RequestMessage message;
        if (payload instanceof RequestMessage) {
            RequestMessage requestMessage = (RequestMessage) payload;
            message = requestMessage;
            requestID = requestMessage.getRequestID();
        }
        else {
            requestID = nextRequestID.getAndIncrement();
            message = new RequestMessage(requestID, payload);
        }

        final int thisRequestID = requestID;

        final MessageListener messagelistener = new MessageListener() {
            public void onNewMessage(Message message) {
                if (message instanceof ResponseMessage) {

                    ResponseMessage responseMessage = (ResponseMessage) message;
                    int responseID = responseMessage.getRequestID();

                    Log.debug(String.format("Received response message '%s' from '%s' (response ID %d)",
                                            responseMessage.toString(),
                                            responseMessage.getSourceID(),
                                            responseID));

                    if (responseID == thisRequestID) {
                        Object responsePayload = responseMessage.getPayload();
                        removeMessageListener(this);
                        responseHandler.onResponse((T) responsePayload);
                    }
                }
            }
        };
        addMessageListener(messagelistener);

        Log.debug(String.format("Sending request message '%s' to '%s' (request ID %d)", message.toString(), destination, requestID));
        sendMessage(destination, message);
    }

}
