package com.logginghub.messaging2.kryo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.logginghub.messaging2.ClassRegisterer;
import com.logginghub.messaging2.Hub;
import com.logginghub.messaging2.MessageSender;
import com.logginghub.messaging2.api.Message;
import com.logginghub.messaging2.api.MessageListener;
import com.logginghub.messaging2.messages.BasicMessage;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.IntegerStat;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.WorkerThread;

public class KryoHub implements Hub, MessageSender {

    public static final int defaultPortx = 58708;
    public static final int defaultPortForTests = 58709;
    private Server server;

    private FactoryMap<String, List<MessageListener>> subscriptionsByDestinationID = new FactoryMap<String, List<MessageListener>>() {
        private static final long serialVersionUID = 1L;

        @Override protected CopyOnWriteArrayList<MessageListener> createEmptyValue(String key) {
            return new CopyOnWriteArrayList<MessageListener>();
        }
    };
    
    private IntegerStat connectionsCountStat = new IntegerStat("connections", 0);
    private IntegerStat newConnectionsStat = new IntegerStat("new", 0);
    private IntegerStat disconnectionsStat = new IntegerStat("disconnections", 0);
    private IntegerStat messagesReceivedStat = new IntegerStat("recv", 0);
    private IntegerStat messagesSentStat = new IntegerStat("sent", 0);

    private Map<Connection, MessageListener> messageListenersByConnection = new HashMap<Connection, MessageListener>();
    private Map<String, MessageListener> clientsByDestinationID = new HashMap<String, MessageListener>();
    private List<Connection> connectedClients = new ArrayList<Connection>();

    private List<MessageListener> globalMessageListeners = new CopyOnWriteArrayList<MessageListener>();

    // private Map<String, Connection> clientsByDestinationID = new
    // HashMap<String, Connection>();
    // private List<Connection> connectedClients = new ArrayList<Connection>();

    private final ClassRegisterer[] registerers;
    private int writeBufferSize = Integer.getInteger("kryo.writebuffersize", 1 * 1024 * 1024);
    private int objectBufferSize = Integer.getInteger("kryo.objectbuffersize", 1 * 1024 * 1024);

    public KryoHub(ClassRegisterer... registerers) {
        this.registerers = registerers;
        server = new Server(writeBufferSize, objectBufferSize);
        KryoHelper.registerClasses(server.getKryo());
        server.addListener(new Listener() {
            @Override public void connected(Connection connection) {
                onNewConnection(connection);
            }

            @Override public void disconnected(Connection connection) {
                onDisconnection(connection);
            }

            @Override public void received(Connection source, Object message) {
                onMessageReceived(source, message);
            }
        });
        
        setupStats();
    }
    
    public KryoHub(int writeBufferSize, int objectBufferSize, ClassRegisterer... registerers) {
        this.writeBufferSize = writeBufferSize;
        this.objectBufferSize = objectBufferSize;
        this.registerers = registerers;
        server = new Server(writeBufferSize, objectBufferSize);
        KryoHelper.registerClasses(server.getKryo());
        server.addListener(new Listener() {
            @Override public void connected(Connection connection) {
                onNewConnection(connection);
            }

            @Override public void disconnected(Connection connection) {
                onDisconnection(connection);
            }

            @Override public void received(Connection source, Object message) {                
                onMessageReceived(source, message);
            }

        });
        
        setupStats();
    }

    private void setupStats() {
        connectionsCountStat.setIncremental(false);
        newConnectionsStat.setIncremental(true);
        disconnectionsStat.setIncremental(true);
        messagesReceivedStat.setIncremental(true);
        messagesSentStat.setIncremental(true);
    }
    
    public void addGlobalMessageListener(MessageListener messageListener) {
        globalMessageListeners.add(messageListener);
    }

    public void removeGlobalMessageListener(MessageListener messageListener) {
        globalMessageListeners.remove(messageListener);
    }

    public void start() {
        start(defaultPortx);
    }

    private CountDownLatch boundLatch = new CountDownLatch(1);
    private WorkerThread binderThread;

    public void start(final int bindPort) {
        server.start();

        binderThread = WorkerThread.execute("KryoHub-bindThread", new Runnable() {
            public void run() {
                boolean bound = false;
                while (!bound) {
                    try {
                        server.bind(bindPort);
                        bound = true;
                        boundLatch.countDown();
                        onBound();
                    }
                    catch (IOException e) {
                        onBindFailure(bindPort, e);
                        bound = false;
                        ThreadUtils.sleep(200);
                    }
                }
            }
        });

    }

    protected void onBindFailure(int bindPort, IOException e) {
        System.err.println("KryoHub failed to bind to port " + bindPort + " : " + e.getMessage());
    }

    protected void onBound() {}

    public void waitUntilBound() {
        try {
            boundLatch.await();
        }
        catch (InterruptedException e) {
            throw new RuntimeException("Thread was interupted waiting for the socket to bind", e);
        }
    }

    public void stop() {
        
        if(binderThread != null) {
            binderThread.dontRunAgain();
            binderThread.stop();
        }
        
        server.close();
        server.stop();        
    }

