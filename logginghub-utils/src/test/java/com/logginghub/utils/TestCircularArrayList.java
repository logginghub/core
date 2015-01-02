package com.logginghub.utils;

import java.util.Iterator;

import com.logginghub.utils.CircularArrayList;

import junit.framework.TestCase;

public class TestCircularArrayList extends TestCase {
    private volatile int pushCount;
    private volatile int popCount;

    public void setUp() {
        pushCount = 0;
        popCount = 0;
    }

    public void testRotation() {
        CircularArrayList<Integer> q = new CircularArrayList<Integer>(); // DEFAULT_CAPACITY = 4
        testRotation0(q);
    }

    public void testExpandingRotation() {
        CircularArrayList<Integer> q = new CircularArrayList<Integer>(); // DEFAULT_CAPACITY = 4
        for (int i = 0; i < 10; i++) {
            testRotation0(q);

            // make expansion happen
            int oldCapacity = q.capacity();
            for (int j = q.capacity(); j >= 0; j--) {
                q.offer(new Integer(++pushCount));
            }

            assertTrue(q.capacity() > oldCapacity);
            testRotation0(q);
        }
    }

    private void testRotation0(CircularArrayList<Integer> q) {
        for (int i = 0; i < q.capacity() * 7 / 4; i++) {
            q.offer(new Integer(++pushCount));
            assertEquals(++popCount, q.poll().intValue());
        }
    }

    public void testRandomAddOnQueue() {
        CircularArrayList<Integer> q = new CircularArrayList<Integer>();
        // Create a queue with 5 elements and capacity 8;
        for (int i = 0; i < 5; i++) {
            q.offer(new Integer(i));
        }

        q.add(0, new Integer(100));
        q.add(3, new Integer(200));
        q.add(7, new Integer(300));

        Iterator<Integer> i = q.iterator();
        assertEquals(8, q.size());
        assertEquals(new Integer(100), i.next());
        assertEquals(new Integer(0), i.next());
        assertEquals(new Integer(1), i.next());
        assertEquals(new Integer(200), i.next());
        assertEquals(new Integer(2), i.next());
        assertEquals(new Integer(3), i.next());
        assertEquals(new Integer(4), i.next());
        assertEquals(new Integer(300), i.next());

        try {
            i.next();
            fail();
        } catch (Exception e) {
            // an exception signifies a successfull test case
            assertTrue(true);            
        }
    }

    public void testRandomAddOnRotatedQueue() {
        CircularArrayList<Integer> q = getRotatedQueue();

        q.add(0, new Integer(100)); // addFirst
        q.add(2, new Integer(200));
        q.add(4, new Integer(300));
        q.add(10, new Integer(400));
        q.add(12, new Integer(500)); // addLast

        Iterator<Integer> i = q.iterator();
        assertEquals(13, q.size());
        assertEquals(new Integer(100), i.next());
        assertEquals(new Integer(0), i.next());
        assertEquals(new Integer(200), i.next());
        assertEquals(new Integer(1), i.next());
        assertEquals(new Integer(300), i.next());
        assertEquals(new Integer(2), i.next());
        assertEquals(new Integer(3), i.next());
        assertEquals(new Integer(4), i.next());
        assertEquals(new Integer(5), i.next());
        assertEquals(new Integer(6), i.next());
        assertEquals(new Integer(400), i.next());
        assertEquals(new Integer(7), i.next());
        assertEquals(new Integer(500), i.next());

        try {
            i.next();
            fail();
        } catch (Exception e) {
            // an exception signifies a successfull test case
            assertTrue(true);
        }
    }

    public void testRandomRemoveOnQueue() {
        CircularArrayList<Integer> q = new CircularArrayList<Integer>();

        // Create a queue with 5 elements and capacity 8;
        for (int i = 0; i < 5; i++) {
            q.offer(new Integer(i));
        }

        q.remove(0);
        q.remove(2);
        q.remove(2);

        Iterator<Integer> i = q.iterator();
        assertEquals(2, q.size());
        assertEquals(new Integer(1), i.next());
        assertEquals(new Integer(2), i.next());

        try {
            i.next();
            fail();
        } catch (Exception e) {
            // an exception signifies a successfull test case
            assertTrue(true);
        }
    }

    public void testRandomRemoveOnRotatedQueue() {
        CircularArrayList<Integer> q = getRotatedQueue();

        q.remove(0); // removeFirst
        q.remove(2); // removeLast in the first half
        q.remove(2); // removeFirst in the first half
        q.remove(4); // removeLast

        Iterator<Integer> i = q.iterator();
        assertEquals(4, q.size());
        assertEquals(new Integer(1), i.next());
        assertEquals(new Integer(2), i.next());
        assertEquals(new Integer(5), i.next());
        assertEquals(new Integer(6), i.next());

        try {
            i.next();
            fail();
        } catch (Exception e) {
            // an exception signifies a successfull test case
            assertTrue(true);            
        }
    }
    
    public void testExpandAndShrink() throws Exception {
        CircularArrayList<Integer> q = new CircularArrayList<Integer>();
        for (int i = 0; i < 1024; i ++) {
            q.offer(i);
        }
        
        assertEquals(1024, q.capacity());
        
        for (int i = 0; i < 512; i ++) {
            q.offer(i);
            q.poll();
        }
        
        assertEquals(2048, q.capacity());
        
        for (int i = 0; i < 1024; i ++) { 
            q.poll();
        }
        
        assertEquals(4, q.capacity());
    }

    private CircularArrayList<Integer> getRotatedQueue() {
        CircularArrayList<Integer> q = new CircularArrayList<Integer>();

        // Ensure capacity: 16
        for (int i = 0; i < 16; i++) {
            q.offer(new Integer(-1));
        }
        q.clear();

        // Rotate it
        for (int i = 0; i < 12; i++) {
            q.offer(new Integer(-1));
            q.poll();
        }

        // Now push items
        for (int i = 0; i < 8; i++) {
            q.offer(new Integer(i));
        }

        return q;
    }
}

   