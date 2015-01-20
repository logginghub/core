package com.logginghub.analytics;

import org.junit.Test;

import com.logginghub.analytics.model.AggregatedDataPoint;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class AggregatedDataPointTest {

    private static final double epsilon = 0.01;

    @Test public void testMerge() {
        AggregatedDataPoint point1 = new AggregatedDataPoint(2000, 3000, new SinglePassStatisticsDoublePrecision(2,5,6));
        AggregatedDataPoint point2 = new AggregatedDataPoint(1000, 4000, new SinglePassStatisticsDoublePrecision(1,2,3,10));
        
        point1.merge(point2);
        
        assertThat(point1.getClose(), is(Double.NaN));
        assertThat(point1.getCount(), is(7));
        assertThat(point1.getStartTime(), is(1000L));
        assertThat(point1.getEndTime(), is(4000L));
        assertThat(point1.getHigh(), is(closeTo(10, epsilon)));
        assertThat(point1.getLow(), is(closeTo(1, epsilon)));
        assertThat(point1.getMean(), is(closeTo(4.14, epsilon)));
        assertThat(point1.getOpen(), is(Double.NaN));
        assertThat(point1.getPercentiles()[50], is(Double.NaN));        
        assertThat(point1.getStddev(), is(Double.NaN));
        assertThat(point1.getTotal(), is(closeTo(29, epsilon)));
        
    }

}
