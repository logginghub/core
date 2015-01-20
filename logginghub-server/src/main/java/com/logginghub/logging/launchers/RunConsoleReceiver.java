package com.logginghub.logging.launchers;



import java.net.InetSocketAddress;
import java.util.List;

import com.logginghub.logging.LoggingPorts;
import com.logginghub.logging.receivers.ConsoleReceiver;
import com.logginghub.utils.NetUtils;

public class RunConsoleReceiver
{
    public static void main(String[] args)
    {
        int defaultPort = LoggingPorts.getSocketHubDefaultPort();
        
        String addressesString;
        
        if(args.length == 0)
        {
            addressesString = "localhost";
        }
        else
        {
            addressesString = args[0];
        }
        
        List<InetSocketAddress> addreses = NetUtils.toInetSocketAddressList(addressesString, defaultPort);
        ConsoleReceiver receiver = new ConsoleReceiver();
        receiver.addConnectionPoints(addreses);
        receiver.start();
    }
}
