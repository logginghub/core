package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.junit.Test;

import com.logginghub.utils.Pair;
import com.logginghub.utils.SinglePassStatisticsLongPrecision;
import com.logginghub.utils.TimeBasedLongStatistics;

public class TestTimeBasedLongStatistics {

    @Test public void test_chunk() throws Exception {

        TimeBasedLongStatistics stats = new TimeBasedLongStatistics(6);
        
        stats.add(0, 1);
        stats.add(999, 2);
        stats.add(1000, 3);
        stats.add(2000, 4);
        stats.add(2001, 5);
        
        assertThat(stats.getCount(), is(5));
        assertThat(stats.getDuration(), is(2001L));
        assertThat(stats.getEarliestTime(), is(0L));
        assertThat(stats.getLatestTime(), is(2001L));
        
        SinglePassStatisticsLongPrecision overallStatistics = stats.getOverallStatistics();
        assertThat(overallStatistics.getCount(), is(5));

        
        Pair<Long, SinglePassStatisticsLongPrecision>[] chunk = stats.chunk(1000);
        assertThat(chunk.length, is(3));
        
        assertThat(chunk[0].getA(), is(0L));
        assertThat(chunk[1].getA(), is(1000L));
        assertThat(chunk[2].getA(), is(2000L));
        
        assertThat(chunk[0].getB().getCount(), is(2));
        assertThat(chunk[1].getB().getCount(), is(1));
        assertThat(chunk[2].getB().getCount(), is(2));
        
        assertThat(chunk[0].getB().getMean(), is(1.5d));
        assertThat(chunk[1].getB().getMean(), is(3d));
        assertThat(chunk[2].getB().getMean(), is(4.5d));

        List<Pair<Long, Double>> meanSeries = TimeBasedLongStatistics.meanSeries(chunk);
        assertThat(meanSeries.size(), is(3));
        assertThat(meanSeries.get(0).getA(), is(0L));
        assertThat(meanSeries.get(1).getA(), is(1000L));
        assertThat(meanSeries.get(2).getA(), is(2000L));
        
        assertThat(meanSeries.get(0).getB(), is(1.5d));
        assertThat(meanSeries.get(1).getB(), is(3d));
        assertThat(meanSeries.get(2).getB(), is(4.5d));
        
        TimeBasedLongStatistics.scaleValueSeries(meanSeries, 10);
        TimeBasedLongStatistics.scaleTimeSeries(meanSeries, 1/1000d);
        
        assertThat(meanSeries.size(), is(3));
        assertThat(meanSeries.get(0).getA(), is(0L));
        assertThat(meanSeries.get(1).getA(), is(1L));
        assertThat(meanSeries.get(2).getA(), is(2L));
        
        assertThat(meanSeries.get(0).getB(), is(15d));
        assertThat(meanSeries.get(1).getB(), is(30d));
        assertThat(meanSeries.get(2).getB(), is(45d));
       
    }

}
