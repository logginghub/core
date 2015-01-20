package com.logginghub.messaging;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logginghub.messaging.Message.Routing;
import com.logginghub.utils.ExceptionHandler;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.NamedThreadFactory;
import com.logginghub.utils.Ports;
import com.logginghub.utils.WorkerThread;

public class MessagingServer implements MessagingSender
{
    // TODO : refactor all the send/request reponse stuff out into a delegate
    // that can be resused between client and server
    private boolean useDispatchThreads = false;
    private Executor dispatchPool;

    private Map<Integer, Exchanger<Serializable>> m_requestResponseExchangers = new HashMap<Integer, Exchanger<Serializable>>();
    private static Logger logger = Logger.getLogger(MessagingServer.class.getName());
    private WorkerThread acceptorThread;
    private int port = Ports.MessagingServer;
    private ServerSocket serverSocket;
    private ExceptionHandler exceptionHandler;
    private AtomicInteger m_nextMappingID = new AtomicInteger();
    // TODO : fix this and make it configurable
    private long m_requestResponseTimeout = 1000000;

    private List<SocketServerListener> listeners = new CopyOnWriteArrayList<SocketServerListener>();
    private boolean shuttingDown = false;

    private CountDownLatch boundLatch = new CountDownLatch(1);

    private MessagePayloadProcessor payloadProcessor = new MessagePayloadProcessor();

    private List<MessagingServerSocketHandler> handlers = new CopyOnWriteArrayList<MessagingServerSocketHandler>();
    private Map<Integer, MessagingServerSocketHandler> handlersByID = new HashMap<Integer, MessagingServerSocketHandler>();

    private AtomicInteger nextID = new AtomicInteger(1);
    private boolean useDynamicPort = false;

    public MessagingServer()
    {

    }

    public void setUseDispatchThreads(boolean useDispatchThreads)
    {
        this.useDispatchThreads = useDispatchThreads;
        if (useDispatchThreads && dispatchPool == null)
        {
            dispatchPool = Executors.newCachedThreadPool(new NamedThreadFactory("MessagingServer-worker-"));
        }
    }

    public boolean willUseDispatchThreads()
    {
        return useDispatchThreads;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler)
    {
        this.exceptionHandler = exceptionHandler;
    }

    public ExceptionHandler getExceptionHandler()
    {
        return exceptionHandler;
    }

    public void addServerSocketConnectorListener(SocketServerListener listener)
    {
        listeners.add(listener);
    }

    public void removeServerSocketConnectorListener(SocketServerListener listener)
    {
        listeners.remove(listener);
    }

    public int getPort()
    {
        int port;

        // jshaw - if we are using dynamic ports we should check the actual
        // server socket instance first
        if (serverSocket != null)
        {
            port = serverSocket.getLocalPort();
        }
        else
        {
            port = this.port;
        }

        return port;
    }

    public void start()
    {
        if (acceptorThread != null)
        {
            throw new RuntimeException("You've started the server socket connector acceptor thread already.");
        }

        acceptorThread = new WorkerThread("ServerSocketConnectorAcceptorThread")
        {
            @Override protected void onRun() throws IOException
            {
                try
                {
                    ensureBound();
                    accept();
                }
                catch (Throwable t)
                {
                    logger.log(Level.WARNING,
                               String.format("Exception caught from bind/accept loop"),
                               t);
                }
            }
        };

        acceptorThread.start();
    }

    private synchronized void ensureBound() throws IOException
    {
        if (serverSocket == null)
        {
            serverSocket = createServerSocket();
            logger.info(String.format("Successfully bound to port %d",
                                      getPort()));

            boundLatch.countDown();

        }
    }

    protected ServerSocket createServerSocket() throws IOException
    {
        ServerSocket serverSocket = new ServerSocket();
        if (useDynamicPort)
        {
            serverSocket.bind(null);
        }
        else
        {
            serverSocket.bind(new InetSocketAddress(port));
        }

        return serverSocket;
    }

    private void accept()
    {
        try
        {
            Socket socket = serverSocket.accept();
            logger.info(String.format("Connection accepted from [%s]",
                                      socket.getRemoteSocketAddress()));

            int clientID = nextID.getAndIncrement();

            MessagingServerSocketHandler handler = new MessagingServerSocketHandler();
            handler.setClientID(clientID);
            handler.addObjectSocketHandlerListener(new MessagingServerSocketListener()
            {
                public void onNewMessage(Message message,
                                         MessagingServerSocketHandler source)
                {
                    logger.fine(String.format("Message '%s' received from '%s'",
                                              message,
                                              source));
                    // Override the message sourceID to make sure it comes from
                    // where it says it does!
                    // TODO : shouldn't we be angry if this doesn't already
                    // match up?
                    message.setSourceClientID(source.getClientID());

                    logger.info(String.format("Message received [%s]", message));

                    // Fire the message to anyone listening to this server
                    fireMessage(message, source);

                    // Send the message to the routing handler in order to
                    // process messages
                    payloadProcessor.process(message, source);

                    // Do a clienty request response mapping bit in case anyone
                    // is using the server directly
                    checkRequestResponse(message, source);

                    // Route the message to its destination
                    route(message, source);
                }

                public void onConnectionClosed(MessagingServerSocketHandler source)
                {
                    removeHandler(source);
                }
            });

            handlers.add(handler);
            handlersByID.put(clientID, handler);

            handler.handleSocket(socket);
            fireAccepted(handler);
        }
        catch (IOException e)
        {
            if (!shuttingDown)
            {
                exceptionHandler.handleException("Exception caught from accept", e);
            }
        }
    }

