package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import com.logginghub.utils.CircularArray;

public class TestCircularArray {

    @Test public void test() {

        CircularArray<Integer> array = new CircularArray<Integer>(Integer.class, 5);

        array.append(5);

        assertThat(array.getCurrentPointer(), is(1));
        assertThat(array.getStartPointer(), is(0));
        assertThat(array.getData(), is(new Integer[] { 5, null, null, null, null }));
        assertThat(array.size(), is(1));
        assertThat(array.getCapacity(), is(5));
        assertThat(array.get(0), is(5));
        assertThat(array.get(1), is(nullValue()));
        assertThat(array.get(2), is(nullValue()));
        assertThat(array.get(3), is(nullValue()));
        assertThat(array.get(4), is(nullValue()));

        array.append(3);

        assertThat(array.getCurrentPointer(), is(2));
        assertThat(array.getStartPointer(), is(0));
        assertThat(array.getData(), is(new Integer[] { 5, 3, null, null, null }));
        assertThat(array.size(), is(2));
        assertThat(array.getCapacity(), is(5));
        assertThat(array.get(0), is(5));
        assertThat(array.get(1), is(3));
        assertThat(array.get(2), is(nullValue()));
        assertThat(array.get(3), is(nullValue()));
        assertThat(array.get(4), is(nullValue()));

        array.append(1);

        assertThat(array.getCurrentPointer(), is(3));
        assertThat(array.getStartPointer(), is(0));
        assertThat(array.getData(), is(new Integer[] { 5, 3, 1, null, null }));
        assertThat(array.size(), is(3));
        assertThat(array.getCapacity(), is(5));
        assertThat(array.get(0), is(5));
        assertThat(array.get(1), is(3));
        assertThat(array.get(2), is(1));
        assertThat(array.get(3), is(nullValue()));
        assertThat(array.get(4), is(nullValue()));


        array.append(10);

        assertThat(array.getCurrentPointer(), is(4));
        assertThat(array.getStartPointer(), is(0));
        assertThat(array.getData(), is(new Integer[] { 5, 3, 1, 10, null }));
        assertThat(array.size(), is(4));
        assertThat(array.getCapacity(), is(5));
        assertThat(array.get(0), is(5));
        assertThat(array.get(1), is(3));
        assertThat(array.get(2), is(1));
        assertThat(array.get(3), is(10));
        assertThat(array.get(4), is(nullValue()));

        array.append(2);

        assertThat(array.getCurrentPointer(), is(0));
        assertThat(array.getStartPointer(), is(0));
        assertThat(array.getData(), is(new Integer[] { 5, 3, 1, 10, 2 }));
        assertThat(array.size(), is(5));
        assertThat(array.getCapacity(), is(5));
        assertThat(array.get(0), is(5));
        assertThat(array.get(1), is(3));
        assertThat(array.get(2), is(1));
        assertThat(array.get(3), is(10));
        assertThat(array.get(4), is(2));

        array.append(7);

        assertThat(array.getCurrentPointer(), is(1));
        assertThat(array.getStartPointer(), is(1));
        assertThat(array.getData(), is(new Integer[] { 7, 3, 1, 10, 2 }));
        assertThat(array.size(), is(5));
        assertThat(array.getCapacity(), is(5));
        assertThat(array.get(0), is(3));
        assertThat(array.get(1), is(1));
        assertThat(array.get(2), is(10));
        assertThat(array.get(3), is(2));
        assertThat(array.get(4), is(7));
        
        array.append(8);

        assertThat(array.getCurrentPointer(), is(2));
        assertThat(array.getStartPointer(), is(2));
        assertThat(array.getData(), is(new Integer[] { 7, 8, 1, 10, 2 }));
        assertThat(array.size(), is(5));
        assertThat(array.getCapacity(), is(5));
        assertThat(array.get(0), is(1));
        assertThat(array.get(1), is(10));
        assertThat(array.get(2), is(2));
        assertThat(array.get(3), is(7));
        assertThat(array.get(4), is(8));
        
        array.append(9);

        assertThat(array.getCurrentPointer(), is(3));
        assertThat(array.getStartPointer(), is(3));
        assertThat(array.getData(), is(new Integer[] { 7, 8, 9, 10, 2 }));
        assertThat(array.size(), is(5));
        assertThat(array.getCapacity(), is(5));
        assertThat(array.get(0), is(10));
        assertThat(array.get(1), is(2));
        assertThat(array.get(2), is(7));
        assertThat(array.get(3), is(8));
        assertThat(array.get(4), is(9));

        array.append(1);

        assertThat(array.getCurrentPointer(), is(4));
        assertThat(array.getStartPointer(), is(4));
        assertThat(array.getData(), is(new Integer[] { 7, 8, 9, 1, 2 }));
        assertThat(array.size(), is(5));
        assertThat(array.getCapacity(), is(5));
        assertThat(array.get(0), is(2));
        assertThat(array.get(1), is(7));
        assertThat(array.get(2), is(8));
        assertThat(array.get(3), is(9));
        assertThat(array.get(4), is(1));
        
        array.append(3);

        assertThat(array.getCurrentPointer(), is(0));
        assertThat(array.getStartPointer(), is(0));
        assertThat(array.getData(), is(new Integer[] { 7, 8, 9, 1, 3 }));
        assertThat(array.size(), is(5));
        assertThat(array.getCapacity(), is(5));
        assertThat(array.get(0), is(7));
        assertThat(array.get(1), is(8));
        assertThat(array.get(2), is(9));
        assertThat(array.get(3), is(1));
        assertThat(array.get(4), is(3));

        array.append(4);

        assertThat(array.getCurrentPointer(), is(1));
        assertThat(array.getStartPointer(), is(1));
        assertThat(array.getData(), is(new Integer[] { 4, 8, 9, 1, 3 }));
        assertThat(array.size(), is(5));
        assertThat(array.getCapacity(), is(5));
        assertThat(array.get(0), is(8));
        assertThat(array.get(1), is(9));
        assertThat(array.get(2), is(1));
        assertThat(array.get(3), is(3));
        assertThat(array.get(4), is(4));

        
        


        
    }
}
