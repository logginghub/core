package com.logginghub.analytics.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import java.util.Set;

import org.junit.Test;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.AggregatedDataPoint;
import com.logginghub.analytics.model.MultiSeriesAggreatedData;
import com.logginghub.utils.CollectionUtils;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;


public class TestMultiSeriesAggreatedData {
    @Test public void test() {

        AggregatedData seriesAPeriod1 = new AggregatedData("seriesA", "aLegend");
        AggregatedData seriesAPeriod2 = new AggregatedData("seriesA", "aLegend");
        AggregatedData seriesB = new AggregatedData("seriesB", "bLegend");
        AggregatedData seriesC = new AggregatedData("seriesC", "cLegend");

        seriesAPeriod1.add(new AggregatedDataPoint(1000, 2000, new SinglePassStatisticsDoublePrecision(1)));
        seriesAPeriod1.add(new AggregatedDataPoint(2000, 3000, new SinglePassStatisticsDoublePrecision(2)));
        seriesAPeriod1.add(new AggregatedDataPoint(3000, 4000, new SinglePassStatisticsDoublePrecision(3)));

        seriesAPeriod2.add(new AggregatedDataPoint(2000, 3000, new SinglePassStatisticsDoublePrecision(10)));
        seriesAPeriod2.add(new AggregatedDataPoint(4000, 5000, new SinglePassStatisticsDoublePrecision(20)));
        seriesAPeriod2.add(new AggregatedDataPoint(6000, 7000, new SinglePassStatisticsDoublePrecision(30)));

        seriesB.add(new AggregatedDataPoint(1000, 2000, new SinglePassStatisticsDoublePrecision(1)));
        seriesB.add(new AggregatedDataPoint(2000, 3000, new SinglePassStatisticsDoublePrecision(2)));
        seriesB.add(new AggregatedDataPoint(3000, 4000, new SinglePassStatisticsDoublePrecision(3)));

        seriesC.add(new AggregatedDataPoint(1000, 2000, new SinglePassStatisticsDoublePrecision(1)));
        seriesC.add(new AggregatedDataPoint(2000, 3000, new SinglePassStatisticsDoublePrecision(2)));
        seriesC.add(new AggregatedDataPoint(3000, 4000, new SinglePassStatisticsDoublePrecision(3)));

        MultiSeriesAggreatedData seriesDataA = new MultiSeriesAggreatedData("valueLegend", "keysLegend", seriesAPeriod1, seriesB);
        MultiSeriesAggreatedData seriesDataB = new MultiSeriesAggreatedData("valueLegend", "keysLegend", seriesAPeriod2, seriesC);

        seriesDataA.append(seriesDataB);
        
        assertThat(seriesDataA.getStartTime(), is(1000L));
        assertThat(seriesDataA.getEndTime(), is(7000L));
        assertThat(seriesDataA.getMappedData().size(), is(3));
        assertThat(seriesDataA.getOrderedData().size(), is(3));
        assertThat(seriesDataA.getOverallValue(AggregatedDataKey.Sum), is(closeTo(78, 0.01)));
        assertThat(seriesDataA.getSeriesNames(), is(((Set<String>)CollectionUtils.newHashSet("seriesA", "seriesB", "seriesC"))));
        assertThat(seriesDataA.getValueLegend(), is("valueLegend"));
        
//        seriesDataA.sortAscending(AggregatedDataKey.Count);
        
//        seriesDataB.sortDescending(AggregatedDataKey.Mean, AggregatedDataKey.Sum);
    }
}
