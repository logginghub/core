package com.logginghub.messaging2.kryo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.logginghub.messaging2.ClassRegisterer;
import com.logginghub.messaging2.Hub;
import com.logginghub.messaging2.MessageSender;
import com.logginghub.messaging2.api.ConnectionListener;
import com.logginghub.messaging2.api.Message;
import com.logginghub.messaging2.api.MessageListener;
import com.logginghub.messaging2.api.MessagingInterface;
import com.logginghub.messaging2.messages.BasicMessage;
import com.logginghub.messaging2.messages.RequestMessage;
import com.logginghub.messaging2.messages.ResponseMessage;
import com.logginghub.utils.EnvironmentProperties;
import com.logginghub.utils.ExceptionHandler;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.WorkerThread;

public class KryoClient implements MessagingInterface, MessageSender {

    private List<InetSocketAddress> addresses = new ArrayList<InetSocketAddress>();
    private Client client;
    private CountDownLatch connectedLatch = new CountDownLatch(1);
    private int defaultRequestTime = 5;
    private TimeUnit defaultRequestUnits = TimeUnit.SECONDS;
    private final String clientID;
    private List<MessageListener> messageListeners = new CopyOnWriteArrayList<MessageListener>();
    private List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<ConnectionListener>();

    private ExceptionHandler asyncExceptionHandler = new ExceptionHandler() {
        public void handleException(String action, Throwable t) {}
    };

    private Set<String> openSubscriptions = new HashSet<String>();

    private AtomicInteger nextRequestID = new AtomicInteger();
    private int timeout = EnvironmentProperties.getInteger("kryoClient.connectionTimeout", 10000);

    // James - we've got a real issue with these buffers, for some reason the
    // kryo clients aren't being finalized when I run this through eclipse/junit
    // in a repeating look. Can't figure out why, but it means these buffers
    // have to stay small for the repeating tests to pass :(
    // private int writeBufferSize = 1 * 1024 * 1024;
    // private int objectBufferSize = 1 * 1024 * 1024;

    private final static int writeBufferSize = Integer.getInteger("kryo.writebuffersize", 1 * 1024 * 1024);
    private final static int objectBufferSize = Integer.getInteger("kryo.objectbuffersize", 1 * 1024 * 1024);

    private DispatchingListener listener;
    private boolean autoReconnect = false;
    private Set<Integer> responseIDsNotToBeSentToGobalListeners = new HashSet<Integer>();
    private WorkerThread reconnectionTimer;
    private volatile boolean isStopped = true;

    public KryoClient(String clientID) {
        this(clientID, new ClassRegisterer[] {});
    }

    public KryoClient(String clientID, ClassRegisterer... registerers) {
        this(clientID, writeBufferSize, objectBufferSize, registerers);
    }

    public KryoClient(String clientID, int writeBufferSize, int objectBufferSize, ClassRegisterer... registerers) {
        this.clientID = clientID;

        client = new Client(writeBufferSize, objectBufferSize);

        KryoHelper.registerClasses(client.getKryo());
        for (ClassRegisterer classRegisterer : registerers) {
            classRegisterer.registerClasses(getKryo());
        }

        listener = new DispatchingListener(new Listener() {
            @Override public void connected(Connection connection) {
                onConnected(connection);
            }

            @Override public void disconnected(Connection connection) {
                onDisconnected(connection);
            }

            @Override public void received(Connection connection, Object message) {
                onMessageReceived(connection, message);
            }
        }, clientID);
        client.addListener(listener);
    }

    public void addConnectionPoint(Hub hub) {
        if (hub instanceof TCPIPHub) {
            TCPIPHub tcpipHub = (TCPIPHub) hub;
            InetSocketAddress address = tcpipHub.getAddress();
            addresses.add(address);
        }
    }

    public void addConnectionPoints(List<InetSocketAddress> connectionPoints) {
        for (InetSocketAddress inetSocketAddress : connectionPoints) {
            addConnectionPoint(inetSocketAddress);
        }
    }

