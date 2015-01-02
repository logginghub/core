package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.utils.SinglePassStatisticsLongPrecisionCircular;

public class TestSinglePassStatisticsLongPrecisionCircular {
    private static final double epsilon = 0.00000001;

    SinglePassStatisticsLongPrecisionCircular statistics = new SinglePassStatisticsLongPrecisionCircular();

    @Test public void testPercentiles() {

        statistics.addValues(15, 20, 35, 40, 50);
        statistics.doCalculations();

        assertThat(statistics.getPercentiles()[0], is(15d));
        assertThat(statistics.getPercentiles()[40], is(29D));
        assertThat(statistics.getPercentiles()[100], is(50d));

    }

    @Test public void testDontRecalculateIfNothingHasChanged() {

        assertThat(statistics.getCalculationCount(), is(0));
        
        // Empty stats shouldn't do anything
        statistics.doCalculations();        
        assertThat(statistics.getCalculationCount(), is(0));
        assertThat(statistics.getSum(), is(closeTo(0, epsilon)));
        
        // Add some values and check its gone up
        statistics.addValues(15, 20, 35, 40, 50);
        statistics.doCalculations();        
        assertThat(statistics.getCalculationCount(), is(1));
        assertThat(statistics.getSum(), is(closeTo(160, epsilon)));
        
        // Dont add anything, shouldn't go up
        statistics.doCalculations();        
        assertThat(statistics.getCalculationCount(), is(1));
        assertThat(statistics.getSum(), is(closeTo(160, epsilon)));
        
        // Chunk in another value and make sure things have updated
        statistics.addValue(10);        
        statistics.doCalculations();        
        assertThat(statistics.getCalculationCount(), is(2));
        assertThat(statistics.getSum(), is(closeTo(170, epsilon)));
    }

    @Test
    public void testEvictOldestItems_empty() {
        statistics.evictOldestItems(10);
        statistics.evictOldestItems(0);
        statistics.evictOldestItems(-10);
        
        
        statistics.addValue(1);
        statistics.addValue(2);
        statistics.addValue(3);
        statistics.addValue(4);
        statistics.addValue(5);
        statistics.addValue(6);
        statistics.doCalculations();
        
        assertThat(statistics.getCurrentCount(), is(6));
        statistics.evictOldestItems(3);
        assertThat(statistics.getCurrentCount(), is(3));
        statistics.doCalculations();
        assertThat(statistics.getSum(), is(15d));
    }

}
