package com.logginghub.utils.execution.tasks;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import com.logginghub.utils.execution.Task;

public class SocketTestTask implements Task<Boolean>
{
    private int port;
    private String host;

    
    public SocketTestTask(String host, int port)
    {
        this.port = port;
        this.host = host;
    }

    public Boolean run()
    {
        boolean success;

        
        try
        {
            Socket socket = new Socket();
            SocketAddress address = new InetSocketAddress(host, port);
            System.out.println("Connecting to " + address);
            socket.connect(address, 1000);
            System.out.println("Connected");
            success = true;
        }
        catch (UnknownHostException e)
        {
            success = false;
        }
        catch (IOException e)
        {
            success = false;
        }

        return success;
    }
}
