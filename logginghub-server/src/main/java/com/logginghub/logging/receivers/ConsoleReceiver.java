package com.logginghub.logging.receivers;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFormatter;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.logeventformatters.WindowsColouredConsoleFormatter;
import com.logginghub.logging.messages.SubscriptionRequestMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.natives.Win32ConsoleAPI;

public class ConsoleReceiver implements LogEventListener
{
    private LogEventFormatter m_formatter = new WindowsColouredConsoleFormatter();
    private SocketClient m_socketCilent;
    private ConsoleInput m_consoleInput;
    private boolean m_commandMode = false;
    private Win32ConsoleAPI m_consoleAPI = new Win32ConsoleAPI();
    private StringBuilder m_currentCommand = new StringBuilder();
    private Set<String> m_filters = new CopyOnWriteArraySet<String>();
    private long m_lastEventTime = 0;
    private Object m_outputLock = new Object();
    private int m_blankCount = 0;

    public ConsoleReceiver()
    {
        m_socketCilent = new SocketClient();
        m_socketCilent.addLogEventListener(this);

        m_consoleInput = new ConsoleInput(new ConsoleInputListener()
        {
            public void onNewChar(char c)
            {
                processChar(c);
            }
        });

        m_filters.add("a");

        startBlanksTimer();
    }

    private void startBlanksTimer()
    {
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                long timeNow = System.currentTimeMillis();

                if(m_lastEventTime > 0 && m_blankCount < 3)
                {
                    long delta = timeNow - m_lastEventTime;

                    if(delta > 5000)
                    {
                        synchronized (m_outputLock)
                        {
                            System.out.println();
                            m_blankCount++;
                        }
                    }
                }
            }
        };

        Timer timer = new Timer("ConsoleReceiver:BlanksTimer", true);
        timer.schedule(task, 2000, 2000);
    }

    private void processChar(char c)
    {
        int i = (int) c;
        if(c == '\n' || c == '\r')
        {
            if(m_commandMode)
            {
                if(m_currentCommand.length() == 0)
                {
                    m_consoleAPI.cls();
                    m_commandMode = false;
                }
                else
                {
                    processCommand(m_currentCommand.toString());
                    m_currentCommand.setLength(0);
                }
            }
            else
            {
                m_commandMode = true;
                m_consoleAPI.cls();
                System.out.print("> ");
            }
        }
        else if(c == 3)
        {
            System.exit(0);
        }
        else
        {
            if(m_commandMode)
            {
                System.out.print(c);
                m_currentCommand.append(c);
            }
        }
    }

    private void processCommand(String command)
    {
        System.out.println("");
        System.out.println("Command was : " + command);
        System.out.print("> ");

        String trimmed = command.trim();
        if(trimmed.startsWith("+"))
        {
            String filter = trimmed.substring(1, trimmed.length());
            m_filters.add(filter);
            System.out.println("Added filter '" + filter
                               + "', there are now "
                               + m_filters.size()
                               + " active filters");
        }
        else if(trimmed.startsWith("-"))
        {
            String filter = trimmed.substring(1, trimmed.length());
            m_filters.remove(filter);
            System.out.println("Removed filter '" + filter
                               + "', there are now "
                               + m_filters.size()
                               + " active filters");
        }
        else if(trimmed.equals("list"))
        {
            Set<String> filters = m_filters;
            System.out.println("Filter list :");
            for(String string : filters)
            {
                System.out.println(string);
            }
        }

    }

    public void start()
    {
        try
        {
            m_socketCilent.send(new SubscriptionRequestMessage());
        }
        catch (LoggingMessageSenderException e)
        {
            throw new RuntimeException("Failed to send subscription message", e);
        }
    }

    public void onNewLogEvent(LogEvent event)
    {
        if(!m_commandMode)
        {
            if(passesFilters(event))
            {
                synchronized (m_outputLock)
                {
                    System.out.println(m_formatter.format(event));
                    m_lastEventTime = System.currentTimeMillis();
                    m_blankCount = 0;
                }
            }
        }
    }

    private boolean passesFilters(LogEvent event)
    {
        boolean passes;

        if(m_filters.isEmpty())
        {
            passes = true;
        }
        else
        {
            passes = false;
            for(String filter : m_filters)
            {
                if(event.getMessage().contains(filter))
                {
                    passes = true;
                    break;
                }
            }
        }

        return passes;
    }

    public void addConnectionPoints(List<InetSocketAddress> addreses)
    {
        for(InetSocketAddress inetSocketAddress : addreses)
        {
            m_socketCilent.addConnectionPoint(inetSocketAddress);
        }
    }
}