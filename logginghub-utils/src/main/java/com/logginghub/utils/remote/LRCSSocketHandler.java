package com.logginghub.utils.remote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logginghub.utils.FileUtils;

public class LRCSSocketHandler implements Runnable
{
    private final Socket m_socket;
    private final ClasspathResolver m_resolver;
    private static Logger logger = Logger.getLogger(LRCSSocketHandler.class.getName());

    public LRCSSocketHandler(Socket socket, ClasspathResolver resolver)
    {
        m_socket = socket;
        m_resolver = resolver;
    }

    public void run()
    {
        ObjectOutputStream outputStream = null;
        ObjectInputStream inputStream = null;
      
        try
        {
            outputStream = new ObjectOutputStream(m_socket.getOutputStream());
            inputStream = new ObjectInputStream(m_socket.getInputStream());

            while (true)
            {
                String classname = (String) inputStream.readUnshared();
                byte[] classBytes = m_resolver.getClassBytes(classname);
                if (classBytes.length == 0)
                {
                    if (logger.isLoggable(Level.FINE))
                    {
                        logger.fine(String.format("Failed to resolve %s as a class, maybe its a resouce?",
                                                  classname));
                    }

                    classBytes = m_resolver.getResourceBytes(classname);
                }
                outputStream.writeUnshared(classBytes);

                if (classBytes.length > 0)
                {
                    logger.info(String.format("Request for class [%s] complete, %d bytes returned",
                                              classname,
                                              classBytes.length));
                }
                else
                {
                    logger.info(String.format("Request for class [%s] failed, class not found. %d bytes returned",
                                              classname,
                                              classBytes.length));
                }
            }
        }
        catch (SocketException se)
        {
            if (se.getMessage().equals("Connection reset"))
            {
                // Fine, the other side disconnected
            }
            else
            {
                se.printStackTrace();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        finally
        {
            logger.info(String.format("Terminating RCS session for socket %s", m_socket));
            
            FileUtils.closeQuietly(inputStream);
            FileUtils.closeQuietly(outputStream);
            FileUtils.closeQuietly(m_socket);
        }
    }
}
