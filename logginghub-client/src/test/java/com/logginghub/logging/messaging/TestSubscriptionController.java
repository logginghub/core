package com.logginghub.logging.messaging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messaging.SubscriptionController;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.LatchFuture;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.WorkerThread;

public class TestSubscriptionController {

    @Test public void test_dispatch() {
    
        final MutableInt firstSubscriptions = new MutableInt(0);
        final MutableInt lastSubscriptions = new MutableInt(0);
        
        SubscriptionController<Destination<String>, String> controller = new SubscriptionController<Destination<String>, String>() {

            @Override protected Future<Boolean> handleFirstSubscription(String channel) {
                firstSubscriptions.increment();
                return null;
            }

            @Override protected Future<Boolean> handleLastSubscription(String channel) {
                lastSubscriptions.increment();
                return null;

            }
        };
        
        final Bucket<String> data = new Bucket<String>();
        
        Destination<String> destination = new Destination<String>() {
            public void send(String t) {
                data.add(t);
            }
        };
        
        controller.addSubscription("channel", destination);
        
        controller.dispatch(Channels.toArray("channel"), "message");        
        assertThat(data.size(), is(1));
        assertThat(data.get(0), is("message"));
        assertThat(firstSubscriptions.value, is(1));
        assertThat(lastSubscriptions.value, is(0));
        
        controller.removeSubscription("channel", destination);
        assertThat(firstSubscriptions.value, is(1));
        assertThat(lastSubscriptions.value, is(1));
        
        controller.dispatch(Channels.toArray("channel"), "message2");        
        assertThat(data.size(), is(1));
        
    }
    
    @Test public void test_dispatch_fail() {
        
        final MutableInt firstSubscriptions = new MutableInt(0);
        final MutableInt lastSubscriptions = new MutableInt(0);
        
        SubscriptionController<Destination<String>, String> controller = new SubscriptionController<Destination<String>, String>() {

            @Override protected Future<Boolean> handleFirstSubscription(String channel) {
                firstSubscriptions.increment();
                return null;
            }

            @Override protected Future<Boolean> handleLastSubscription(String channel) {
                lastSubscriptions.increment();
                return null;

            }
        };
        
        final Bucket<String> data = new Bucket<String>();
        
        Destination<String> destination = new Destination<String>() {
            public void send(String t) {
                throw new RuntimeException("Failed");
            }
        };
        
        controller.addSubscription("channel", destination);
        assertThat(firstSubscriptions.value, is(1));
        assertThat(lastSubscriptions.value, is(0));
        assertThat(controller.hasSubscriptions("channel"), is(true));
        assertThat(controller.getSubscriptionCount("channel"), is(1));
        
        controller.dispatch(Channels.toArray("channel"), "message");        
        assertThat(data.size(), is(0));
        assertThat(controller.hasSubscriptions("channel"), is(false));
        assertThat(controller.getSubscriptionCount("channel"), is(0));
        assertThat(firstSubscriptions.value, is(1));
        assertThat(lastSubscriptions.value, is(1));
        
        
    }
    
    @Test public void testAddSubscription() throws Exception {

        final MutableInt subscriptionCount = new MutableInt(0);

        final LatchFuture<Boolean> future = new LatchFuture<Boolean>();

        SubscriptionController<Destination<String>, String> controller = new SubscriptionController<Destination<String>, String>() {

            @Override protected Future<Boolean> handleFirstSubscription(String channel) {
                subscriptionCount.increment();
                return future;
            }

            @Override protected Future<Boolean> handleLastSubscription(String channel) {
                return null;

            }
        };

        // Add the first subscription
        final LatchFuture<Boolean> future1 = (LatchFuture<Boolean>) controller.addSubscription("channelA", new Destination<String>() {
            public void send(String t) {

            }
        });

        // Add a second subscription - this shouldn't result in another _actual_ subscription as the
        // first one should have triggered that
        final LatchFuture<Boolean> future2 = (LatchFuture<Boolean>) controller.addSubscription("channelA", new Destination<String>() {
            public void send(String t) {

            }
        });

        // Make sure the counts are correct
        assertThat(subscriptionCount.value, is(1));
        assertThat(controller.getDestinations("channelA").size(), is(2));

        // Now check the futures are working - they should all be the same
        assertThat(future, is(sameInstance(future1)));
        assertThat(future1, is(sameInstance(future2)));

        assertThat(future.isDone(), is(false));

        // Start a thread to wait on the futures
        final WorkerThread thread = WorkerThread.execute("waiting thread", new Runnable() {
            public void run() {
                try {
                    future1.get();
                    future2.get();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        // Pretend we are the other thread returning the subscription result
        future.trigger(true);

        // Everything should be good now
        assertThat(future1.getWithDefaultTimeout(), is(true));
        assertThat(future2.getWithDefaultTimeout(), is(true));

        // Make sure the thread is done
        ThreadUtils.repeatUntilTrue(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return thread.isRunning() == false;
            }
        });
    }

}