    protected void checkRequestResponse(Message message,
                                        MessagingServerSocketHandler source)
    {
        int requestResponseMapping = message.getRequestResponseMapping();
        if (requestResponseMapping != -1)
        {
            Exchanger<Serializable> exchanger = m_requestResponseExchangers.remove(requestResponseMapping);
            if (exchanger != null)
            {
                try
                {
                    exchanger.exchange(message.getPayload());
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException("Interupted waiting on payload exchanger",
                                               e);
                }
            }
        }

    }

    private void removeHandler(MessagingServerSocketHandler source)
    {
        handlers.remove(source);
        handlersByID.remove(source.getClientID());
    }

    protected void route(Message message, MessagingServerSocketHandler source)
    {
        switch (message.getRouting())
        {
            case Broadcast:
            {
                broadcast(message, source);
                break;
            }
            case PointToPoint:
            {
                routePointToPoint(message);
                break;
            }
            case SendToServer:
            {
                // Dont have to do anything
                break;
            }
            default:
            {
                throw new MessagingRuntimeException("Unsupported routing type " +
                                                    message.getRouting());
            }
        }
    }

    private void routePointToPoint(Message message)
    {
        int destinationID = message.getDestinationClientID();

        MessagingServerSocketHandler destination = handlersByID.get(destinationID);
        if (destination != null)
        {
            if (useDispatchThreads)
            {
                dispatchPool.execute(new WriteOperation(message,
                                                        destination,
                                                        this));
            }
            else
            {
                try
                {
                    logger.log(Level.FINE,
                               String.format("Sending point-to-point message to socket handler [%s]",
                                             message,
                                             destination));
                    destination.sendMessage(message);
                }
                catch (MessagingRuntimeException e)
                {
                    logger.log(Level.INFO,
                               String.format("Exception caught sending message [%s] to socket handler [%s], disconnect and removing this handler",
                                             message,
                                             destination),
                               e);
                    destination.shutdown();
                    removeHandler(destination);
                }
            }
        }
        else
        {
            logger.log(Level.WARNING,
                       String.format("Couldn't find a destination with identifier [%s], failed to route message",
                                     destinationID));

        }
    }

    private void broadcast(Message message, MessagingServerSocketHandler source)
    {
        Iterator<MessagingServerSocketHandler> iterator = handlers.iterator();
        while (iterator.hasNext())
        {
            MessagingServerSocketHandler handler = iterator.next();

            if (handler != source)
            {
                if (useDispatchThreads)
                {
                    dispatchPool.execute(new WriteOperation(message,
                                                            handler,
                                                            this));
                }
                else
                {
                    try
                    {
                        logger.log(Level.FINE,
                                   String.format("Broadcasting message to socket handler [%s]",
                                                 message,
                                                 handler));
                        handler.sendMessage(message);
                    }
                    catch (MessagingRuntimeException e)
                    {
                        logger.log(Level.INFO,
                                   String.format("Exception caught sending message [%s] to socket handler [%s], disconnect and removing this handler",
                                                 message,
                                                 handler),
                                   e);
                        handler.shutdown();
                        iterator.remove();
                    }
                }
            }
        }
    }

    // private void sendToSocket(MessagingServerSocketHandler handler,
    // Message message)
    // {
    // try
    // {
    // handler.sendMessage(message);
    // }
    // catch (MessagingRuntimeException e)
    // {
    //
    // }
    //
    // }

    protected void fireAccepted(MessagingServerSocketHandler handler)
    {
        for (SocketServerListener socketServerListener : listeners)
        {
            socketServerListener.onAccepted(handler);
        }
    }

    protected void fireMessage(Message message,
                               MessagingServerSocketHandler source)
    {
        for (SocketServerListener socketServerListener : listeners)
        {
            socketServerListener.onMessage(message, source);
        }
    }

    public void stop()
    {
        logger.info(String.format("SocketServer stopping"));

        shuttingDown = true;
        FileUtils.closeQuietly(serverSocket);
        acceptorThread.stop();

        for (MessagingServerSocketHandler messagingServerSocketHandler : handlers)
        {
            messagingServerSocketHandler.shutdown();
        }
    }

    public void waitUntilBound()
    {
        try
        {
            boundLatch.await();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(String.format("Thread interupted waiting for bound latch"),
                                       e);
        }
    }

    public <T> void registerHandler(Class<T> c,
                                    MessagePayloadHandler<T> objectHandler)
    {
        payloadProcessor.addHandler(c, objectHandler);
    }

    public void setUseDynamicPort()
    {
        this.useDynamicPort = true;
    }

    // The sending bit from the client, so the server can make request response
    // style calls to its clients. Feels a bit odd - its been added to allow the
    // server-side remoting calls to be made symetrically between client and
    // servers... instead maybe we could have a virtual client acting within the
    // server instead?
    @SuppressWarnings("unchecked") public <T extends Serializable> T makeRequest(Serializable request,
                                                                                 int destinationID)
                    throws IOException
    {
        Message message = new Message();
        message.setPayload(request);
        message.setDestinationClientID(destinationID);
        message.setSourceClientID(0);
        message.setRouting(Routing.PointToPoint);
        return (T) makeRequestInternal(message);
    }

    private int getNextRequestResponseID()
    {
        return m_nextMappingID.getAndIncrement();
    }

    private void sendMessage(Message message)
    {
        routePointToPoint(message);
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

    public <T extends Serializable> T makeRequest(Serializable request)
                    throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Used by the asynchronous write operations to notify the server if
     * something went wrong
     * 
     * @param message
     * @param destination
     */
    public void asyncWriteOperationFailed(Message message,
                                          MessagingServerSocketHandler destination)
    {
        removeHandler(destination);
        destination.shutdown();
    }
}
