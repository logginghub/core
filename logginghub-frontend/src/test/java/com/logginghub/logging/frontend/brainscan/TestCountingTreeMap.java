package com.logginghub.logging.frontend.brainscan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.frontend.brainscan.CountingTreeMap;

public class TestCountingTreeMap {

//    @Test public void test_counters() {
//
//        CountingTreeMap root = new CountingTreeMap();
//
//        root.count("a");
//        assertThat(root.getCount(), is(1));
//        assertThat(root.getCount("a"), is(1));
//        root.clear();
//
//        root.count("a").count("b").count("c");
//        root.count("a").count("b").count("d");
//        root.count("a").count("e").count("c");
//        root.count("b").count("e").count("c");
//
//        assertThat(root.getCount(), is(4));
//        assertThat(root.getCount("a"), is(3));
//        assertThat(root.get("a").getCount("b"), is(2));
//        assertThat(root.get("a").get("b").getCount("c"), is(1));
//        assertThat(root.get("a").get("b").get("c").getCount(), is(1));
//        assertThat(root.getCount("b"), is(1));
//
//    }

    @Test public void test_values() {

        CountingTreeMap root = new CountingTreeMap();
        
        root.count(1);
        assertThat(root.getCount(), is(1L));
        
        root.count("a", 10);
        assertThat(root.getCount(), is(11L));
        assertThat(root.get("a").getCount(), is(10L));
        
        root.get("a").count("b", 5);
        assertThat(root.getCount(), is(16L));
        assertThat(root.get("a").getCount(), is(15L));
        assertThat(root.get("a").get("b").getCount(), is(5L));
        
//        root.count("b", 20);
//
//        root.count("a").count("b", 3).count("d", 5);
//        
//        assertThat(root.getCount("a"), is(9));
//        assertThat(root.get("a").getCount("b"), is(8));
//        assertThat(root.get("a").get("b").getCount("c"), is(5));

    }
}
