package com.logginghub.utils.net.objectclient;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.logginghub.utils.Bucket;
import com.logginghub.utils.net.objectclient.ObjectSocketClient;
import com.logginghub.utils.net.objectclient.ObjectSocketClientListener;
import com.logginghub.utils.net.objectclient.ObjectSocketClient.Status;

public class ObjectSocketClientTest
{
    @Test public void test() throws InterruptedException
    {
        String server = "localhost";
        int port = 6666;

        final Bucket<Status> statusChanges = new Bucket<Status>();
        final Bucket<Object> objects = new Bucket<Object>();
        
        ObjectSocketClient client = new ObjectSocketClient(server, port);
        assertEquals(Status.Disconnected, client.getStatus());
        
        client.setReconnectionTimeout(1000);
        client.addSocketClientListener(new ObjectSocketClientListener()
        {
            public void onStatusChanged(Status oldStatus, Status newStatus)
            {
                statusChanges.add(newStatus);
            }

            public void onNewObject(Object object)
            {
                objects.add(object);
            }
        });
        client.start();
        Thread.sleep(900);
        client.stop();
        
        assertEquals(0, objects.size());
        assertEquals(2, statusChanges.size());
        assertEquals(Status.Disconnected, client.getStatus());
    }
}