    protected void onMessageReceived(final Connection source, Object message) {
        
        messagesReceivedStat.increment();
        
        notifyGlobalListeners(message);

        if (message instanceof ConnectionMessage) {
            String destinationID = ((ConnectionMessage) message).getDestinationID();

            // Wrap the connection in a local message receiver interface so we
            // have one consistent way of dispatching messages to local or
            // remote clients
            MessageListener mri = new MessageListener() {
                public void onNewMessage(Message message) {
                    if (source.sendTCP(message) == 0) {
                        // james - not sure if this is a bad thing or not...
                        // throw new RuntimeException("Failed to send message "
                        // + message +
                        // ", kryo sendTCP method returned zero bytes.");
                    }
                }
            };

            Log.debug("Connection message received from '" + source + "' using destinationID '" + destinationID);

            clientsByDestinationID.put(destinationID, mri);
            messageListenersByConnection.put(source, mri);
            source.sendTCP(new ConnectedMessage());
        }
        else if (message instanceof SubscribeMessage) {
            SubscribeMessage subscribeMessage = (SubscribeMessage) message;
            subscriptionsByDestinationID.get(subscribeMessage.getChannelID()).add(messageListenersByConnection.get(source));
        }
        else if (message instanceof UnsubscribeMessage) {
            UnsubscribeMessage unsubscribeMessage = (UnsubscribeMessage) message;
            subscriptionsByDestinationID.get(unsubscribeMessage.getChannelID()).remove(messageListenersByConnection.get(source));
        }
        else if (message instanceof BasicMessage) {
            BasicMessage basicMessage = (BasicMessage) message;
            routeMessage(basicMessage);
        }
    }

    public void routeMessage(BasicMessage basicMessage) {
        String destinationID = basicMessage.getDestinationID();

        Log.debug("Message received from '" + basicMessage.getSource() + "' to '" + basicMessage.getDestination() + "' type '" + basicMessage.getClass().getSimpleName() + "'");

        // Check point to point
        boolean sent = false;

        MessageListener connection = clientsByDestinationID.get(destinationID);
        if (connection != null) {
            Log.info("Sending message to '" + destinationID + "' from '" + basicMessage.getSourceID() + "'");
            connection.onNewMessage(basicMessage);
            sent = true;
        }
        else {
            // Check broadcast
            List<MessageListener> list = subscriptionsByDestinationID.get(destinationID);
            for (MessageListener messageListener : list) {
                messageListener.onNewMessage(basicMessage);
                sent = true;
            }
        }

        if (!sent) {
            Log.debug("No destination found for message '" + destinationID + "'");
        }
    }

    private void notifyGlobalListeners(Object message) {
        if (message instanceof Message) {
            for (MessageListener messageListener : globalMessageListeners) {
                messageListener.onNewMessage((Message) message);
            }
        }
    }

    protected void onDisconnection(Connection connection) {
        connectionsCountStat.decrement();
        disconnectionsStat.increment();
        
        connectedClients.remove(connection);

        MessageListener messageListener = messageListenersByConnection.remove(connection);
        Collection<List<MessageListener>> values = subscriptionsByDestinationID.values();
        for (List<MessageListener> list : values) {
            list.remove(messageListener);
        }
    }

    protected void onNewConnection(Connection connection) {
        
        newConnectionsStat.increment();
        connectionsCountStat.increment();
        
        if (registerers != null) {
            for (ClassRegisterer classRegisterer : registerers) {
                classRegisterer.registerClasses(connection.getEndPoint().getKryo());
            }
        }

        KryoHelper.registerClasses(connection.getEndPoint().getKryo());
        connectedClients.add(connection);
        // TODO : fix this
        connection.setTimeout(4000000);
    }

    /**
     * Accept a local connection
     */
    public void connect(String destinationID, MessageListener client) {
        Log.info("Local client '" + destinationID + "' connected");
        clientsByDestinationID.put(destinationID, client);
    }

    /**
     * Disconnect a local connection
     */
    public void disconnect(String destinationID) {
        clientsByDestinationID.remove(destinationID);
    }

    /**
     * Send a message into the hub from a local client.
     */
    public void sendMessage(String destination, Message message) {
        messagesSentStat.increment();
        if (message instanceof BasicMessage) {
            BasicMessage basicMessage = (BasicMessage) message;
            String destinationID = basicMessage.getDestinationID();
            MessageListener connection = clientsByDestinationID.get(destinationID);
            if (connection != null) {
                Log.debug("Sending message to '" + destinationID + "' from '" + basicMessage.getSourceID() + "'");
                connection.onNewMessage(basicMessage);
            }
            else {
                Log.debug("No destination found for message '" + destinationID + "'");
            }
        }
        else {
            throw new RuntimeException("Dont know how to send this kind of message: " + message.getClass().getName());
        }
    }

    public void send(String destinationID, String sourceID, Object payload) {
        BasicMessage message = new BasicMessage(payload, sourceID, destinationID);
        sendMessage(destinationID, message);
    }

    public void shutdown() {
        server.stop();
    }

    public int getConnectedClientsCount() {
        return connectedClients.size();
    }

    
    public IntegerStat getConnectionsStat() {
        return newConnectionsStat;
    }
    
    public IntegerStat getDisconnectionsStat() {
        return disconnectionsStat;
    }
    
    public IntegerStat getMessagesReceivedStat() {
        return messagesReceivedStat;
    }
    
    public IntegerStat getConnectionsCountStat() {
        return connectionsCountStat;
    }

    public IntegerStat getMessagesSentStat() {
        return messagesSentStat;
    }
}
