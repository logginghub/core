package com.logginghub.utils;

import org.junit.Test;

import com.logginghub.utils.FixedSizeCircularArrayList;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class TestFixedSizeCircularArrayList {

    private FixedSizeCircularArrayList<String> array = new FixedSizeCircularArrayList<String>(5);
    
    @Test public void test() {
        
        array.add("a");
        assertThat(array.size(), is(1));
        array.add("b");
        assertThat(array.size(), is(2));
        array.add("c");
        assertThat(array.size(), is(3));
        array.add("d");
        assertThat(array.size(), is(4));
        array.add("e");
        assertThat(array.size(), is(5));

        assertThat(array.remove(0), is("a"));
        assertThat(array.remove(0), is("b"));
        assertThat(array.remove(0), is("c"));
        
        array.add("f");
        assertThat(array.size(), is(3));
        
        assertThat(array.remove(0), is("d"));
        assertThat(array.remove(0), is("e"));
        assertThat(array.remove(0), is("f"));
        
        array.add("g");
        assertThat(array.size(), is(1));
        assertThat(array.remove(0), is("g"));
    }
    
}

