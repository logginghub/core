package com.logginghub.logging.container;

import java.net.InetSocketAddress;

import com.logginghub.messaging.Level3AsyncClient;
import com.logginghub.utils.ResourceUtils;

public class RemoteContainerClient {

    private Level3AsyncClient client;

    public static void main(String[] args) {
        
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String file = args[2];
        
        RemoteContainerClient client = new RemoteContainerClient();
        client.connect(host, port);
        client.send(ResourceUtils.read(file));
        client.close();
        
    }

    private void close() {
        client.close();
    }

    private void send(String read) {
        client.send("container", read).await();
    }

    private void connect(String host, int port) {
        client = new Level3AsyncClient();
        client.addConnectionPoint(new InetSocketAddress(host, port));
        client.connect().await();
    }
    
}
