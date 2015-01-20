package com.logginghub.logging.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.ConnectionPointManager;
import com.logginghub.utils.WorkerThread;

/**
 * Socket IO based receiver thread. It connects to a source server socket and reads
 * log events.
 * @author admin
 * @deprecated Use the SocketClient/SocketConnection etc
 *
 */
public class SocketReceiverThread extends WorkerThread
{
    private ConnectionPointManager m_connectionPointManager = new ConnectionPointManager();
    private List<LogEventListener> m_listeners = new ArrayList<LogEventListener>();
    // private Thread m_thread;
    // private boolean m_keepRunning = true;
    private Logger m_logger = Logger.getLogger(this.getClass().getName());
    private Socket m_socket;
    private long m_reconnectionPause;
    private InputStream m_inputStream;
    private OutputStream m_outputStream;
    private LogEventInputStream m_logEventInputStream;
    private CountDownLatch m_connectedLatch = new CountDownLatch(1);

    public SocketReceiverThread()
    {
        super("LogEventReceiver");
    }

    public void addConnectionPoint(InetSocketAddress inetSocketAddress)
    {
        m_connectionPointManager.addConnectionPoint(inetSocketAddress);
    }

    public void addConnectionPoints(List<InetSocketAddress> addreses)
    {
        for(InetSocketAddress inetSocketAddress : addreses)
        {
            addConnectionPoint(inetSocketAddress);
        }
    }

    public void removeConnectionPoint(InetSocketAddress inetSocketAddress)
    {
        m_connectionPointManager.removeConnectionPoint(inetSocketAddress);
    }

    public void clearConnectionPoints()
    {
        m_connectionPointManager.clearConnectionPoints();
    }

    public List<InetSocketAddress> getConnectionPoints()
    {
        return m_connectionPointManager.getConnectionPoints();
    }

    public void addLogEventListener(LogEventListener listener)
    {
        m_listeners.add(listener);
    }

    public void removeLogEventListener(LogEventListener listener)
    {
        m_listeners.remove(listener);
    }

    // public void start()
    // {
    // if(m_thread == null)
    // {
    // Runnable runnable = new Runnable()
    // {
    // public void run()
    // {
    // runInternal();
    // }
    // };
    //
    // m_thread = new Thread(runnable, "RecieverThread");
    // m_thread.start();
    // }
    // }

    private void fireListeners(LogEvent event)
    {
        for(LogEventListener listener : m_listeners)
        {
            listener.onNewLogEvent(event);
        }
    }

    private void disconnect()
    {
        if(m_socket != null && m_socket.isConnected())
        {
            try
            {
                m_socket.close();
            }
            catch (IOException e)
            {
            }

            m_socket = null;

            m_connectedLatch = new CountDownLatch(1);
        }
    }

    private void ensureConnected()
    {
        while(m_socket == null && keepRunning())
        {
            InetSocketAddress nextConnectionPoint = m_connectionPointManager.getNextConnectionPoint();

            try
            {
                m_logger.info("Attempting to connect to " + nextConnectionPoint);
                m_socket = new Socket(nextConnectionPoint.getAddress(),
                                      nextConnectionPoint.getPort());

                m_inputStream = m_socket.getInputStream();
                m_outputStream = m_socket.getOutputStream();
                m_logEventInputStream = new LogEventInputStream(m_inputStream);

                m_logger.info("Connection to " + nextConnectionPoint + " was successful");
                m_connectedLatch.countDown();
            }
            catch (IOException e)
            {
                m_logger.log(Level.INFO, "Connection to " + nextConnectionPoint + " failed.", e);
                reconnectionPause();
            }
        }
    }

    private void reconnectionPause()
    {
        try
        {
            Thread.sleep(m_reconnectionPause);
        }
        catch (InterruptedException e)
        {

        }
    }

    public void waitUntilConnected()
    {
        try
        {
            m_connectedLatch.await();
        }
        catch (InterruptedException ie)
        {

        }
    }

    // public void stop()
    // {
    // m_keepRunning = false;
    // if(m_thread != null)
    // {
    // m_thread.interrupt();
    // try
    // {
    // m_thread.join();
    // }
    // catch (InterruptedException ie)
    // {
    // throw new RuntimeException(
    // "Thread was interuped waiting on the receiver thread to shutdown. We dont know what state it will be in now."
    // );
    // }
    // }
    // }

    @Override
    protected void onRun()
    {
        ensureConnected();

        if(keepRunning())
        {
            try
            {
                LogEvent event = m_logEventInputStream.readLogEvent();
                m_logger.log(Level.FINE, "Event received " + event);
                fireListeners(event);
            }
            catch (IOException e)
            {
                disconnect();
            }
        }
    }

    @Override
    protected void beforeStop()
    {
        super.beforeStop();

        if(m_logEventInputStream != null)
        {
            try
            {
                m_logEventInputStream.close();
            }
            catch (IOException e)
            {
            }
        }
    }

}
