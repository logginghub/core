package com.logginghub.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.logginghub.messaging.Level1BlockingClient;
import com.logginghub.messaging.Level1BlockingServer;
import com.logginghub.messaging.Level2BlockingClient;
import com.logginghub.messaging.Level2BlockingServer;
import com.logginghub.messaging.Level3BlockingClient;
import com.logginghub.messaging.Level3BlockingServer;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Timeout;
import com.logginghub.messaging.fixture.HelloListener;
import com.logginghub.messaging.fixture.HelloProvider;
import com.logginghub.messaging.fixture.HelloService;
import com.logginghub.messaging2.encoding.AllTypesDummyObject;

@Ignore // TODO : doesn't run from maven
public class TestBlocking {

    @Rule public ExpectedException exception = ExpectedException.none();
    
    @Before public void before() {
//        Logger.setRootLevel(Logger.debug);
//        Logger.setLevel(IntegerHeaderFrameDecoder.class, Logger.info);
//        Logger.setLevel(IntegerHeaderFrameEncoder.class, Logger.info);        
    }
    
    @Test public void test_client_cant_connect() { 
        Level1BlockingClient client = new Level1BlockingClient();
        int port = NetUtils.findFreePort();
        client.addConnectionPoint(new InetSocketAddress("localhost", port));
        
        exception.expect(RuntimeException.class);
        exception.expectMessage("Connection refused");
        client.connect();
    }

    @Test public void test_client_server_receive_next_multiple() {

        Level1BlockingClient client = new Level1BlockingClient();
        Level1BlockingServer server = new Level1BlockingServer();
        server.randomisePort();

        server.bind();

        client.addConnectionPoint(server);

        client.connect();

        client.send("Message 1");

        String received = server.receiveNext();
        assertThat(received, is("Message 1"));
        
        Timeout.defaultTimeout.setTimeout(1, TimeUnit.SECONDS);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Timed out fired waiting for events");
        server.receiveNext();
    }
    
    @Test public void test_client_server() {

        Level1BlockingClient client = new Level1BlockingClient();
        client.setBlockingTimeout(5, TimeUnit.SECONDS);

        Level1BlockingServer server = new Level1BlockingServer();
        server.randomisePort();

        server.bind();

        client.addConnectionPoint(server);

        client.connect();

        AllTypesDummyObject allTypesDummyObject = new AllTypesDummyObject();
        allTypesDummyObject.setStringObject("This is a different value");
        client.send(allTypesDummyObject);

        AllTypesDummyObject received = server.receiveNext();

        assertThat(received, is(allTypesDummyObject));

        client.close();
        server.close();
    }

    @Test public void test_client_to_client() {
        
        Level2BlockingClient clientA = new Level2BlockingClient();
        Level2BlockingClient clientB = new Level2BlockingClient();
        Level2BlockingServer server = new Level2BlockingServer();

        server.bind();

        clientA.addConnectionPoint(server);
        clientB.addConnectionPoint(server);

        clientA.connect();
        clientB.connect();

        clientB.subscribe("clientBChannel");

        AllTypesDummyObject allTypesDummyObject = new AllTypesDummyObject();
        allTypesDummyObject.setStringObject("This is a different value");
        
        clientA.send("clientBChannel", "", allTypesDummyObject);

        AllTypesDummyObject received = clientB.receiveNext("clientBChannel");

        assertThat(received, is(allTypesDummyObject));

        clientA.close();
        clientB.close();
        server.close();
    }

    
    @Test public void test_client_to_client_multi_channel() {

        Level2BlockingClient clientA = new Level2BlockingClient();
        Level2BlockingClient clientB = new Level2BlockingClient();
        Level2BlockingServer server = new Level2BlockingServer();

        server.bind();

        clientA.addConnectionPoint(server);
        clientB.addConnectionPoint(server);

        clientA.connect();
        clientB.connect();

        clientB.subscribe("clientBChannelOne");
        clientB.subscribe("clientBChannelTwo");

        AllTypesDummyObject allTypesDummyObject = new AllTypesDummyObject();
        allTypesDummyObject.setStringObject("This is a different value");
        
        clientA.send("clientBChannelOne", "", allTypesDummyObject);
        clientA.send("clientBChannelTwo", "", new String("hello"));
        
        AllTypesDummyObject receivedOne = clientB.receiveNext("clientBChannelOne");
        String receivedTwo= clientB.receiveNext("clientBChannelTwo");

        assertThat(receivedOne, is(allTypesDummyObject));
        assertThat(receivedTwo, is("hello"));

        clientA.close();
        clientB.close();
        server.close();
    }   
    
