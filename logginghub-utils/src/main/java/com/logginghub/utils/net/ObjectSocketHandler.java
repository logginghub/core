package com.logginghub.utils.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ObjectSocketHandler implements SocketHandler, ObjectDestination
{
    private static Logger logger = Logger.getLogger(ObjectSocketHandler.class.getName());
    
    private InputStream m_inputStream;
    private OutputStream m_outputStream;
    private Socket m_socket;

    private ObjectInputStreamReaderThread m_reader;

    private List<ObjectSocketListener> m_listeners = new CopyOnWriteArrayList<ObjectSocketListener>();

    private ObjectOutputStream m_objectsOut;
    
    private ExecutorService m_dispatchThreads = Executors.newCachedThreadPool();

    public ObjectSocketHandler()
    {

    }

    public void addObjectSocketHandlerListener(ObjectSocketListener listener)
    {
        m_listeners.add(listener);
    }

    public void removeObjectSocketHandlerListener(ObjectSocketListener listener)
    {
        m_listeners.remove(listener);
    }

    private void fireNewObject(Object object)
    {
        for (ObjectSocketListener listener : m_listeners)
        {
            listener.onNewObject(object, this);
        }
    }
    
    private void fireConnectionClosed()
    {
        for (ObjectSocketListener listener : m_listeners)
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

            m_objectsOut = new ObjectOutputStream(m_outputStream);

            m_reader = new ObjectInputStreamReaderThread(m_inputStream);
            m_reader.addListener(new ObjectInputStreamReaderListener()
            {
                public void onObjectRead(final Object object)
                {                    
                    logger.info(String.format("New object read from input stream : %s, queuing for dispatch", object));
                    m_dispatchThreads.execute(new Runnable()
                    {                        
                        public void run()
                        {
                            fireNewObject(object);                        
                        }
                    });                   
                }

                public void onStreamClosed()
                {
                    logger.info(String.format("Connection closed from %s", socket));
                    fireConnectionClosed();
                }
            });
            m_reader.start();
        }
        catch (IOException e)
        {
            throw new RuntimeException(String.format("Failed to setup streams for socket %s",
                                                     socket),
                                       e);
        }
    }

    public void send(Object object)
    {
        try
        {
            logger.info(String.format("Sending object %s", object));
            
            m_objectsOut.writeUnshared(object);
            m_objectsOut.flush();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to encode message using java serialisation",e);
        }
    }

    public void shutdown()
    {
        m_dispatchThreads.shutdown();
        m_reader.stop();
    }
}
