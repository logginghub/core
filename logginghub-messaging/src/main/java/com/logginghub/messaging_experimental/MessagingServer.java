package com.logginghub.messaging_experimental;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.logginghub.utils.WorkerThread;

public class MessagingServer
{
    public final static int defaultPort = 59771;
    private int port = defaultPort;
    private WorkerThread thread = new WorkerThread("ServerThread")
    {
        @Override protected void onRun() throws Throwable
        {
            serverMainLoop();
        }
    };

    public void start()
    {
        thread.start();
    }

    protected void serverMainLoop()
    {
        /* todo : implement me 
        ServerSocket socket = new ServerSocket(port);
        while (thread.isRunning())
        {
            try
            {
                Socket accepted = socket.accept();
                MessagingClient client = new MessagingClient(socket);
            }
            catch (IOException e)
            {}
        }
        */
    }
}
