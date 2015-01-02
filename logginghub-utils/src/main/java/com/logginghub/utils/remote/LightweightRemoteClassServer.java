package com.logginghub.utils.remote;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.WorkerThread;

public class LightweightRemoteClassServer extends WorkerThread
{
    private static Logger logger = Logger.getLogger(LightweightRemoteClassServer.class.getName());
    private ExecutorService m_pool = Executors.newCachedThreadPool();
    private ClasspathResolver m_resolver = new ClasspathResolver();
    private final int m_port;
    private ServerSocket m_serverSocket;

    public static void main(String[] args) throws IOException
    {
        int port = Integer.parseInt(args[0]);
        LightweightRemoteClassServer server = new LightweightRemoteClassServer(port);
        server.start();
    }

    public LightweightRemoteClassServer(int port)
    {
        super("LightweightRemoteClassServer");
        m_port = port;
    }

    @Override protected void onRun()
    {
        if (m_serverSocket == null)
        {
            try
            {
                m_serverSocket = new ServerSocket(m_port);
                logger.info(String.format("Succesfully bound to port %d",
                                          m_port));
            }
            catch (IOException e)
            {
                FileUtils.closeQuietly(m_serverSocket);
                m_serverSocket = null;
            }
        }

        Socket socket = null;
        try
        {
            socket = m_serverSocket.accept();
            logger.info(String.format("Connection accepted from %s",
                                      socket.toString()));

        }
        catch (IOException e)
        {
            FileUtils.closeQuietly(m_serverSocket);
            m_serverSocket = null;
        }

        if (socket != null)
        {
            LRCSSocketHandler handler = new LRCSSocketHandler(socket,
                                                              m_resolver);

            m_pool.execute(handler);
        }
    }
}
