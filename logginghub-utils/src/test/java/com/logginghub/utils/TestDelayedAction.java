package com.logginghub.utils;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.logginghub.utils.Bucket;
import com.logginghub.utils.DelayedAction;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class TestDelayedAction {

    @Test
    public void test_execute_once() {
    
        DelayedAction action = new DelayedAction(500, TimeUnit.MILLISECONDS);
        
        final Bucket<String> bucket = new Bucket<String>();
        Runnable runnable = new Runnable() {
            public void run() {
                bucket.add("item");
            }
        };
        
        action.execute(runnable);
        bucket.waitForMessages(1);
        
        assertThat(bucket.size(), is(1));
        assertThat(bucket.get(0), is("item"));
    }

    @Test
    public void test_execute_multiple() {
    
        DelayedAction action = new DelayedAction(500, TimeUnit.MILLISECONDS);
        
        final Bucket<String> bucket = new Bucket<String>();
        
        Runnable runnable1 = new Runnable() {
            public void run() {
                bucket.add("item-1");
            }
        };
        Runnable runnable2 = new Runnable() {
            public void run() {
                bucket.add("item-2");
            }
        };
        Runnable runnable3 = new Runnable() {
            public void run() {
                bucket.add("item-3");
            }
        };
        
        action.execute(runnable1);
        action.execute(runnable2);
        action.execute(runnable3);
        bucket.waitForMessages(1);
        
        assertThat(bucket.size(), is(1));
        assertThat(bucket.get(0), is("item-3"));
    }

    
}
