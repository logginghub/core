package com.logginghub.analytics;

import java.util.List;

import org.junit.Test;

import com.logginghub.analytics.TimeSeriesAggregator;
import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.TimeSeriesData;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;


public class TimeSeriesAggregatorTest {
    
    double epsilon = 0.001;
    
    @Test public void test() {

        TimeSeriesData data = new TimeSeriesData();
        data.setKeysLegend(new String[] {"keyA", "keyB"});
        data.setValuesLegend(new String[] {"valueA", "valueB"});
        
        data.add(1, new String[] { "a", "b"} , new double[] { 1, 10 } );
        data.add(2, new String[] { "a", "b"} , new double[] { 2, 20 } );
        data.add(3, new String[] { "a", "b"} , new double[] { 3, 30 } );
        data.add(4, new String[] { "a", "b"} , new double[] { 4, 40 } );
        data.add(5, new String[] { "a", "b"} , new double[] { 5, 50 } );
        data.add(10, new String[] { "a", "b"} , new double[] { 10, 110 } );
        data.add(11, new String[] { "a", "b"} , new double[] { 11, 100 } );
        data.add(12, new String[] { "a", "b"} , new double[] { 12, 150 } );
        data.add(15, new String[] { "a", "b"} , new double[] { 15, 120 } );
        data.add(30, new String[] { "a", "b"} , new double[] { 30, 300 } );
        data.add(31, new String[] { "a", "b"} , new double[] { 31, 310 } );
        data.add(39, new String[] { "a", "b"} , new double[] { 39, 390 } );
        data.add(50, new String[] { "a", "b"} , new double[] { 50, 500 } );
        
        
        TimeSeriesAggregator aggregator = new TimeSeriesAggregator();
        AggregatedData aggregated = aggregator.aggregate("Data", data, 10, 1);
        
        assertThat(aggregated.size(), is(6));

        assertThat(aggregated.get(0).getPercentile(0), is(10d));
        assertThat(aggregated.get(1).getPercentile(0), is(100d));
        assertThat(aggregated.get(2).getPercentile(0), is(Double.NaN));
        assertThat(aggregated.get(3).getPercentile(0), is(300d));
        assertThat(aggregated.get(4).getPercentile(0), is(Double.NaN));
        assertThat(aggregated.get(5).getPercentile(0), is(500d));
        
        assertThat(aggregated.get(0).getStddev(), is(closeTo(14.142, epsilon)));
        assertThat(aggregated.get(1).getStddev(), is(closeTo(18.708, epsilon)));
        assertThat(aggregated.get(2).getStddev(), is(Double.NaN));
        assertThat(aggregated.get(3).getStddev(), is(closeTo(40.277, epsilon)));
        assertThat(aggregated.get(4).getStddev(), is(Double.NaN));
        assertThat(aggregated.get(5).getStddev(), is(closeTo(0, epsilon)));
        
        assertThat(aggregated.get(0).getStartTime(), is(0L));
        assertThat(aggregated.get(1).getStartTime(), is(10L));
        assertThat(aggregated.get(2).getStartTime(), is(20L));
        assertThat(aggregated.get(3).getStartTime(), is(30L));
        assertThat(aggregated.get(4).getStartTime(), is(40L));
        assertThat(aggregated.get(5).getStartTime(), is(50L));
        
        assertThat(aggregated.get(0).getOpen(), is(10d));
        assertThat(aggregated.get(1).getOpen(), is(110d));
        assertThat(aggregated.get(2).getOpen(), is(Double.NaN));
        assertThat(aggregated.get(3).getOpen(), is(300d));
        assertThat(aggregated.get(4).getOpen(), is(Double.NaN));
        assertThat(aggregated.get(5).getOpen(), is(500d));
        
        assertThat(aggregated.get(0).getLow(), is(10d));
        assertThat(aggregated.get(1).getLow(), is(100d));
        assertThat(aggregated.get(2).getLow(), is(Double.NaN));
        assertThat(aggregated.get(3).getLow(), is(300d));
        assertThat(aggregated.get(4).getLow(), is(Double.NaN));
        assertThat(aggregated.get(5).getLow(), is(500d));
        
        assertThat(aggregated.get(0).getMean(), is(30d));
        assertThat(aggregated.get(1).getMean(), is(120d));
        assertThat(aggregated.get(2).getMean(), is(Double.NaN));
        assertThat(aggregated.get(3).getMean(), is(333.3333333333333d));
        assertThat(aggregated.get(4).getMean(), is(Double.NaN));
        assertThat(aggregated.get(5).getMean(), is(500d));
        
        assertThat(aggregated.get(0).getMedian(), is(30d));
        assertThat(aggregated.get(1).getMedian(), is(115d));
        assertThat(aggregated.get(2).getMedian(), is(Double.NaN));
        assertThat(aggregated.get(3).getMedian(), is(310d));
        assertThat(aggregated.get(4).getMedian(), is(Double.NaN));
        assertThat(aggregated.get(5).getMedian(), is(500d));
        
        assertThat(aggregated.get(0).getHigh(), is(50d));
        assertThat(aggregated.get(1).getHigh(), is(150d));
        assertThat(aggregated.get(2).getHigh(), is(Double.NaN));
        assertThat(aggregated.get(3).getHigh(), is(390d));
        assertThat(aggregated.get(4).getHigh(), is(Double.NaN));
        assertThat(aggregated.get(5).getHigh(), is(500d));
        
        assertThat(aggregated.get(0).getCount(), is(5));
        assertThat(aggregated.get(1).getCount(), is(4));
        assertThat(aggregated.get(2).getCount(), is(0));
        assertThat(aggregated.get(3).getCount(), is(3));
        assertThat(aggregated.get(4).getCount(), is(0));
        assertThat(aggregated.get(5).getCount(), is(1));
        
        assertThat(aggregated.get(0).getTotal(), is(150d));
        assertThat(aggregated.get(1).getTotal(), is(480d));
        assertThat(aggregated.get(2).getTotal(), is(0d));
        assertThat(aggregated.get(3).getTotal(), is(1000d));
        assertThat(aggregated.get(4).getTotal(), is(0d));
        assertThat(aggregated.get(5).getTotal(), is(500d));
        
        assertThat(aggregated.get(0).getClose(), is(50d));
        assertThat(aggregated.get(1).getClose(), is(120d));
        assertThat(aggregated.get(2).getClose(), is(Double.NaN));
        assertThat(aggregated.get(3).getClose(), is(390d));
        assertThat(aggregated.get(4).getClose(), is(Double.NaN));
        assertThat(aggregated.get(5).getClose(), is(500d));
        
        
        
    }
}
