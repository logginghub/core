package com.logginghub.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer implements Runnable
{
    private int port;

    public EchoServer(int port)
    {
        this.port = port;
    }

    public void start()
    {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run()
    {
        try
        {
            runInternal();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private void runInternal() throws IOException
    {
        System.out.println("Binding to port " + port);
        ServerSocket socket = new ServerSocket(port);

        while (true)
        {
            Socket client = socket.accept();
            System.out.println("Accepted connection from " + client);

            InputStream inputStream = client.getInputStream();
            OutputStream outputStream = client.getOutputStream();
            int read = 0;

            while (read != -1)
            {
                read = inputStream.read();

                outputStream.write(read);
                outputStream.flush();
            }

            inputStream.close();
            outputStream.close();
        }
    }
    
    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException
    {
        int port = 7;

        if (args.length > 1)
        {
            port = Integer.parseInt(args[0]);
        }

        EchoServer server = new EchoServer(port);
        server.run();
    }
}
