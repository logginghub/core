package com.logginghub.messaging2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;
import com.logginghub.messaging2.api.Message;
import com.logginghub.messaging2.api.MessageListener;
import com.logginghub.messaging2.api.MessagingInterface;
import com.logginghub.messaging2.kryo.KryoClient;
import com.logginghub.messaging2.kryo.KryoHub;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.ThreadUtils;

public class AbstractTwoClientBase {

    static {
//        Log.set(Log.LEVEL_DEBUG);
    }
    
    protected KryoHub hub;
    protected KryoClient kryoClientA;
    protected KryoClient kryoClientB;
    
    protected MessagingInterface clientA;
    protected MessagingInterface clientB;
    
    protected Bucket<Message> clientABucket = new Bucket<Message>();
    protected Bucket<Message> clientBBucket = new Bucket<Message>();

    private int startThreads;
    private MessageListener messageListenerA;
    private MessageListener messageListenerB;
    
    
    @Before public void setup() {
        
        startThreads = Thread.activeCount();
        
        int port = NetUtils.findFreePort();
        
        System.out.println("-------------------------------------------------------------------------------------");
        InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", port);
        hub = new KryoHub();
        kryoClientA = new KryoClient("clientA");
        kryoClientB = new KryoClient("clientB");

        clientA = kryoClientA;
        clientB = kryoClientB;
        
        kryoClientA.addConnectionPoint(inetSocketAddress);
        kryoClientB.addConnectionPoint(inetSocketAddress);

        messageListenerA = new MessageListener() {
            public void onNewMessage(Message message) {
                System.out.println("Client A recived : " + message);
                clientABucket.add(message);                
            }
        };
        kryoClientA.addMessageListener(messageListenerA);

        messageListenerB = new MessageListener() {
            public void onNewMessage(Message message) {
                System.out.println("Client B recived : " + message);
                clientBBucket.add(message);
            }
        };
        kryoClientB.addMessageListener(messageListenerB);

        hub.start(port);
        hub.waitUntilBound();
        kryoClientA.connect();
        kryoClientB.connect();
    }

    @After public void teardown() {
        if (hub != null) {
            hub.shutdown();
            hub = null;
        }

        if (kryoClientA != null) {
            kryoClientA.removeMessageListener(messageListenerA);
            kryoClientA.stop();
            kryoClientA = null;
            clientA = null;
            messageListenerA = null;
        }

        if (kryoClientB != null) {
            kryoClientB.removeMessageListener(messageListenerB);
            kryoClientB.stop();
            kryoClientB = null;
            clientB = null;
            messageListenerB = null;
        }
        
        
        int endThreads;
        int count = 0;
        while((endThreads = Thread.activeCount()) > startThreads){
            ThreadUtils.sleep(10);
            count++;
            if(count > 1000){
                break;
            }
        }
        assertThat("It looks like you are leaking threads", endThreads, is(startThreads));
        
//        Kryo.getContext().reset();
        
        System.gc();
        System.gc();
                       
    }

}
