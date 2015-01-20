package com.logginghub.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SSLMessagingClient extends MessagingClient
{
    private static Logger logger = Logger.getLogger(SSLMessagingClient.class.getName());
    

    static
    {
        System.setProperty("javax.net.ssl.keyStore", "mySrvKeystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        System.setProperty("javax.net.ssl.trustStore", "jssecacerts");
    }

    public SSLMessagingClient(String server, int port)
    {
        super(server, port);
    }

    public SSLMessagingClient()
    {

    }

    @Override protected Socket createSocket(InetSocketAddress address)
                    throws IOException
    {
        if (getProxy() == null)
        {
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(address.getHostName(),
                                                                            address.getPort());
            return sslsocket;
        }
        else
        {
            logger.info(String.format("Creating socket using proxy [%s]",
                                      getProxy()));

            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(new Socket(getProxy()),
                                                                            address.getHostName(),
                                                                            address.getPort(),
                                                                            true);
            return sslsocket;
        }
    }   
}
