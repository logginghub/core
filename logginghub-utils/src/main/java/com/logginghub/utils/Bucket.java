package com.logginghub.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.logginghub.utils.logging.Logger;

/**
 * A simple bucket that is handy for testing listeners - pop things into buckets inside the listener
 * methods and then check them later. It also helps with concurrency providing latch based wait for
 * message methods.
 * 
 * @author James
 * 
 * @param <T>
 */
public class Bucket<T> implements Iterable<T>, StreamListener<T>, Destination<T> {
    private List<T> contents = new ArrayList<T>();
    private CountDownLatch countdownLatch;

    private static final Logger logger = Logger.getLoggerFor(Bucket.class);

    private Timeout timeout = Timeout.defaultTimeout;
    private String name = "";

    public Bucket() {

    }

    public Bucket(String name) {
        this.name = name;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public void clear() {
        contents.clear();
    }

    public void addAll(Collection<T> collection) {
        for (T t : collection) {
            add(t);
        }
    }

    public void add(T t) {
        synchronized (contents) {
            contents.add(t);
            if (countdownLatch != null) {
                countdownLatch.countDown();
            }

            logger.fine("Item added to bucket{}, it now contains {} items", (name.length() == 0 ? "" : " " + name), contents.size());
        }

        // Check for the waiting for a specific item latches
        Set<BucketMatcher<T>> keySet = matcherLatches.keySet();
        for (BucketMatcher<T> bucketMatcher : keySet) {
            if (bucketMatcher.matches(t)) {
                CountDownLatch latch = matcherLatches.get(bucketMatcher);
                latch.countDown();
            }
        }
    }

    public void waitForMessages(int numberOfMessages) {
        waitForMessages(numberOfMessages, timeout.getTime(), timeout.getUnits());
    }

    public void waitForMessages(int numberOfMessages, String interval) {
        waitForMessages(numberOfMessages, TimeUtils.parseInterval(interval), TimeUnit.MILLISECONDS);
    }

    private Map<BucketMatcher<T>, CountDownLatch> matcherLatches = new HashMap<BucketMatcher<T>, CountDownLatch>();

    public void waitFor(BucketMatcher<T> matcher, long time, TimeUnit units) {
        CountDownLatch latch = new CountDownLatch(1);
        synchronized (matcherLatches) {
            matcherLatches.put(matcher, latch);
        }

        boolean alreadyReceived = false;
        synchronized (contents) {
            for (T t : contents) {
                if (matcher.matches(t)) {
                    alreadyReceived = true;
                    break;
                }
            }
        }

        if (alreadyReceived) {
            // No need to wait, we already have that object
        }
        else {
            try {
                if (latch.await(time, units)) {
                    // Great, the object has arrived
                }
                else {
                    throw new RuntimeException("Timed out fired waiting for events.");
                }
            }
            catch (InterruptedException e) {
                throw new RuntimeException("Thread was interupted waiting for log events coming into the bucket");
            }
        }

        synchronized (matcherLatches) {
            matcherLatches.remove(matcher);
        }
    }

    public T popFirst() {
        T t;
        synchronized (contents) {
            t = contents.remove(0);
        }
        return t;
    }

    public void waitForMessages(int numberOfMessages, long time, TimeUnit units) {
        boolean wait = false;
        synchronized (contents) {
            int currentMessages = contents.size();
            if (currentMessages < numberOfMessages) {
                int diff = numberOfMessages - currentMessages;
                countdownLatch = new CountDownLatch(diff);
                wait = true;
            }
        }

        if (wait) {
            try {
                if (units != null) {
                    if (countdownLatch.await(time, units)) {
                        // Fine
                    }
                    else {
                        throw new RuntimeException("Timed out fired waiting for events.");
                    }
                }
                else {
                    countdownLatch.await();
                }
            }
            catch (InterruptedException e) {
                throw new RuntimeException("Thread was interupted waiting for log events coming into the bucket");
            }
        }
    }

    public int size() {
        return contents.size();
    }

    public List<T> getEvents() {
        return contents;
    }

    public T get(int index) {
        return contents.get(index);
    }

    public void dump() {
        List<T> events = contents;

        for (T t : events) {
            System.out.println(t);
        }
    }

    public Iterator<T> iterator() {
        return contents.iterator();
    }

    public T getFirst(BucketMatcher<T> matcher) {
        T first = null;
        synchronized (contents) {
            for (T t : contents) {
                if (matcher.matches(t)) {
                    first = t;
                    break;
                }
            }
        }
        return first;
    }

    @Override public String toString() {
        return contents.toString();
    }

    @SuppressWarnings("unchecked") public <E> List<E> extract(BucketMatcher<T> bucketMatcher) {
        List<E> extract = new ArrayList<E>();
        synchronized (contents) {
            for (T t : contents) {
                if (bucketMatcher.matches(t)) {
                    extract.add((E) t);
                }
            }
        }

        return extract;
    }

    public void onNewItem(T t) {
        add(t);
    }

    public void send(T t) {
        add(t);
    }

    public WorkerThread startSizeOutput() {
        WorkerThread thread = WorkerThread.executeOngoing("bucketSizeOutput", 1000, new Runnable() {
            public void run() {
                Out.out("Bucket '{}' size = {}", name, contents.size());
            }
        });
        return thread;
    }

    public boolean isEmpty() {
        synchronized (contents) {
            return contents.isEmpty();
        }
    }

    public T remove(int i) {
        synchronized (contents) {
            return contents.remove(i);
        }
    }

}
