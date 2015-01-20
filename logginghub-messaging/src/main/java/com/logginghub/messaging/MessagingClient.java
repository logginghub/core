package com.logginghub.messaging;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logginghub.messaging.Message.Routing;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Ports;
import com.logginghub.utils.WorkerThread;

/**
 * Simple object io socket client. It will continue trying to connect and
 * receive messages until you stop the worker thread.
 * 
 * @author James
 * 
 */
public class MessagingClient extends WorkerThread implements MessagingSender, GenericMessageSenderInterface
{
    private AtomicInteger m_nextMappingID = new AtomicInteger();
    private String name;
    private Runnable executeAfterNextMessage = null;

    private enum MessageAction
    {
        PublishToListeners,
        ConsumeMessage
    }

    // private int m_port;
    // private String m_server;

    // private List<InetSocketAddress> connectionPoints = new
    // ArrayList<InetSocketAddress>();
    private ConnectionPointManager connectionPoints = new ConnectionPointManager();

    public final static int notConnected = -1;
    private int clientID = notConnected;
    private Socket m_socket;
    private ObjectInputStream m_objectInputStream;
    private ObjectOutputStream m_objectOutputStream;
    private long m_reconnectionTimeout = 5000;
    private static Logger logger = Logger.getLogger(MessagingClient.class.getName());
    private Map<Integer, Exchanger<Serializable>> m_requestResponseExchangers = new HashMap<Integer, Exchanger<Serializable>>();
    private long m_requestResponseTimeout = 10000;

    private List<MessagingClientClientListener> m_listeners = new CopyOnWriteArrayList<MessagingClientClientListener>();

    private Map<Status, CountDownLatch> m_statusLatches = new ConcurrentHashMap<Status, CountDownLatch>();

    private Status m_status = Status.Disconnected;
    private Proxy proxy = null;

    public enum Status
    {
        Connecting,
        Connected,
        ConnectedWithClientID,
        Disconnected;
    }

    public MessagingClient()
    {
        super("MessagingClient");
        connectionPoints.setDefaultConnectionPoint(new InetSocketAddress("localhost",
                                                                         Ports.MessagingServer));
    }

    public MessagingClient(String server, int port)
    {
        super("MessagingClient");
        connectionPoints.addConnectionPoint(new InetSocketAddress(server, port));
    }

    @Override public void setName(String name)
    {
        this.name = name;
        super.setName(name + "-ReceiveThread");
    }

    public String getName()
    {
        return name;
    }

    public ConnectionPointManager getConnectionPoints()
    {
        return connectionPoints;
    }

    public void addConnectionPoint(InetSocketAddress connectionPoint)
    {
        connectionPoints.addConnectionPoint(connectionPoint);
    }

    public long getRequestResponseTimeout()
    {
        return m_requestResponseTimeout;
    }

    public void setRequestResponseTimeout(long amount, TimeUnit units)
    {
        m_requestResponseTimeout = units.toMillis(amount);
    }

    public void waitForStatus(Status status)
    {
        CountDownLatch countDownLatch = null;
        synchronized (m_status)
        {
            if (m_status != status)
            {
                countDownLatch = m_statusLatches.get(status);
                if (countDownLatch == null)
                {
                    countDownLatch = new CountDownLatch(1);
                    m_statusLatches.put(status, countDownLatch);
                }
            }
        }

        if (countDownLatch != null)
        {
            try
            {
                countDownLatch.await();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(String.format("Thread interupted waiting for status [%s]",
                                                         status));
            }
        }
    }

    private void changeStatus(Status status)
    {
        Status oldStatus = null;

        synchronized (m_status)
        {
            if (m_status != status)
            {
                oldStatus = m_status;
                m_status = status;

                CountDownLatch latch = m_statusLatches.remove(status);
                if (latch != null)
                {
                    latch.countDown();
                }
            }
        }

        if (oldStatus != null)
        {
            fireStatusChanged(oldStatus, status);
        }
    }

    @Override protected void onRun() throws IOException, ClassNotFoundException
    {
        try
        {
            ensureConnected();
        }
        catch (EOFException e)
        {
            // The socket was closed, this isn't an error
            disconnect();
        }
        catch (IOException e)
        {
            logger.info(String.format("Connection failed, waiting %d ms before we try again",
                                      m_reconnectionTimeout));

            waitForReconnectionTime();
        }

        if (isConnected())
        {
            try
            {
                waitForMessage();
            }
            catch (InterruptedException ie)
            {
                // Faaaaaaaaa
                ie.printStackTrace();
            }
            catch (EOFException e)
            {
                // The socket was closed, this isn't an error
                disconnect();
            }
            catch (SocketException se)
            {
                // TODO : is there a better way of handling this? Can't we do
                // this for all SocketExceptions?
                if (se.getMessage().equals("Socket is closed") ||
                    se.getMessage().equals("socket closed") ||
                    se.getMessage().equals("Connection reset"))
                {
                    // The socket was closed, this isn't an error
                    disconnect();
                }
                else
                {
                    throw se;
                }
            }
            catch (IOException e)
            {
                disconnect();
                throw e;
            }
            catch (ClassNotFoundException e)
            {
                disconnect();
                throw e;
            }
        }
    }

