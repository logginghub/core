package com.logginghub.messaging;

import com.logginghub.messaging.directives.ResponseMessage;
import com.logginghub.messaging.fixture.HelloListener;
import com.logginghub.messaging.fixture.HelloProvider;
import com.logginghub.messaging.fixture.HelloService;
import com.logginghub.messaging.netty.RequestContext;
import com.logginghub.messaging.netty.RequestContextServerMessageListener;
import com.logginghub.messaging.netty.ServerConnectionListener;
import com.logginghub.messaging.netty.ServerHandler;
import com.logginghub.messaging2.encoding.AllTypesDummyObject;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.OneWayExchanger;
import com.logginghub.utils.ReusableLatch;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.Timeout;
import com.logginghub.utils.logging.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Ignore // TODO : dies during maven test run
public class TestNonBlocking {

    @Rule public ExpectedException exception = ExpectedException.none();

    @Before public void before() {
//        Logger.setRootLevel(Logger.debug);
//        Logger.setLevel(IntegerHeaderFrameDecoder.class, Logger.info);
//        Logger.setLevel(IntegerHeaderFrameEncoder.class, Logger.info);
//        Logger.setLevel(ReflectionSerialiser.class, Logger.info);
    }

    @Test public void test_multiple_subscribe() {

        Level2AsyncClient clientA = new Level2AsyncClient("ClientA");

        Level2AsyncServer server = new Level2AsyncServer("Server");

        server.bind().await();
        clientA.addConnectionPoint(server);
        clientA.connect().await();

        clientA.subscribe("broadcast").awaitResponse();
        clientA.subscribe("broadcast").awaitResponse();

        AllTypesDummyObject allTypesDummyObject = new AllTypesDummyObject();
        allTypesDummyObject.setStringObject("This is a different value");

        MessageBucket clientAMessageBucket = new MessageBucket();

        clientA.addMessageListener(clientAMessageBucket);

        server.broadcast("broadcast", allTypesDummyObject);

        clientAMessageBucket.waitForMessages(1);

        assertThat(clientAMessageBucket.size(), is(1));
        
        assertThat((AllTypesDummyObject) clientAMessageBucket.get(0), is(allTypesDummyObject));

        clientA.close();
        server.close();
    }
    
    @Test public void test_server_broadcast() {

        Level2AsyncClient clientA = new Level2AsyncClient("ClientA");
        Level2AsyncClient clientB = new Level2AsyncClient("ClientB");

        Level2AsyncServer server = new Level2AsyncServer("Server");

        server.bind().await();
        clientA.addConnectionPoint(server);
        clientA.connect().await();

        clientB.addConnectionPoint(server);
        clientB.connect().await();

        clientA.subscribe("broadcast").awaitResponse();
        clientB.subscribe("broadcast").awaitResponse();

        AllTypesDummyObject allTypesDummyObject = new AllTypesDummyObject();
        allTypesDummyObject.setStringObject("This is a different value");

        MessageBucket clientAMessageBucket = new MessageBucket();
        MessageBucket clientBMessageBucket = new MessageBucket();

        clientA.addMessageListener(clientAMessageBucket);
        clientB.addMessageListener(clientBMessageBucket);

        server.broadcast("broadcast", allTypesDummyObject);

        clientAMessageBucket.waitForMessages(1);
        clientBMessageBucket.waitForMessages(1);

        assertThat(clientBMessageBucket.size(), is(1));
        assertThat(clientAMessageBucket.size(), is(1));
        
        assertThat((AllTypesDummyObject) clientAMessageBucket.get(0), is(allTypesDummyObject));
        assertThat((AllTypesDummyObject) clientBMessageBucket.get(0), is(allTypesDummyObject));

        clientA.close();
        clientB.close();
        server.close();
    }
    
