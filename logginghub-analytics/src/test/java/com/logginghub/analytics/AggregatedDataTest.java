package com.logginghub.analytics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.junit.Test;

import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.AggregatedDataPoint;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class AggregatedDataTest {

    private static final double epsilon = 0.00001;

    @Test public void testMerge() {

        AggregatedData seriesAPeriod1 = new AggregatedData("seriesA", "aLegend");
        AggregatedData seriesAPeriod2 = new AggregatedData("seriesA", "aLegend");

        seriesAPeriod1.add(new AggregatedDataPoint(1000, 2000, new SinglePassStatisticsDoublePrecision(1)));
        seriesAPeriod1.add(new AggregatedDataPoint(2000, 3000, new SinglePassStatisticsDoublePrecision(2)));
        seriesAPeriod1.add(new AggregatedDataPoint(3000, 4000, new SinglePassStatisticsDoublePrecision(3)));

        seriesAPeriod2.add(new AggregatedDataPoint(2000, 3000, new SinglePassStatisticsDoublePrecision(10)));
        seriesAPeriod2.add(new AggregatedDataPoint(4000, 5000, new SinglePassStatisticsDoublePrecision(20)));
        seriesAPeriod2.add(new AggregatedDataPoint(6000, 7000, new SinglePassStatisticsDoublePrecision(30)));

        seriesAPeriod1.merge(seriesAPeriod2);

        assertThat(seriesAPeriod1.getStartTime(), is(1000L));
        assertThat(seriesAPeriod1.getEndTime(), is(7000L));

        assertThat(seriesAPeriod1.getLegend(), is("aLegend"));
        assertThat(seriesAPeriod1.getSeriesName(), is("seriesA"));

        assertThat(seriesAPeriod1.size(), is(5));
        
        assertThat(seriesAPeriod1.get(0).getStartTime(), is(1000L));
        assertThat(seriesAPeriod1.get(1).getStartTime(), is(2000L));
        assertThat(seriesAPeriod1.get(2).getStartTime(), is(3000L));
        assertThat(seriesAPeriod1.get(3).getStartTime(), is(4000L));
        assertThat(seriesAPeriod1.get(4).getStartTime(), is(6000L));
        
        assertThat(seriesAPeriod1.get(0).getEndTime(), is(2000L));
        assertThat(seriesAPeriod1.get(1).getEndTime(), is(3000L));
        assertThat(seriesAPeriod1.get(2).getEndTime(), is(4000L));
        assertThat(seriesAPeriod1.get(3).getEndTime(), is(5000L));
        assertThat(seriesAPeriod1.get(4).getEndTime(), is(7000L));
        
        assertThat(seriesAPeriod1.get(0).getCount(), is(1));
        assertThat(seriesAPeriod1.get(1).getCount(), is(2));
        assertThat(seriesAPeriod1.get(2).getCount(), is(1));
        assertThat(seriesAPeriod1.get(3).getCount(), is(1));
        assertThat(seriesAPeriod1.get(4).getCount(), is(1));
        
        assertThat(seriesAPeriod1.get(0).getTotal(), is(closeTo(1, epsilon)));
        assertThat(seriesAPeriod1.get(1).getTotal(), is(closeTo(12, epsilon)));
        assertThat(seriesAPeriod1.get(2).getTotal(), is(closeTo(3, epsilon)));
        assertThat(seriesAPeriod1.get(3).getTotal(), is(closeTo(20, epsilon)));
        assertThat(seriesAPeriod1.get(4).getTotal(), is(closeTo(30, epsilon)));                     
    }

}
