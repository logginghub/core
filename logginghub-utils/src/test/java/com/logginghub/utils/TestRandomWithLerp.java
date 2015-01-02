package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Stack;

import org.junit.Test;

import com.logginghub.utils.DoubleValueGenerator;
import com.logginghub.utils.Function;
import com.logginghub.utils.LERP;
import com.logginghub.utils.RandomWithLerp;

public class TestRandomWithLerp {

    @Test public void test_simple_linear() throws Exception {

//        Logger.setLevel(RandomWithLerp.class, Logger.fine);

        final Stack<Double> values = new Stack<Double>();
        values.push(0d);
        values.push(10d);
        values.push(0d);

        DoubleValueGenerator trend = new DoubleValueGenerator() {
            public double next() {
                return 10;
            }
        };

        DoubleValueGenerator value = new DoubleValueGenerator() {
            public double next() {
                return values.pop();
            }
        };

        RandomWithLerp randomWithMomentum = new RandomWithLerp(trend, value);

        assertThat(randomWithMomentum.next(), is(0d));
        assertThat(randomWithMomentum.next(), is(1d));
        assertThat(randomWithMomentum.next(), is(2d));
        assertThat(randomWithMomentum.next(), is(3d));
        assertThat(randomWithMomentum.next(), is(4d));
        assertThat(randomWithMomentum.next(), is(5d));
        assertThat(randomWithMomentum.next(), is(6d));
        assertThat(randomWithMomentum.next(), is(7d));
        assertThat(randomWithMomentum.next(), is(8d));
        assertThat(randomWithMomentum.next(), is(9d));

        assertThat(randomWithMomentum.next(), is(10d));
        assertThat(randomWithMomentum.next(), is(9d));
        assertThat(randomWithMomentum.next(), is(8d));
        assertThat(randomWithMomentum.next(), is(7d));
        assertThat(randomWithMomentum.next(), is(6d));
        assertThat(randomWithMomentum.next(), is(5d));
        assertThat(randomWithMomentum.next(), is(4d));
        assertThat(randomWithMomentum.next(), is(3d));
        assertThat(randomWithMomentum.next(), is(2d));
        assertThat(randomWithMomentum.next(), is(1d));

    }

    @Test public void test_function_for_result_manipulation() throws Exception {

//        Logger.setLevel(RandomWithLerp.class, Logger.fine);

        final Stack<Double> values = new Stack<Double>();
        values.push(0d);
        values.push(10d);
        values.push(0d);

        DoubleValueGenerator trend = new DoubleValueGenerator() {
            public double next() {
                return 10;
            }
        };

        DoubleValueGenerator value = new DoubleValueGenerator() {
            public double next() {
                return values.pop();
            }
        };

        RandomWithLerp randomWithMomentum = new RandomWithLerp(trend, value);
        randomWithMomentum.setFunction(new Function<Double, Double>() {
            public Double apply(Double a) {
                return a * 2;
            }
        });

        assertThat(randomWithMomentum.next(), is(2 * 0d));
        assertThat(randomWithMomentum.next(), is(2 * 1d));
        assertThat(randomWithMomentum.next(), is(2 * 2d));
        assertThat(randomWithMomentum.next(), is(2 * 3d));
        assertThat(randomWithMomentum.next(), is(2 * 4d));
        assertThat(randomWithMomentum.next(), is(2 * 5d));
        assertThat(randomWithMomentum.next(), is(2 * 6d));
        assertThat(randomWithMomentum.next(), is(2 * 7d));
        assertThat(randomWithMomentum.next(), is(2 * 8d));
        assertThat(randomWithMomentum.next(), is(2 * 9d));

        assertThat(randomWithMomentum.next(), is(2 * 10d));
        assertThat(randomWithMomentum.next(), is(2 * 9d));
        assertThat(randomWithMomentum.next(), is(2 * 8d));
        assertThat(randomWithMomentum.next(), is(2 * 7d));
        assertThat(randomWithMomentum.next(), is(2 * 6d));
        assertThat(randomWithMomentum.next(), is(2 * 5d));
        assertThat(randomWithMomentum.next(), is(2 * 4d));
        assertThat(randomWithMomentum.next(), is(2 * 3d));
        assertThat(randomWithMomentum.next(), is(2 * 2d));
        assertThat(randomWithMomentum.next(), is(2 * 1d));

    }

    @Test public void test_gaussian() throws Exception {

//        Logger.setLevel(RandomWithLerp.class, Logger.info);

        final Stack<Double> values = new Stack<Double>();
        values.push(0d);
        values.push(10d);
        values.push(0d);

        DoubleValueGenerator trend = new DoubleValueGenerator() {
            public double next() {
                return 10;
            }
        };

        DoubleValueGenerator value = new DoubleValueGenerator() {
            public double next() {
                return values.pop();
            }
        };

        final LERP gaussian = LERP.gausian();
        
        final RandomWithLerp randomWithMomentum = new RandomWithLerp(trend, value);
        randomWithMomentum.setFunction(new Function<Double, Double>() {
            public Double apply(Double a) {
                return a * gaussian.lerp(randomWithMomentum.getFactorThroughTrendSpace());
            }
        });
        
        // TODO : get this to apply to the values 0 and 10.  It should start at 0 and reach 10, but the values in between should be slower in the middle
        
        
        Thread.sleep(100);

        for (int i = 0; i < 20; i++) {
            System.out.println(randomWithMomentum.next());
        }

    }
}