    @Test public void test_server_broadcast_after_disconnect() {
        
       
        Level2AsyncClient clientA = new Level2AsyncClient("ClientA");
        Level2AsyncClient clientB = new Level2AsyncClient("ClientB");
        Level2AsyncClient clientC = new Level2AsyncClient("ClientB");

        Level2AsyncServer server = new Level2AsyncServer("Server");

        server.bind().await();
        clientA.addConnectionPoint(server);
        clientB.addConnectionPoint(server);
        clientC.addConnectionPoint(server);        
        
        clientA.connect().await();
        clientB.connect().await();
        clientC.connect().await();

        clientA.subscribe("broadcast").awaitResponse();
        clientB.subscribe("broadcast").awaitResponse();
        clientC.subscribe("broadcast").awaitResponse();

        AllTypesDummyObject allTypesDummyObject = new AllTypesDummyObject();
        allTypesDummyObject.setStringObject("This is a different value");

        MessageBucket clientAMessageBucket = new MessageBucket();
        MessageBucket clientBMessageBucket = new MessageBucket();
        MessageBucket clientCMessageBucket = new MessageBucket();

        clientA.addMessageListener(clientAMessageBucket);
        clientB.addMessageListener(clientBMessageBucket);
        clientC.addMessageListener(clientCMessageBucket);

        server.broadcast("broadcast", allTypesDummyObject);

        clientAMessageBucket.waitForMessages(1);
        clientBMessageBucket.waitForMessages(1);
        clientCMessageBucket.waitForMessages(1);

        assertThat(clientAMessageBucket.size(), is(1));
        assertThat(clientBMessageBucket.size(), is(1));
        assertThat(clientCMessageBucket.size(), is(1));
        
        assertThat((AllTypesDummyObject) clientAMessageBucket.get(0), is(allTypesDummyObject));
        assertThat((AllTypesDummyObject) clientBMessageBucket.get(0), is(allTypesDummyObject));
        assertThat((AllTypesDummyObject) clientCMessageBucket.get(0), is(allTypesDummyObject));

        final ReusableLatch latch = new ReusableLatch(1);
        server.addConnectionListener(new ServerConnectionListener() {
            public void onNewConnection(ServerHandler serverHandler) {}
            public void onDisconnection(ServerHandler serverHandler) {
                latch.countDown();
            }
            public void onBound(InetSocketAddress address) {}
            public void onBindFailure(InetSocketAddress address, Exception e) {}
        });
        
        clientB.close();
        latch.await();
        
        allTypesDummyObject.setCharType('z');
        server.broadcast("broadcast", allTypesDummyObject);

        clientAMessageBucket.waitForMessages(2);
        clientCMessageBucket.waitForMessages(2);

        assertThat(clientAMessageBucket.size(), is(2));
        assertThat(clientBMessageBucket.size(), is(1));
        assertThat(clientCMessageBucket.size(), is(2));
        
        assertThat((AllTypesDummyObject) clientAMessageBucket.get(1), is(allTypesDummyObject));
        assertThat((AllTypesDummyObject) clientCMessageBucket.get(1), is(allTypesDummyObject));
        
        clientA.close();
        clientC.close();
        server.close();
    }
    
    @Test public void test_server_already_bound_timeout() {

        int port = NetUtils.findFreePort();

        Level1AsyncServer server1 = new Level1AsyncServer();
        server1.setPort(port);

        Level1AsyncServer server2 = new Level1AsyncServer();
        server2.setPort(port);

        final ReusableLatch latch = new ReusableLatch(1);

        server1.bind(new AsycNotificationAdaptor() {
            @Override public void onSuccess() {
                latch.countDown();
            }
        });

        latch.await();

        // Right thats got something bound on that port, time to fire up the
        // second server
        final Bucket<Throwable> failure = new Bucket<Throwable>();
        final Bucket<Boolean> successes = new Bucket<Boolean>();
        final Bucket<Boolean> timeouts = new Bucket<Boolean>();

        server2.bind(1, TimeUnit.SECONDS, new AsycNotificationAdaptor() {
            @Override public void onSuccess() {
                successes.add(true);
            }

            @Override public void onFailure(Throwable reason) {
                failure.add(reason);
            }

            @Override public void onTimeout() {
                timeouts.add(true);
                latch.countDown();
            }
        });

        ThreadUtils.sleep(2000);

        server1.close();

        latch.await();

        assertThat(successes.size(), is(0));
        assertThat(failure.size(), is(0));
        assertThat(timeouts.size(), is(1));

        assertThat(timeouts.get(0), is(true));
    }

    @Test public void test_server_already_bound() {

        int port = NetUtils.findFreePort();

        Level1AsyncServer server1 = new Level1AsyncServer();
        server1.setPort(port);

        Level1AsyncServer server2 = new Level1AsyncServer();
        server2.setPort(port);

        final ReusableLatch latch = new ReusableLatch(1);

        server1.bind(new AsycNotificationAdaptor() {
            @Override public void onSuccess() {
                latch.countDown();
            }
        });

        latch.await();

        // Right thats got something bound on that port, time to fire up the
        // second server
        final Bucket<Boolean> successes = new Bucket<Boolean>();

        server2.bind(new AsycNotificationAdaptor() {
            @Override public void onSuccess() {
                successes.add(true);
                latch.countDown();
            }
        });

        ThreadUtils.sleep(1000);

        server1.close();

        latch.await();

        assertThat(successes.size(), is(1));
        assertThat(successes.get(0), is(true));
    }

