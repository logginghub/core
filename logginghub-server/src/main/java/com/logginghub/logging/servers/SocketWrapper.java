package com.logginghub.logging.servers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketWrapper
{
    private OutputStream m_outputStream;
    private InputStream m_inputStream;

    public SocketWrapper(Socket socket) throws IOException
    {
        m_inputStream = socket.getInputStream();
        m_outputStream = socket.getOutputStream();
    }
    
    
}