    private void waitForReconnectionTime()
    {
        try
        {
            Thread.sleep(m_reconnectionTimeout);
        }
        catch (InterruptedException e1)
        {}
    }

    private synchronized boolean isConnected()
    {
        return m_socket != null;
    }

    private synchronized void disconnect()
    {
        if (m_socket != null)
        {
            FileUtils.closeQuietly(m_objectInputStream);
            FileUtils.closeQuietly(m_objectOutputStream);
            FileUtils.closeQuietly(m_socket);
        }

        changeStatus(Status.Disconnected);
    }

    @Override protected void beforeStop()
    {
        super.beforeStop();
        disconnect();
    }

    private void waitForMessage() throws IOException, ClassNotFoundException,
                    InterruptedException
    {
        Object object = m_objectInputStream.readObject();
        if (object instanceof Message)
        {
            Message message = (Message) object;
            logger.info(String.format("[%s] Message received [%s] ",
                                      name,
                                      message));

            MessageAction action = handleMessage(message);
            if (action == MessageAction.PublishToListeners)
            {
                fireNewMessage(message);
            }
        }
        else
        {
            throw new RuntimeException(String.format("Object decoded from the stream was not a Message (it was of type [%s]",
                                                     object.getClass()
                                                           .getName()));
        }
    }

    /*
     * public void sendObject(Serializable object) { Message message = new
     * Message(); message.setPayload(object); sendMessage(message); }
     */

    public void sendMessage(Message message)
    {
        try
        {
            logger.info(String.format("Sending message [%s]", message));

            m_objectOutputStream.reset();
            m_objectOutputStream.writeObject(message);
            m_objectOutputStream.flush();

            // This batshitcrazy bit allows people to execute things (in this
            // case system.exit) after the next message has been sent. This is
            // really handy in the case where you want to do something but you
            // need to send a response back to someone to say you've done it,
            // but actually it'll result in a situation where you wouldn't have
            // been able to send a message after you'd actually done that thing!
            if (executeAfterNextMessage != null)
            {
                executeAfterNextMessage.run();
                executeAfterNextMessage = null;
            }
        }
        catch (IOException e)
        {
            logger.log(Level.INFO,
                       "Exception caught sending message, disconnecting...");
            disconnect();
            throw new MessagingRuntimeException("Failed to send message, messaging client has disconnected",
                                                e);
        }
    }

    private synchronized void ensureConnected() throws IOException
    {
        if (!isConnected())
        {
            changeStatus(Status.Connecting);

            InetSocketAddress nextConnectionPoint = connectionPoints.getNextConnectionPoint();

            logger.info(String.format("Attempting to connect to %s:%d",
                                      nextConnectionPoint.getHostName(),
                                      nextConnectionPoint.getPort()));

            m_socket = createSocket(nextConnectionPoint);

            logger.finer(String.format("Successfully connected to %s:%d, initialising streams...",
                                       nextConnectionPoint.getHostName(),
                                       nextConnectionPoint.getPort()));

            OutputStream outputStream = m_socket.getOutputStream();
            InputStream inputStream = m_socket.getInputStream();

            m_objectOutputStream = new ObjectOutputStream(outputStream);
            m_objectInputStream = new ObjectInputStream(inputStream);

            logger.info(String.format("Succesfully connected to %s:%d",
                                      nextConnectionPoint.getHostName(),
                                      nextConnectionPoint.getPort()));

            changeStatus(Status.Connected);
        }
    }

    protected Socket createSocket(InetSocketAddress connectionPoint)
                    throws IOException
    {
        if (getProxy() == null)
        {
            return new Socket(connectionPoint.getHostName(),
                              connectionPoint.getPort());
        }
        else
        {
            logger.info(String.format("Creating socket using proxy [%s]", proxy));

            Socket socket = new Socket(getProxy());
            socket.connect(connectionPoint);
            return socket;
        }
    }

