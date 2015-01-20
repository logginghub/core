package com.logginghub.logging.servers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logginghub.logging.listeners.ConnectionListener;
import com.logginghub.utils.WorkerThread;

/**
 * A worker thread that handles a java.net.ServerSocket. Add
 * <code>ConnectionListener</code>s to handle the newly accepted connections.
 * 
 * @deprecated not used anymore
 * @author admin
 */
public class ServerSocketWorkerThread extends WorkerThread
{
    private List<ConnectionListener> m_listeners = new ArrayList<ConnectionListener>();
    private int m_port;
    private ServerSocket m_serverSocket;
    private Logger m_logger = Logger.getLogger(this.getClass().getName());
    private CountDownLatch m_boundLatch = new CountDownLatch(1);

    public ServerSocketWorkerThread(int port)
    {
        super("ServerSocketWorkerThread");
        
        m_port = port;
    }

    public void addConnectionListner(ConnectionListener listener)
    {
        m_listeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener)
    {
        m_listeners.remove(listener);
    }

    // //////////////////////////////////////////////////////////////////
    // WorkerThread overrides
    // //////////////////////////////////////////////////////////////////

    @Override
    protected void onRun()
    {
        if(ensureBound())
        {
            try
            {
                Socket accepted = accept();
                fireListeners(accepted);
            }
            catch (IOException e)
            {
                if(keepRunning())
                {
                    m_logger.log(Level.INFO, "Failed to accept connection", e);
                }
            }
        }
    }

    @Override
    protected void beforeStart()
    {

    }

    @Override
    protected void beforeStop()
    {
        if(m_serverSocket != null)
        {
            try
            {
                m_serverSocket.close();
            }
            catch (IOException e)
            {
                m_logger.log(Level.FINE,
                             "Failed to close server socket whilst stopping, this is odd but shouldn't cause any issues",
                             e);
            }
        }
    }

    // //////////////////////////////////////////////////////////////////
    // Private methods
    // //////////////////////////////////////////////////////////////////

    private void fireListeners(Socket accepted)
    {
        for(ConnectionListener listener : m_listeners)
        {
//            listener.onNewConnection(accepted);
        }
    }

    private Socket accept() throws IOException
    {
        return m_serverSocket.accept();
    }

    private boolean ensureBound()
    {
        if(m_serverSocket == null || !m_serverSocket.isBound())
        {
            try
            {
                m_serverSocket = new ServerSocket(m_port);
                m_logger.log(Level.INFO, "Successfully bound to server port " + m_port);
                m_boundLatch.countDown();
            }
            catch (IOException e)
            {
                m_logger.log(Level.WARNING, "Failed to bind to server port " + m_port, e);
                m_serverSocket = null;
            }
        }

        return true;
    }

    public void waitUntilBound()
    {
        try
        {
            m_boundLatch.await();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Thread was interupted waiting on the bound latch, so we do not know what start the server socket is in", e);
        }
    }
}
