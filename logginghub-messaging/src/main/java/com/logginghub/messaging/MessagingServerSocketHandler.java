package com.logginghub.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logginghub.utils.NamedThreadFactory;

public class MessagingServerSocketHandler implements GenericMessageSenderInterface
{
    private static Logger logger = Logger.getLogger(MessagingServerSocketHandler.class.getName());

    private InputStream m_inputStream;
    private OutputStream m_outputStream;
    private Socket m_socket;

    private ObjectInputStreamReaderThread m_reader;

    private List<MessagingServerSocketListener> m_listeners = new CopyOnWriteArrayList<MessagingServerSocketListener>();

    private ObjectOutputStream m_objectsOut;

    private NamedThreadFactory threadFactory;
    private ExecutorService m_dispatchThreads;

    private int clientID;

    public MessagingServerSocketHandler()
    {

    }

    public void addObjectSocketHandlerListener(MessagingServerSocketListener listener)
    {
        m_listeners.add(listener);
    }

    public void removeObjectSocketHandlerListener(MessagingServerSocketListener listener)
    {
        m_listeners.remove(listener);
    }

    private void fireNewMessage(Message message)
    {
        for (MessagingServerSocketListener listener : m_listeners)
        {
            listener.onNewMessage(message, this);
        }
    }

    private void fireConnectionClosed()
    {
        for (MessagingServerSocketListener listener : m_listeners)
        {
            listener.onConnectionClosed(this);
        }
    }

    public Socket getSocket()
    {
        return m_socket;
    }

    public void handleSocket(final Socket socket)
    {
        m_socket = socket;
        try
        {
            m_inputStream = socket.getInputStream();
            m_outputStream = socket.getOutputStream();

            threadFactory = new NamedThreadFactory("ObjectSocketHandlerDispatch(" +
                                                   socket.getRemoteSocketAddress() +
                                                   ")");
            m_dispatchThreads = Executors.newCachedThreadPool(threadFactory);

            m_objectsOut = new ObjectOutputStream(m_outputStream);

            m_reader = new ObjectInputStreamReaderThread(m_inputStream,
                                                         "ObjectSocketReader-" +
                                                                         socket.toString());
            m_reader.addListener(new ObjectInputStreamReaderListener()
            {
                public void onObjectRead(final Object object)
                {
                    if (logger.isLoggable(Level.FINE))
                    {
                        logger.fine(String.format("New object read from input stream : %s, queuing for dispatch",
                                                  object));
                    }

                    if (object instanceof Message)
                    {
                        final Message message = (Message) object;

                        m_dispatchThreads.execute(new Runnable()
                        {
                            public void run()
                            {
                                fireNewMessage(message);
                            }
                        });
                    }
                    else
                    {
                        throw new RuntimeException(String.format("Object received of type [%s], we only accept objects of type Message.",
                                                                 object.getClass()
                                                                       .getName()));
                    }
                }

                public void onStreamClosed()
                {
                    if (logger.isLoggable(Level.FINE))
                    {
                        logger.fine(String.format("Connection closed from %s",
                                                  socket));
                    }

                    fireConnectionClosed();
                }
            });
            m_reader.start();

            Message message = new Message();
            message.setPayload(new ConnectedPayload(clientID));
            sendObject(message);

        }
        catch (IOException e)
        {
            throw new RuntimeException(String.format("Failed to setup streams for socket %s",
                                                     socket),
                                       e);
        }
    }

    public void shutdown()
    {
        m_dispatchThreads.shutdown();
        m_reader.stop();
    }

    private void sendObject(Message message)
    {
        synchronized (m_objectsOut)
        {
            try
            {
                if (logger.isLoggable(Level.FINE))
                {
                    logger.fine(String.format("Sending object %s", message));
                }

                m_objectsOut.reset();
                m_objectsOut.writeObject(message);
                m_objectsOut.flush();
            }
            catch (IOException e)
            {
                throw new MessagingRuntimeException("Failed to encode message using java serialisation",
                                                    e);
            }
        }
    }

    public void sendMessage(Message response)
    {
        sendObject(response);
    }

    public void reply(Message message, Serializable responsePayload)
    {
        logger.fine(String.format("Replying to message '%s' with payload '%s'",
                                  message,
                                  responsePayload));
        Message response = new Message();

        response.setPayload(responsePayload);
        response.setDestinationClientID(message.getSourceClientID());
        response.setRequestResponseMapping(message.getRequestResponseMapping());

        sendMessage(response);
    }

    public void setClientID(int clientID)
    {
        this.clientID = clientID;
    }

    public int getClientID()
    {
        return clientID;
    }
}