    protected MessageAction handleMessage(Object object)
                    throws InterruptedException
    {
        MessageAction action;

        Message message = (Message) object;

        Serializable payload = message.getPayload();
        if (payload instanceof ConnectedPayload)
        {
            ConnectedPayload connectedPayload = (ConnectedPayload) payload;
            setClientID(connectedPayload.getClientID());
            changeStatus(Status.ConnectedWithClientID);
            action = MessageAction.ConsumeMessage;
        }
        else
        {
            action = MessageAction.PublishToListeners;
        }

        int requestResponseMapping = message.getRequestResponseMapping();
        if (requestResponseMapping != -1)
        {
            Exchanger<Serializable> exchanger = m_requestResponseExchangers.remove(requestResponseMapping);
            if (exchanger != null)
            {
                exchanger.exchange(message.getPayload());
            }
        }

        return action;
    }

    private void setClientID(int clientID)
    {
        this.clientID = clientID;
    }

    public void addSocketClientListener(MessagingClientClientListener listener)
    {
        m_listeners.add(listener);
    }

    public void removeSocketClientListener(MessagingClientClientListener listener)
    {
        m_listeners.remove(listener);
    }

    private void fireNewMessage(Message message)
    {
        for (MessagingClientClientListener listener : m_listeners)
        {
            listener.onNewMessage(message);
        }
    }

    private void fireStatusChanged(Status oldStatus, Status newStatus)
    {
        for (MessagingClientClientListener listener : m_listeners)
        {
            listener.onStatusChanged(oldStatus, newStatus);
        }
    }

    public void setReconnectionTimeout(long i)
    {
        m_reconnectionTimeout = i;
    }

    public long getReconnectionTimeout()
    {
        return m_reconnectionTimeout;
    }

    public synchronized Status getStatus()
    {
        return m_status;
    }

    @SuppressWarnings("unchecked") public <T extends Serializable> T makeRequest(Serializable request,
                                                                                 int destinationID)
                    throws IOException
    {
        Message message = new Message();
        message.setPayload(request);
        message.setDestinationClientID(destinationID);
        message.setSourceClientID(clientID);
        message.setRouting(Routing.PointToPoint);
        return (T) makeRequestInternal(message);
    }

    private Serializable makeRequestInternal(Message message)
                    throws IOException
    {
        int nextRequestResponseID = getNextRequestResponseID();
        message.setRequestResponseMapping(nextRequestResponseID);

        Exchanger<Serializable> exchanger = new Exchanger<Serializable>();
        m_requestResponseExchangers.put(nextRequestResponseID, exchanger);

        sendMessage(message);

        Serializable result = null;
        try
        {
            result = exchanger.exchange(null,
                                        m_requestResponseTimeout,
                                        TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            throw new IOException("Thread was interupted before the response arrived");
        }
        catch (TimeoutException e)
        {
            throw new IOException("Timeout fired waiting for the response to arrive");
        }

        return result;
    }

    @SuppressWarnings("unchecked") public <T extends Serializable> T makeRequest(Serializable request)
                    throws IOException
    {
        Message message = new Message();
        message.setPayload(request);
        message.setRouting(Routing.SendToServer);
        return (T) makeRequestInternal(message);
    }

    private int getNextRequestResponseID()
    {
        return m_nextMappingID.getAndIncrement();
    }

    public void waitUntilConnected()
    {
        waitForStatus(Status.ConnectedWithClientID);
    }

    public void broadcast(Serializable payload)
    {
        Message message = new Message();
        message.setPayload(payload);
        message.setRouting(Message.Routing.Broadcast);
        sendMessage(message);
    }

    public void reply(Message message, Serializable payload)
    {
        Message responseMessage = new Message();
        responseMessage.setPayload(payload);
        responseMessage.setRequestResponseMapping(message.getRequestResponseMapping());
        responseMessage.setDestinationClientID(message.getSourceClientID());
        if (message.getSourceClientID() == 0)
        {
            responseMessage.setRouting(Message.Routing.SendToServer);
        }
        else
        {
            responseMessage.setRouting(Message.Routing.PointToPoint);
        }
        responseMessage.setSourceClientID(clientID);
        sendMessage(responseMessage);
    }

    /**
     * Send a message to the server. It will not be automatically routed
     * anywhere else, but the server might choose to do anything with.
     * 
     * @param payload
     */
    public void send(Serializable payload)
    {
        Message message = new Message();
        message.setPayload(payload);
        message.setRouting(Message.Routing.SendToServer);
        sendMessage(message);
    }

    public void setProxy(Proxy proxy)
    {
        this.proxy = proxy;
    }

    public Proxy getProxy()
    {
        return proxy;
    }

    public int getClientID()
    {
        return clientID;
    }

    public void executeAfterNextMessage(Runnable runnable)
    {
        this.executeAfterNextMessage = runnable;
    }
}
