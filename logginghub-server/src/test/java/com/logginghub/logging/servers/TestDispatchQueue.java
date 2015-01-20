package com.logginghub.logging.servers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.logginghub.logging.servers.DispatchQueue;
import com.logginghub.logging.servers.DispatchQueue.DispatchQueueConfiguration;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;

public class TestDispatchQueue {

    @Test public void test() throws Exception {

        DispatchQueue<String> queue = new DispatchQueue<String>();
        DispatchQueueConfiguration configuration = new DispatchQueueConfiguration();
        configuration.asynchronousQueueDiscardSize = 5;
        configuration.asynchronousQueueWarningSize = 2;
        configuration.name = "Test Queue";

        queue.configure(configuration, null);
        queue.start();
        
        final Bucket<String> results = new Bucket<String>();

        final CountDownLatch latch = new CountDownLatch(1);
        queue.addDestination(new Destination<String>() {
            @Override public void send(String t) {
                try {
                    latch.await();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }

                results.add(t);
            }
        });

        // This will leave the queue and be dispatched straight away
        queue.send("Hello 1");

        // So this is the first item that will get left on the queue
        queue.send("Hello 2");
        queue.send("Hello 3");

        // This one should trigger a warning
        queue.send("Hello 4");

        // These shouldnt, due to the warning throttler
        queue.send("Hello 5");
        queue.send("Hello 6");

        // These should be discarded
        queue.send("Hello 7");
        queue.send("Hello 8");

        latch.countDown();

        queue.waitForQueueToDrain();

        assertThat(results.size(), is(6));
        assertThat(results.get(0), is("Hello 1"));
        assertThat(results.get(1), is("Hello 2"));
        assertThat(results.get(2), is("Hello 3"));
        assertThat(results.get(3), is("Hello 4"));
        assertThat(results.get(4), is("Hello 5"));
        assertThat(results.get(5), is("Hello 6"));

    }

}
