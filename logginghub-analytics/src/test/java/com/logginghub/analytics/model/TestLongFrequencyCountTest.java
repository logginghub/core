package com.logginghub.analytics.model;

import com.logginghub.utils.MutableLongValue;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestLongFrequencyCountTest {

    @Test public void test(){
        
        LongFrequencyCount count = new LongFrequencyCount();
        
        count.getMetadata().put("key", 10);
        
        count.count("c", 7);
        count.count("a", 1);
        count.count("b", 5);
        count.count("a", 3);
        count.count("b", 10);
        
        assertThat(count.getTotal(), is(26L));
        
        List<MutableLongValue> sortedValues = count.getSortedValues();
        
        assertThat(sortedValues.size(), is(3));
        assertThat(sortedValues.get(0).key.toString(), is("b"));
        assertThat(sortedValues.get(0).value, is(15L));
        assertThat(sortedValues.get(1).key.toString(), is("c"));
        assertThat(sortedValues.get(1).value, is(7L));
        assertThat(sortedValues.get(2).key.toString(), is("a"));
        assertThat(sortedValues.get(2).value, is(4L));
        
        
        Map<String, MutableLongValue> data = count.getData();
        assertThat(data.size(), is(3));
        assertThat(data.get("a").key.toString(), is("a"));
        assertThat(data.get("a").value, is(4L));
        assertThat(data.get("b").key.toString(), is("b"));
        assertThat(data.get("b").value, is(15L));
        assertThat(data.get("c").key.toString(), is("c"));
        assertThat(data.get("c").value, is(7L));
        
        assertThat(count.getMetadata().getInt("key", -1), is(10));
        
        List<MutableLongValue> top = count.top(2).getSortedValues();
        
        assertThat(top.size(), is(2));
        assertThat(top.get(0).key.toString(), is("b"));
        assertThat(top.get(0).value, is(15L));
        assertThat(top.get(1).key.toString(), is("c"));
        assertThat(top.get(1).value, is(7L));
        
    }
    
}