    @Test public void test_client_connection_timeout() {

        Level1AsyncServer server = new Level1AsyncServer();

        Level1AsyncClient client = new Level1AsyncClient();
        client.addConnectionPoint(server);

        final Bucket<Throwable> failure = new Bucket<Throwable>();
        final Bucket<Boolean> successes = new Bucket<Boolean>();
        final Bucket<Boolean> timeouts = new Bucket<Boolean>();

        client.connect(1, TimeUnit.SECONDS, new AsycNotification() {
            public void onTimeout() {
                timeouts.add(true);
            }

            public void onSuccess() {
                successes.add(true);
            }

            public void onFailure(Throwable reason) {
                failure.add(reason);
            }
        });

        ThreadUtils.sleep(2000);

        timeouts.waitForMessages(1);

        assertThat(successes.size(), is(0));
        assertThat(failure.size(), is(0));
        assertThat(timeouts.size(), is(1));

        assertThat(timeouts.get(0), is(true));
    }

    @Test public void test_client_connection_eventual_success() throws Throwable {

        Level1AsyncServer server = new Level1AsyncServer();

        Level1AsyncClient client = new Level1AsyncClient();
        client.addConnectionPoint(server);

        final Bucket<Throwable> failure = new Bucket<Throwable>();
        final Bucket<Boolean> successes = new Bucket<Boolean>();
        final Bucket<Boolean> timeouts = new Bucket<Boolean>();

        client.connect(new AsycNotification() {
            public void onTimeout() {
                timeouts.add(true);
            }

            public void onSuccess() {
                successes.add(true);
            }

            public void onFailure(Throwable reason) {
                failure.add(reason);
            }
        });

        ThreadUtils.sleep(2000);

        assertThat(successes.size(), is(0));
        assertThat(failure.size(), is(0));
        assertThat(timeouts.size(), is(0));

        Notification notification = new Notification();
        server.bind(notification);
        notification.await();

        successes.waitForMessages(1);

        assertThat(successes.size(), is(1));
        assertThat(failure.size(), is(0));
        assertThat(timeouts.size(), is(0));

        client.close();
        server.close();
    }

    @Test public void test_client_server() throws Throwable {

        Level1AsyncClient client = new Level1AsyncClient();
        Level1AsyncServer server = new Level1AsyncServer();

        MessageBucket bucket = new MessageBucket();
        server.addMessageListener(bucket);

        Notification notification = new Notification();
        server.bind(notification);
        notification.await();

        client.addConnectionPoint(server);
        client.connect(notification);
        notification.await();

        AllTypesDummyObject allTypesDummyObject = new AllTypesDummyObject();
        allTypesDummyObject.setStringObject("This is a different value");
        client.send(allTypesDummyObject, notification);
        notification.await();

        bucket.waitForMessages(1);

        AllTypesDummyObject received = (AllTypesDummyObject) bucket.get(0);

        assertThat(received, is(allTypesDummyObject));

        client.close();
        server.close();
    }

    @Test public void test_client_server_request_response_success() throws Throwable {

        Level2AsyncClient client = new Level2AsyncClient("Client");
        Level2AsyncServer server = new Level2AsyncServer("Server");

        server.addListener(new RequestContextServerMessageListener() {
            public <T> void onNewMessage(Object message, ServerHandler receivedFrom, RequestContext requestContext) {
                requestContext.reply("Result!");
            }
        });
        

        Notification notification = new Notification();
        server.bind(notification);
        notification.await();

        client.addConnectionPoint(server);
        client.connect(notification);
        notification.await();

        AllTypesDummyObject allTypesDummyObject = new AllTypesDummyObject();
        allTypesDummyObject.setStringObject("This is a different value");

        final Bucket<String> responseBucket = new Bucket<String>();

        client.sendRequest(allTypesDummyObject, notification, new ResponseListener<String>() {
            public void onResponse(String response) {
                responseBucket.add(response);
            }

            public void onResponseFailure(Throwable reason) {}

            public void onResponseTimeout() {}
        });
        notification.await();

        responseBucket.waitForMessages(1);
        assertThat(responseBucket.get(0), is("Result!"));

        client.close();
        server.close();
    }