    @Test public void test_client_to_server_service() throws IllegalArgumentException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        
        Level3BlockingClient client = new Level3BlockingClient();
        client.setName("Client");
        
        Level3BlockingServer server = new Level3BlockingServer();

        server.bind();
        client.addConnectionPoint(server);
        client.connect();

        server.register("hello", HelloService.class, new HelloProvider());
        
        HelloService hello = client.getService("hello");
        
        assertThat(hello.hello("James"), is("Hello James"));
        
        final Bucket<String> helloBucket = new Bucket<String>();
        HelloListener listener = new HelloListener() {            
            public void onHello(String name) {
                helloBucket.add(name);
            }
        };
        hello.addListener(listener);
        
        assertThat(hello.hello("Sarah"), is("Hello Sarah"));
        
        assertThat(helloBucket.size(), is(1));
        assertThat(helloBucket.get(0), is("Sarah"));

        hello.removeListener(listener);
        
        assertThat(hello.hello("Jenna"), is("Hello Jenna"));
        
        assertThat(helloBucket.size(), is(1));
        assertThat(helloBucket.get(0), is("Sarah"));
        
        client.close();
        server.close();
    }
      
    @Test public void test_client_to_client_service() throws IllegalArgumentException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Level3BlockingClient clientA = new Level3BlockingClient();
        Level3BlockingClient clientB = new Level3BlockingClient();
        
        clientA.setName("ClientA");
        clientB.setName("ClientB");

        Level3BlockingServer server = new Level3BlockingServer();
        server.bind();

        clientA.addConnectionPoint(server);
        clientB.addConnectionPoint(server);
        
        clientA.connect();
        clientB.connect();

        clientB.setHomeChannel("clientB");
        clientA.setHomeChannel("clientA");
        
        clientB.register("hello", HelloService.class, new HelloProvider());
        
        HelloService hello = clientA.getService("clientB", "hello");
        
        assertThat(hello.hello("James"), is("Hello James"));
        
        final Bucket<String> helloBucket = new Bucket<String>();
        HelloListener listener = new HelloListener() {            
            public void onHello(String name) {
                helloBucket.add(name);
            }
        };
        hello.addListener(listener);
        
        assertThat(hello.hello("Sarah"), is("Hello Sarah"));
        
        assertThat(helloBucket.size(), is(1));
        assertThat(helloBucket.get(0), is("Sarah"));

        hello.removeListener(listener);
        
        assertThat(hello.hello("Jenna"), is("Hello Jenna"));
        
        assertThat(helloBucket.size(), is(1));
        assertThat(helloBucket.get(0), is("Sarah"));
        
        clientA.close();
        clientB.close();
        server.close();
    }
    
    @Test public void test_client_to_client_multiple_providers() throws IllegalArgumentException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        Level3BlockingClient clientA = new Level3BlockingClient();
        Level3BlockingClient clientB = new Level3BlockingClient();
        Level3BlockingClient clientC = new Level3BlockingClient();
        
        clientA.setName("ClientA");
        clientB.setName("ClientB");
        clientC.setName("ClientC");

        Level3BlockingServer server = new Level3BlockingServer();
        server.bind();

        clientA.addConnectionPoint(server);
        clientB.addConnectionPoint(server);
        clientC.addConnectionPoint(server);
        
        clientA.connect();
        clientB.connect();
        clientC.connect();

        clientA.setHomeChannel("clientA");
        clientB.setHomeChannel("clientB");
        clientC.setHomeChannel("clientC");
        
        clientB.register("hello", HelloService.class, new HelloProvider());
        clientC.register("hello", HelloService.class, new HelloProvider() {
            @Override public String hello(String name) {
                String result = "Goodbye " + name;
                notify(name);
                return result;
            }
        });
        
        HelloService helloB = clientA.getService("clientB", "hello");
        HelloService helloC = clientA.getService("clientC", "hello");
        
        assertThat(helloB.hello("James"), is("Hello James"));
        assertThat(helloC.hello("James"), is("Goodbye James"));        
        
        clientA.close();
        clientB.close();
        clientC.close();
        server.close();
    }

}
