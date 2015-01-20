package com.logginghub.messaging2;

import java.util.concurrent.Exchanger;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.logginghub.messaging2.RemoteListener;
import com.logginghub.messaging2.proxy.ReceiverMessageProxyConnector;
import com.logginghub.messaging2.proxy.SenderMessageProxy;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class MethodInvocationTest extends AbstractTwoClientBase {

    @RemoteListener public interface TestInterfaceListener {
        void onCallback(String message);
    }

    public interface TestInterface {
        String ask(String question);

        void async(TestInterfaceListener listener);
    }

    public static class TestImplementation implements TestInterface {
        public String ask(String question) {
            return "What?";
        }

        public void async(final TestInterfaceListener listener) {
            // If we call straight back here, we'll cause a deadlock - we _must_
            // dispatch this in another thread so this method can return
            // cleanly. Otherwise why the hell aren't we just using a return
            // value?

            // I've just changed this to make void @RemoteListener calls
            // non-blocking so we dont need this extra dispatcher. Its a bit of
            // a hidden voodoo that'll probably come back to bite me, but it
            // makes the pattern simpler

            // Thread thread = new Thread(new Runnable() {
            // public void run() {
            listener.onCallback("callback");
            // }
            // });
            // thread.start();
        }
    }

    @Test public void test() throws InterruptedException, TimeoutException {

//        TestInterface testInterface = SenderMessageProxy.newInstance(TestInterface.class, kryoClientA, "clientB", "testInterface");
//        ReceiverMessageProxyConnector.bind(new TestImplementation(), kryoClientB, "testInterface");
//
//        assertThat(testInterface.ask("Question?"), is("What?"));
    }

    @Test public void test_remote_listener() throws InterruptedException, TimeoutException {

//        TestInterface testInterface = SenderMessageProxy.newInstance(TestInterface.class, kryoClientA, "clientB", "testInterface");
//        ReceiverMessageProxyConnector.bind(new TestImplementation(), kryoClientB, "testInterface");
//
//        final Exchanger<String> exchanger = new Exchanger<String>();
//        testInterface.async(new TestInterfaceListener() {
//            public void onCallback(String message) {
//                try {
//                    exchanger.exchange(message);
//                }
//                catch (InterruptedException e) {}
//            }
//        });
//
//        String asyncResponse = exchanger.exchange(null, 10, TimeUnit.SECONDS);
//        assertThat(asyncResponse, is("callback"));
    }

}