    @Test public void test_client_server_request_response_timeout() throws Throwable {

        Level2AsyncClient client = new Level2AsyncClient();
        client.setName("Client");

        Level2AsyncServer server = new Level2AsyncServer();

        Notification notification = new Notification();
        server.bind(notification);
        notification.await();

        client.addConnectionPoint(server);
        client.connect(notification);
        notification.await();

        AllTypesDummyObject allTypesDummyObject = new AllTypesDummyObject();
        allTypesDummyObject.setStringObject("This is a different value");

        client.setDefaultTimeout(500, TimeUnit.MILLISECONDS);

        final Bucket<String> responseBucket = new Bucket<String>();
        final Bucket<Boolean> timeouts = new Bucket<Boolean>();

        client.sendRequest(allTypesDummyObject, notification, new ResponseListener<String>() {
            public void onResponse(String response) {
                responseBucket.add(response);
            }

            public void onResponseFailure(Throwable reason) {}

            public void onResponseTimeout() {
                timeouts.add(true);
            }
        });
        notification.await();

        ThreadUtils.sleep(1000);

        assertThat(timeouts.size(), is(1));
        assertThat(responseBucket.size(), is(0));

        client.close();
        server.close();
    }

    @Test public void test_client_to_client_blocking_wrappers() throws Throwable {

        Level2AsyncClient clientA = new Level2AsyncClient("ClientA");
        Level2AsyncClient clientB = new Level2AsyncClient("ClientB");

        Level2AsyncServer server = new Level2AsyncServer("Server");

        server.bind().await();
        clientA.addConnectionPoint(server);
        clientA.connect().await();

        clientB.addConnectionPoint(server);
        clientB.connect().await();

        clientB.subscribe("clientBChannel").awaitResponse();

        AllTypesDummyObject allTypesDummyObject = new AllTypesDummyObject();
        allTypesDummyObject.setStringObject("This is a different value");

        MessageBucket clientBMessageBucket = new MessageBucket();
        clientB.addMessageListener(clientBMessageBucket);

        clientA.send("clientBChannel", allTypesDummyObject).await();

        clientBMessageBucket.waitForMessages(1);

        assertThat(clientBMessageBucket.size(), is(1));
        assertThat((AllTypesDummyObject) clientBMessageBucket.get(0), is(allTypesDummyObject));

        clientA.close();
        clientB.close();
        server.close();
    }

    @Test public void test_client_to_client() throws Throwable {

        

        Level2AsyncClient clientA = new Level2AsyncClient("ClientA");
        Level2AsyncClient clientB = new Level2AsyncClient("ClientB");
        Level2AsyncServer server = new Level2AsyncServer("Server");

        Notification notification = new Notification();
        server.bind(notification);
        notification.await();

        clientA.addConnectionPoint(server);
        clientA.connect(notification);
        notification.await();

        clientB.addConnectionPoint(server);
        clientB.connect(notification);
        notification.await();

        MessageBucket clientBMessageBucket = new MessageBucket();

        final Bucket<Boolean> responseBucket = new Bucket<Boolean>();
        clientB.subscribe("clientBChannel", notification, new ResponseListener<ResponseMessage>() {
            public void onResponse(ResponseMessage response) {
                responseBucket.add(response.isSuccess());
            }

            public void onResponseFailure(Throwable reason) {}

            public void onResponseTimeout() {}
        });
        notification.await();
        responseBucket.waitForMessages(1);
        assertThat(responseBucket.get(0), is(true));

        AllTypesDummyObject allTypesDummyObject = new AllTypesDummyObject();
        allTypesDummyObject.setStringObject("This is a different value");

        clientB.addMessageListener(clientBMessageBucket);

        clientA.send("clientBChannel", allTypesDummyObject, notification);

        clientBMessageBucket.waitForMessages(1);

        assertThat(clientBMessageBucket.size(), is(1));
        assertThat((AllTypesDummyObject) clientBMessageBucket.get(0), is(allTypesDummyObject));

        clientA.close();
        clientB.close();
        server.close();
    }

