package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.logginghub.utils.Bucket;
import com.logginghub.utils.BucketMatcher;
import com.logginghub.utils.MutableBoolean;

public class TestBucket {
    private Bucket<String> bucket;

    @Before public void setup() {
        bucket = new Bucket<String>();
    }

    @Test public void test() {
        assertThat(bucket.size(), is(0));
        bucket.add("foo");
        assertThat(bucket.size(), is(1));
        assertThat(bucket.get(0), is("foo"));

        List<String> events = bucket.getEvents();
        assertThat(events.size(), is(1));
        assertThat(events.get(0), is("foo"));
    }

    @Test public void testThreading() throws InterruptedException {
        final MutableBoolean happy = new MutableBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.execute(new Runnable() {
            public void run() {
                bucket.waitForMessages(1, 20, TimeUnit.SECONDS);
                happy.setValue(true);
                latch.countDown();
            }
        });

        assertThat(happy.booleanValue(), is(false));
        bucket.add("foo");
        assertThat(latch.await(1, TimeUnit.SECONDS), is(true));
        assertThat(happy.booleanValue(), is(true));
    }

    @Test public void testMatchedThreading() throws InterruptedException {
        final MutableBoolean happy = new MutableBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.execute(new Runnable() {
            public void run() {
                bucket.waitFor(new BucketMatcher<String>() {
                    public boolean matches(String t) {
                        return "moo".equals(t);
                    }
                }, 20, TimeUnit.SECONDS);

                happy.setValue(true);
                latch.countDown();
            }
        });

        assertThat(happy.booleanValue(), is(false));
        bucket.add("foo");
        Thread.sleep(500);
        assertThat(happy.booleanValue(), is(false));
        bucket.add("moo");
        assertThat(latch.await(1, TimeUnit.SECONDS), is(true));
    }

}
