package com.logginghub.analytics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.analytics.model.TimeSeriesData;

public class TestTimeSeriesData {

    TimeSeriesData data = new TimeSeriesData();

    @Test public void testAddAndSort() {

        data.add(10, new String[] { "a", "b" }, new double[] {10.0});
        
        assertThat(data.size(), is(1));
        assertThat(data.get(0).getKeys(), is(new String[] { "a", "b" }));
        assertThat(data.get(0).getTime(), is(10L));
        assertThat(data.get(0).getValues()[0], is(closeTo(10,0.00001)));
     
        data.add(5, new String[] { "a" },new double[] { 5.0});
        
        assertThat(data.size(), is(2));
        assertThat(data.get(0).getKeys(), is(new String[] { "a", "b" }));
        assertThat(data.get(0).getTime(), is(10L));
        assertThat(data.get(0).getValues()[0], is(closeTo(10,0.00001)));

        assertThat(data.get(1).getKeys(), is(new String[] { "a" }));
        assertThat(data.get(1).getTime(), is(5L));
        assertThat(data.get(1).getValues()[0], is(closeTo(5,0.00001)));
        
        data.sort();
        
        assertThat(data.size(), is(2));
        assertThat(data.get(1).getKeys(), is(new String[] { "a", "b" }));
        assertThat(data.get(1).getTime(), is(10L));
        assertThat(data.get(1).getValues()[0], is(closeTo(10,0.00001)));

        assertThat(data.get(0).getKeys(), is(new String[] { "a" }));
        assertThat(data.get(0).getTime(), is(5L));
        assertThat(data.get(0).getValues()[0], is(closeTo(5,0.00001)));
    }
    
   
}
