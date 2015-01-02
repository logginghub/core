package com.logginghub.utils;

import org.hamcrest.number.IsCloseTo;
import org.junit.Test;

import com.logginghub.utils.SinglePassStatisticsDoublePrecision;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class TestSinglePassStatisticsDoublePrecision {
    private static final double epsilon = 0.00000001;

    @Test public void testPercentiles() {

        SinglePassStatisticsDoublePrecision statistics = new SinglePassStatisticsDoublePrecision();

        statistics.addValues(15, 20, 35, 40, 50);
        statistics.doCalculations();

        assertThat(statistics.getPercentiles()[0], is(15d));
        assertThat(statistics.getPercentiles()[40], is(29D));
        assertThat(statistics.getPercentiles()[100], is(50d));

    }

    @Test public void testDontRecalculateIfNothingHasChanged() {

        SinglePassStatisticsDoublePrecision statistics = new SinglePassStatisticsDoublePrecision();
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

}
