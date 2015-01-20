package com.logginghub.logging.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.LoggingMessageCollectionMessage;
import com.logginghub.utils.ExceptionHandler;
import com.logginghub.utils.ExpandingByteBuffer;
import com.logginghub.utils.WorkerThread;

/**
 * A LoggingMessageSender that uses the decorator pattern to provide aggregation
 * features to another LoggingMessageSender.
 * 
 * @author admin
 */
public class AggregatingSender implements LoggingMessageSender
{
    private static Logger logger = Logger.getLogger(AggregatingSender.class.getName());
    private LoggingMessageSender m_decorated;

    private List<LoggingMessage> m_queue = new ArrayList<LoggingMessage>();

    private int m_triggerSize = 10;
    private int m_triggerBytes = 1024;
    private long m_triggerInterval = 1000;

    private ExpandingByteBuffer m_estimationBuffer = new ExpandingByteBuffer();
    private LoggingMessageCodex m_codex = new LoggingMessageCodex();
    private int m_currentEstimatedBytes = 0;

    private WorkerThread m_workerThread;

    private ExceptionHandler m_exceptionHandler;

    public AggregatingSender(LoggingMessageSender decorated,
                             ExceptionHandler exceptionHandler)
    {
        m_decorated = decorated;
        m_exceptionHandler = exceptionHandler;
        start();
    }

    private void start()
    {
        m_workerThread = new WorkerThread("AggregatingSenderThread")
        {
            @Override protected void onRun() throws Throwable
            {
                processQueue();
                sleepUntilTriggerInterval();
            }
        };
        m_workerThread.start();
    }

    protected void sleepUntilTriggerInterval()
    {
        try
        {
            if (m_triggerInterval == -1)
            {
                Thread.sleep(Long.MAX_VALUE);
            }
            else
            {
                Thread.sleep(m_triggerInterval);
            }
        }
        catch (InterruptedException e)
        {
            // We expect to get interupted, so do nothing
        }
    }

    protected void processQueue()
    {
        LoggingMessageCollectionMessage messageCollection = null;

        synchronized (m_queue)
        {
            if (m_queue.size() > 0)
            {
                messageCollection = new LoggingMessageCollectionMessage();
                for (LoggingMessage message : m_queue)
                {
                    messageCollection.addMessage(message);
                }

                m_queue.clear();
                m_estimationBuffer.clear();
                m_currentEstimatedBytes = 0;
            }
        }

        if (messageCollection != null)
        {
            try
            {
                if (logger.isLoggable(Level.FINE))
                {
                    logger.fine(String.format("Sending message collection '%s'",
                                              messageCollection));
                }
                m_decorated.send(messageCollection);
            }
            catch (LoggingMessageSenderException e)
            {
                AggregatingSenderException ase = new AggregatingSenderException("Exception caught passing aggregated message collection to the decorated sender. The messages that weren't sent are attached to the exception if you want to try and replay them.",
                                                                                e);
                ase.setMessageCollection(messageCollection);
                m_exceptionHandler.handleException("Exception caught passing aggregated message collection to the decorated sender.", ase);
            }
        }
    }

    public void send(LoggingMessage message)
                    throws LoggingMessageSenderException
    {
        synchronized (m_queue)
        {
            m_queue.add(message);

            if (m_triggerSize != -1 && m_queue.size() >= m_triggerSize)
            {
                interuptDispatchThread();
            }

            if (m_triggerBytes != -1)
            {
                m_codex.encode(m_estimationBuffer, message);

                m_currentEstimatedBytes += m_estimationBuffer.position();
                m_estimationBuffer.clear();

                if (m_currentEstimatedBytes >= m_triggerBytes)
                {
                    logger.fine(String.format("Estimate bytes '%d' is greater than the trigger bytes limit '%d', waking up the dispatch thread",
                                              m_currentEstimatedBytes,
                                              m_triggerBytes));
                    interuptDispatchThread();
                }
            }
        }
    }

    public int getTriggerSize()
    {
        return m_triggerSize;
    }

    public void setTriggerSize(int triggerSize)
    {
        m_triggerSize = triggerSize;
    }

    public long getTriggerInterval()
    {
        return m_triggerInterval;
    }

    /**
     * Change the trigger interval. This results in the current queue being
     * flushed before starting with the new interval.
     * 
     * @param triggerInterval
     */
    public void setTriggerInterval(long triggerInterval)
    {
        m_triggerInterval = triggerInterval;
        interuptDispatchThread();
    }

    private void interuptDispatchThread()
    {
        m_workerThread.interupt();
    }

    public void setTriggerBytes(int triggerBytes)
    {
        m_triggerBytes = triggerBytes;
    }

    public int getTriggerBytes()
    {
        return m_triggerBytes;
    }

    public void stop()
    {
        m_workerThread.stop();
    }
}
