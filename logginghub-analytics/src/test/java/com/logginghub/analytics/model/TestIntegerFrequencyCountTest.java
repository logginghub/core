package com.logginghub.analytics.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.logginghub.utils.IntegerFrequencyCount;
import com.logginghub.utils.MutableIntegerValue;

public class TestIntegerFrequencyCountTest {

    @Test public void test(){
        
        IntegerFrequencyCount count = new IntegerFrequencyCount();
        
        count.getMetadata().put("key", 10);
        
        count.count("c", 7);
        count.count("a", 1);
        count.count("b", 5);
        count.count("a", 3);
        count.count("b", 10);
        
        assertThat(count.getTotal(), is(26));
        
        List<MutableIntegerValue> sortedValues = count.getSortedValues();
        
        assertThat(sortedValues.size(), is(3));
        assertThat(sortedValues.get(0).key, is("b"));
        assertThat(sortedValues.get(0).value, is(15));
        assertThat(sortedValues.get(1).key, is("c"));
        assertThat(sortedValues.get(1).value, is(7));
        assertThat(sortedValues.get(2).key, is("a"));
        assertThat(sortedValues.get(2).value, is(4));
        
        
        Map<String, MutableIntegerValue> data = count.getData();
        assertThat(data.size(), is(3));
        assertThat(data.get("a").key, is("a"));
        assertThat(data.get("a").value, is(4));
        assertThat(data.get("b").key, is("b"));
        assertThat(data.get("b").value, is(15));
        assertThat(data.get("c").key, is("c"));
        assertThat(data.get("c").value, is(7));
        
        assertThat(count.getMetadata().getInt("key", -1), is(10));
        
        List<MutableIntegerValue> top = count.top(2).getSortedValues();
        
        assertThat(top.size(), is(2));
        assertThat(top.get(0).key, is("b"));
        assertThat(top.get(0).value, is(15));
        assertThat(top.get(1).key, is("c"));
        assertThat(top.get(1).value, is(7));
        
    }
    
}
