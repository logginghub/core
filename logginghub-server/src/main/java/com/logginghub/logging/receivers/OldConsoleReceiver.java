package com.logginghub.logging.receivers;

import java.net.InetSocketAddress;
import java.util.List;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFormatter;
import com.logginghub.logging.LoggingPorts;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.logeventformatters.SingleLineLogEventTextFormatter;
import com.logginghub.logging.messaging.SocketReceiverThread;
import com.logginghub.utils.NetUtils;

public class OldConsoleReceiver implements LogEventListener
{
    private SocketReceiverThread m_receiver;
    private LogEventFormatter m_formatter = new SingleLineLogEventTextFormatter();

    public OldConsoleReceiver()
    {
        m_receiver = new SocketReceiverThread();
    }

    public void start()
    {
        m_receiver.addLogEventListener(this);
        m_receiver.start();
    }

    public void stop()
    {
        m_receiver.removeLogEventListener(this);
        m_receiver.stop();
    }

    private void addConnectionPoints(List<InetSocketAddress> addreses)
    {
        for(InetSocketAddress inetSocketAddress : addreses)
        {
            addConnectionPoint(inetSocketAddress);
        }
    }

    public void onNewLogEvent(LogEvent event)
    {
        System.out.println(m_formatter.format(event));
    }

    public static void main(String[] args)
    {
        int defaultPort = LoggingPorts.getSocketHubDefaultPort();
        List<InetSocketAddress> addreses = NetUtils.toInetSocketAddressList(args, defaultPort);
        OldConsoleReceiver receiver = new OldConsoleReceiver();
        receiver.addConnectionPoints(addreses);
        receiver.start();
        receiver.join();
    }

    private void join()
    {
        m_receiver.join();
    }

    public void addConnectionPoint(InetSocketAddress inetSocketAddress)
    {
        m_receiver.addConnectionPoint(inetSocketAddress);
    }

    public void waitUntilConnected()
    {
        m_receiver.waitUntilConnected();
    }
}
