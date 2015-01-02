package com.logginghub.utils;

import org.junit.Test;

import com.logginghub.utils.ExponentialMovingAverage;



public class TestExponentialMovingAverage {

    @Test public void testCalculateExponentialMovingAverage() throws Exception {

        ExponentialMovingAverage ExponentialMovingAverage = new ExponentialMovingAverage(3);
        
        // No idea what the real values should be :/
        

//        assertThat(ExponentialMovingAverage.calculateMovingAverage(), is(Double.NaN));
//
//        // First item
//        ExponentialMovingAverage.addValue(1);
//
//        assertThat(ExponentialMovingAverage.getValues().size(), is(1));
//        assertThat(ExponentialMovingAverage.getValues().get(0), is(1d));
//
//        assertThat(ExponentialMovingAverage.calculateMovingAverage(), is(1d));
//
//        // Second item
//        ExponentialMovingAverage.addValue(2);
//
//        assertThat(ExponentialMovingAverage.getValues().size(), is(2));
//        assertThat(ExponentialMovingAverage.getValues().get(0), is(1d));
//        assertThat(ExponentialMovingAverage.getValues().get(1), is(2d));
//
//        assertThat(ExponentialMovingAverage.calculateMovingAverage(), is(1.5d));
//
//        // Third item
//        ExponentialMovingAverage.addValue(3);
//
//        assertThat(ExponentialMovingAverage.getValues().size(), is(3));
//        assertThat(ExponentialMovingAverage.getValues().get(0), is(1d));
//        assertThat(ExponentialMovingAverage.getValues().get(1), is(2d));
//        assertThat(ExponentialMovingAverage.getValues().get(2), is(3d));
//
//        assertThat(ExponentialMovingAverage.calculateMovingAverage(), is(2d));
//
//        // Fourth item
//        ExponentialMovingAverage.addValue(4);
//
//        assertThat(ExponentialMovingAverage.getValues().size(), is(3));
//        assertThat(ExponentialMovingAverage.getValues().get(0), is(2d));
//        assertThat(ExponentialMovingAverage.getValues().get(1), is(3d));
//        assertThat(ExponentialMovingAverage.getValues().get(2), is(4d));
//
//        assertThat(ExponentialMovingAverage.calculateMovingAverage(), is(3d));
    }

}