    public void addConnectionPoints(String connectionPointString, int defaultPort) {
        List<InetSocketAddress> inetSocketAddressList = NetUtils.toInetSocketAddressList(connectionPointString, defaultPort);
        addConnectionPoints(inetSocketAddressList);
    }

    public void addConnectionPoint(InetSocketAddress inetSocketAddress) {
        addresses.add(inetSocketAddress);
    }

    public void addMessageListener(MessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    public void addConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    public void removeConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.remove(connectionListener);
    }

    public int allocateRequestID() {
        final int requestID = nextRequestID.getAndIncrement();
        return requestID;
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void connect() {
        start();

        try {
            boolean await = connectedLatch.await(timeout + 1000, TimeUnit.MILLISECONDS);
            if (!await) {
                throw new RuntimeException("Timed out waiting for the kryo connection");
            }
        }
        catch (InterruptedException e) {}
    }

    public synchronized void stop() {
        isStopped = true;
        if (reconnectionTimer != null) {
            reconnectionTimer.stop();
        }

        client.removeListener(listener);
        client.stop();
                
        // jshaw - a hack, it doesn't look like kyro waits its thread to stop?
        ThreadUtils.sleep(200);
        listener.stop();
    }

    public String getDestinationID() {
        return clientID;
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
            message = new BasicMessage(payload, this.clientID, destinationID);
        }

        client.sendTCP(message);
    }

    @SuppressWarnings("unchecked") public <T> T sendRequest(String destinationID, Object payload) {
        return (T) sendRequest(destinationID, payload, defaultRequestTime, defaultRequestUnits);
    }

    /**
     * Send a request and wait for the response asynchronously
     * 
     * @param destination
     * @param payload
     * @param responseHandler
     */
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
        responseIDsNotToBeSentToGobalListeners.add(requestID);

        final Listener listener = new Listener() {
            @Override public void received(Connection connection, Object object) {
                super.received(connection, object);

                if (object instanceof ResponseMessage) {

                    ResponseMessage responseMessage = (ResponseMessage) object;
                    int responseID = responseMessage.getRequestID();

                    Log.debug(String.format("Received response message '%s' from '%s' (response ID %d)", responseMessage.toString(), responseMessage.getSourceID(), responseID));

                    if (responseID == thisRequestID) {
                        Object responsePayload = responseMessage.getPayload();
                        client.removeListener(this);
                        responseHandler.onResponse((T) responsePayload);
                    }
                }
            }
        };
        client.addListener(listener);

        Log.debug(String.format("Sending request message '%s' to '%s' (request ID %d)", message.toString(), destination, requestID));
        sendMessage(destination, message);
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
        Listener listener = new Listener() {
            @Override public void received(Connection connection, Object object) {
                super.received(connection, object);

                if (object instanceof ResponseMessage) {

                    ResponseMessage responseMessage = (ResponseMessage) object;
                    int responseID = responseMessage.getRequestID();

                    Log.debug(String.format("Received response message '%s' from '%s' (response ID %d)", responseMessage.toString(), responseMessage.getSourceID(), responseID));

                    if (responseID == thisRequestID) {
                        try {
                            resultExchanger.exchange(responseMessage.getPayload());
                        }
                        catch (InterruptedException e) {}
                    }
                }
            }
        };
        client.addListener(listener);

