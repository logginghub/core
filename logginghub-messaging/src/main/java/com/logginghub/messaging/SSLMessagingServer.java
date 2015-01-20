package com.logginghub.messaging;

import java.io.IOException;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import com.logginghub.utils.ExceptionHandler;

/**
 * SSL flavour of the messaging server.
 * @author James
 */
public class SSLMessagingServer extends MessagingServer
{
//    static
//    {
//        System.setProperty("javax.net.ssl.keyStore", "mySrvKeystore");
//        System.setProperty("javax.net.ssl.keyStorePassword", "password");
//    }
    static
    {
        System.setProperty("javax.net.ssl.keyStore", "mySrvKeystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", "jssecacerts");
    }
    
    public SSLMessagingServer(int port, ExceptionHandler exceptionHandler)
    {
        setPort(port);
        setExceptionHandler(exceptionHandler);
    }

    public SSLMessagingServer()
    {

    }

    
    @Override protected ServerSocket createServerSocket() throws IOException
    {
        SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslserversocket = (SSLServerSocket) sslserversocketfactory.createServerSocket(getPort());
        return sslserversocket;
    }
}
