package com.logginghub.logging.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.LoggingMessageCodex;
import com.logginghub.utils.ConnectionPointManager;
import com.logginghub.utils.ExpandingByteBuffer;

/**
 * Socket io based publisher. Use this to connect to a server port and publish
 * full log events.
 * 
 * @author admin
 * @deprecated You should be using the socket client now
 */
public class SocketPublisher
{
    private static final int defaultCapacity = 1024;
    private Logger m_logger = Logger.getLogger(this.getClass().getName());

    private ThreadLocal<ExpandingByteBuffer> m_encodeBuffersByThread = new ThreadLocal<ExpandingByteBuffer>();

    private ConnectionPointManager m_connectionPointManager = new ConnectionPointManager();

    private Socket m_socket;

    private OutputStream m_outputStream;
    private long m_reconnectionPause = 1000;

    private LoggingMessageCodex m_codex = new LoggingMessageCodex();
    
    public SocketPublisher()
    {
        m_logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.WARNING);
        m_logger.addHandler(handler);
        m_logger.setLevel(Level.WARNING);
    }

    // //////////////////////////////////////////////////////////////////
    // Accessors
    // //////////////////////////////////////////////////////////////////

    public void addConnectionPoint(InetSocketAddress inetSocketAddress)
    {
        m_connectionPointManager.addConnectionPoint(inetSocketAddress);
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

    public void publish(LogEvent record) throws FailedToSendException
    {
        LogEventMessage message = new LogEventMessage(record);
        
        ExpandingByteBuffer expandingByteBuffer = getEncodeBufferForThread();

        int startingPosition = expandingByteBuffer.getBuffer().position();

        boolean done = false;

        while(!done)
        {
            try
            {                
                m_codex.encode(expandingByteBuffer, message);
                done = true;
            }
            catch (BufferOverflowException boe)
            {
                expandingByteBuffer.doubleSize();
                expandingByteBuffer.getBuffer().clear();
            }
        }

        ByteBuffer encodeBuffer = expandingByteBuffer.getBuffer();
        encodeBuffer.flip();

        try
        {
            writeToOutput(encodeBuffer);
        }
        catch (FailedToSendException ftse)
        {
            // Reset the buffer position back to scrub that last entry out
            encodeBuffer.position(startingPosition);
            throw ftse;
        }
    }

    // //////////////////////////////////////////////////////////////////
    // Private methods
    // //////////////////////////////////////////////////////////////////

    private synchronized void writeToOutput(ByteBuffer encodeBuffer) throws FailedToSendException
    {
        boolean success = false;
        int attempts = 0;
        int maxAttempts = 3;

        int startingPosition = encodeBuffer.position();

        while(!success && attempts < maxAttempts)
        {
            attempts++;
            attemptConnection();

            if(m_socket != null)
            {
                m_logger.log(Level.FINE,
                             "About to write encoded event (" + encodeBuffer.remaining() +
                                     " bytes) to the publisher output stream");

                try
                {
                    // jshaw - this is realllllly slow
                    while(encodeBuffer.remaining() > 0)
                    {

                        m_outputStream.write(encodeBuffer.get());
                    }

                    success = true;
                }
                catch (IOException e)
                {
                    m_logger.log(Level.INFO,
                                 "Failed to write log record to " + m_connectionPointManager.getCurrentConnectionPoint() +
                                         ", disconnecting and trying the next connection",
                                 e);
                    disconnect();
                    encodeBuffer.position(startingPosition);
                }
            }
        }

        encodeBuffer.compact();

        if(!success)
        {
            throw new FailedToSendException("Failed to publish log event after " + maxAttempts +
                                            ", giving up.");
            // m_logger.log(Level.WARNING,
            // "Couldn't write event to logging hub after 3 attempts, will retry next time you log something"
            // );
        }
    }

    private void attemptConnection()
    {
        if(m_socket == null)
        {
            InetSocketAddress nextConnectionPoint = m_connectionPointManager.getNextConnectionPoint();

            try
            {
                m_logger.info("Attempting to connect to " + nextConnectionPoint);
                m_socket = new Socket(nextConnectionPoint.getAddress(),
                                      nextConnectionPoint.getPort());

                m_logger.info("Connection to " + nextConnectionPoint + " was successful");
                m_outputStream = m_socket.getOutputStream();
            }
            catch (IOException e)
            {
                m_logger.log(Level.INFO, "Connection to " + nextConnectionPoint + " failed.", e);
                m_socket = null;
                disconnect();
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

    private ExpandingByteBuffer getEncodeBufferForThread()
    {
        ExpandingByteBuffer expandingBuffer = m_encodeBuffersByThread.get();

        if(expandingBuffer == null)
        {
            expandingBuffer = new ExpandingByteBuffer(defaultCapacity);
            m_encodeBuffersByThread.set(expandingBuffer);
        }

        return expandingBuffer;
    }

    public void disconnect()
    {
        if(m_socket != null && !m_socket.isClosed())
        {
            try
            {
                m_socket.close();
            }
            catch (IOException e)
            {
            }

            m_socket = null;
        }
    }

    public void replaceConnectionList(List<InetSocketAddress> newConnectionPointList)
    {
        InetSocketAddress currentConnectionPoint = m_connectionPointManager.getCurrentConnectionPoint();

        m_connectionPointManager.clearConnectionPoints();
        m_connectionPointManager.addConnectionPoints(newConnectionPointList);

        if(newConnectionPointList.contains(currentConnectionPoint))
        {
            // All good, no need to reconnect
        }
        else
        {
            disconnect();
        }
    }
}