    @Test public void test_client_to_client_multi_channel() {

        Level2AsyncClient clientA = new Level2AsyncClient("ClientA");
        Level2AsyncClient clientB = new Level2AsyncClient("ClientB");
        Level2AsyncServer server = new Level2AsyncServer("Server");

        server.bind().await();

        clientA.addConnectionPoint(server);
        clientB.addConnectionPoint(server);

        clientA.connect().await();
        clientB.connect().await();

        clientB.subscribe("clientBChannelOne").awaitResponse();
        clientB.subscribe("clientBChannelTwo").awaitResponse();

        AllTypesDummyObject allTypesDummyObject = new AllTypesDummyObject();
        allTypesDummyObject.setStringObject("This is a different value");

        MessageBucket clientBChannelOneBucket = new MessageBucket();
        MessageBucket clientBChannelTwoBucket = new MessageBucket();

        clientB.addMessageListener("clientBChannelOne", clientBChannelOneBucket);
        clientB.addMessageListener("clientBChannelTwo", clientBChannelTwoBucket);

        clientA.send("clientBChannelOne", allTypesDummyObject).await();
        clientA.send("clientBChannelTwo", new String("hello")).await();

        clientBChannelOneBucket.waitForMessages(1);
        clientBChannelTwoBucket.waitForMessages(1);

        AllTypesDummyObject receivedOne = (AllTypesDummyObject) clientBChannelOneBucket.get(0);
        String receivedTwo = (String) clientBChannelTwoBucket.get(0);

        assertThat(receivedOne, is(allTypesDummyObject));
        assertThat(receivedTwo, is("hello"));

        clientA.close();
        clientB.close();
        server.close();
    }

    @Test public void test_client_to_server_service() throws IllegalArgumentException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InterruptedException, TimeoutException {

        Level3AsyncClient client = new Level3AsyncClient("Client");
        Level3AsyncServer server = new Level3AsyncServer("Server");

        server.bind().await();
        client.addConnectionPoint(server);
        client.connect().await();

        server.register("hello", HelloService.class, new HelloProvider());

        // HelloService hello = client.getService("hello").awaitService();

        final OneWayExchanger<HelloService> exchanger = new OneWayExchanger<HelloService>();
        
        client.getService("hello", new ServiceListener<HelloService>() {
            public void onServiceAvailable(HelloService t) {
                exchanger.set(t);
            }

            public void onServiceTimeout() {}

            public void onServiceFailure(Throwable reason) {}
        });

        HelloService hello = exchanger.get();

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

    @Test public void test_client_to_client_service() throws IllegalArgumentException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException,
                    InvocationTargetException, NoSuchMethodException {

        Level3AsyncClient clientA = new Level3AsyncClient("ClientA");
        Level3AsyncClient clientB = new Level3AsyncClient("ClientB");

        Level3AsyncServer server = new Level3AsyncServer("Server");
        server.bind().await();

        clientA.addConnectionPoint(server);
        clientB.addConnectionPoint(server);

        clientA.connect().await();
        clientB.connect().await();

        clientA.setHomeChannel("clientA").awaitResponse();
        clientB.setHomeChannel("clientB").awaitResponse();

        clientB.register("hello", HelloService.class, new HelloProvider());

        HelloService hello = clientA.getService("clientB", "hello", HelloService.class).awaitService();

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

    @Test public void test_client_to_client_multiple_providers() throws IllegalArgumentException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException,
                    InvocationTargetException, NoSuchMethodException {

        Level3AsyncClient clientA = new Level3AsyncClient("ClientA");
        Level3AsyncClient clientB = new Level3AsyncClient("ClientB");
        Level3AsyncClient clientC = new Level3AsyncClient("ClientC");

        Level3AsyncServer server = new Level3AsyncServer();
        server.bind().await();

        clientA.addConnectionPoint(server);
        clientB.addConnectionPoint(server);
        clientC.addConnectionPoint(server);

        clientA.connect().await();
        clientB.connect().await();
        clientC.connect().await();

        clientA.setHomeChannel("clientA").awaitResponse();
        clientB.setHomeChannel("clientB").awaitResponse();
        clientC.setHomeChannel("clientC").awaitResponse();

        clientB.register("hello", HelloService.class, new HelloProvider());
        clientC.register("hello", HelloService.class, new HelloProvider() {
            @Override public String hello(String name) {
                String result = "Goodbye " + name;
                notify(name);
                return result;
            }
        });

        HelloService helloB = clientA.getService("clientB", "hello", HelloService.class).awaitService();
        HelloService helloC = clientA.getService("clientC", "hello", HelloService.class).awaitService();

        assertThat(helloB.hello("James"), is("Hello James"));
        assertThat(helloC.hello("James"), is("Goodbye James"));

        clientA.close();
        clientB.close();
        clientC.close();
        server.close();
    }

    

    private void debugMode() {
        Timeout.defaultTimeout.setTimeout(10000, TimeUnit.SECONDS);
        Logger.setRootLevel(Logger.trace);
    }

}
