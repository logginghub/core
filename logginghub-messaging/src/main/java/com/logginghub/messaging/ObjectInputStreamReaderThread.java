package com.logginghub.messaging;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.utils.WorkerThread;

public class ObjectInputStreamReaderThread extends WorkerThread
{
    private InputStream m_inputStream;

    private List<ObjectInputStreamReaderListener> m_listeners = new CopyOnWriteArrayList<ObjectInputStreamReaderListener>();

    private ObjectInputStream m_objectInputStream;

    public void addListener(ObjectInputStreamReaderListener listener)
    {
        m_listeners.add(listener);
    }

    public void removeListener(ObjectInputStreamReaderListener listener)
    {
        m_listeners.remove(listener);
    }

    private void fireObjectRead(Object object)
    {
        for (ObjectInputStreamReaderListener listener : m_listeners)
        {
            listener.onObjectRead(object);
        }
    }

    private void fireStreamClosed()
    {
        for (ObjectInputStreamReaderListener listener : m_listeners)
        {
            listener.onStreamClosed();
        }
    }

    public ObjectInputStreamReaderThread(InputStream inputStream, String name) throws IOException
    {
        super(name);
        m_inputStream = inputStream;
        m_objectInputStream = new ObjectInputStream(m_inputStream);
    }

    @Override protected void onRun()
    {
        try
        {
            Object object = m_objectInputStream.readObject();
            fireObjectRead(object);
        }
        catch (SocketException socketException)
        {
            fireStreamClosed();
            finished();
        }
        catch (EOFException eof)
        {
            fireStreamClosed();
            finished();
        }
        catch (IOException e)
        {
            if (keepRunning())
            {

                e.printStackTrace();
            }
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    @Override public void stop()
    {
        try
        {
            m_objectInputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        super.stop();
    }

}