        Log.debug(String.format("Sending request message '%s' to '%s' (request ID %d)", message.toString(), destinationID, requestID));
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
        finally {
            client.removeListener(listener);
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

    public synchronized void start() {
        isStopped = false;
        autoReconnect = false;
        client.setTimeout(3000000);
        client.start();

        connectInternal();
    }

    public void startBackground() {
        isStopped = false;
        autoReconnect = true;
        client.setTimeout(3000000);
        client.start();

        startConnectionMonitor();
    }

    private void startConnectionMonitor() {
        reconnectionTimer = WorkerThread.every("KryoClient-AsyncConnectionTimer", 1, TimeUnit.SECONDS, new Runnable() {
            public void run() {
                if (!client.isConnected()) {
                    try {
                        connectInternal();
                    }
                    catch (RuntimeException e) {
                        // Fine. We'll just try again in a bit.
                    }
                }
            }
        });
    }

    private void connectInternal() {
        Random random = new Random();
        InetSocketAddress inetSocketAddress = addresses.get(random.nextInt(addresses.size()));

        try {
            client.connect(timeout, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        }
        catch (IOException e) {

            if (e.getCause() instanceof ClosedByInterruptException) {
                // Hmm interesting case, not really an issue if we've been asked to
                // stop?
                if (isStopped) {
                    // This is fine
                }
                else {
                    throw new RuntimeException("Failed to create client connection", e);
                }
            }

            asyncExceptionHandler.handleException("Connecting", e);
        }
    }

    public void sendMessage(String destinationID, Message message) {
        message.setSource(this.clientID);
        message.setDestination(destinationID);
        client.sendTCP(message);
    }

    protected void onConnected(Connection connection) {
        connection.sendTCP(new ConnectionMessage(clientID));

        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onConnected();
        }
    }

    /**
     * Subscribe to a new channel, and notify this specific listener when messages are received on that channel.
     * 
     * @param uuidForChannel
     * @param messageListener
     */
    public void subscribe(final String uuidForChannel, final MessageListener messageListener) {

        openSubscriptions.add(uuidForChannel);
        addMessageListener(new MessageListener() {
            public void onNewMessage(Message message) {
                if (message.getDestination().equals(uuidForChannel)) {
                    messageListener.onNewMessage(message);
                }
            }
        });
    }

    /**
     * Subscribe to a new channel. Message notifications will be distributed to existing message listeners.
     * 
     * @param channelID
     */
    public void subscribe(String channelID) {
        client.sendTCP(new SubscribeMessage(channelID));
    }

    public void unsubscribe(String channelID) {
        openSubscriptions.remove(channelID);
        client.sendTCP(new UnsubscribeMessage(channelID));
    }

    private void connected(ConnectedMessage connectedMessage) {
        connectedLatch.countDown();
    }

    protected void onDisconnected(Connection connection) {
        connectedLatch = new CountDownLatch(1);

        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onDisconnected();
        }

        if (autoReconnect) {
            connectInternal();
        }
    }

    protected void onMessageReceived(Connection connection, Object message) {

        if (message instanceof ResponseMessage) {
            ResponseMessage responseMessage = (ResponseMessage) message;
            int requestID = responseMessage.getRequestID();
            if (responseIDsNotToBeSentToGobalListeners.contains(requestID)) {
                // This request is being handled by a dedicated notification
                // listener and shouldn't be sent to the normal 'global'
                // listeners
            }
            else {
                notifyListeners(responseMessage);
            }
        }
        else if (message instanceof BasicMessage) {
            BasicMessage basicMessage = (BasicMessage) message;
            notifyListeners(basicMessage);
        }
        else if (message instanceof ConnectedMessage) {
            ConnectedMessage connectedMessage = (ConnectedMessage) message;
            connected(connectedMessage);
            resubscribe();
        }
    }

    private void resubscribe() {
        for (String channelID : openSubscriptions) {
            subscribe(channelID);
        }
    }

    private void notifyListeners(BasicMessage basicMessage) {
        for (MessageListener messageListener : messageListeners) {
            messageListener.onNewMessage(basicMessage);
        }
    }

    public Kryo getKryo() {
        return client.getKryo();
    }

    public boolean isStopped() {
        return isStopped;
    }

    public void setAsyncExceptionHandler(ExceptionHandler asyncExceptionHandler) {
        this.asyncExceptionHandler = asyncExceptionHandler;
    }

}
