package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.utils.LERP;

public class TestLERP {

    @Test public void testLerp() throws Exception {

        LERP lerp = new LERP(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        assertThat(lerp.lerp(0d), is(0d));
        assertThat(lerp.lerp(0.1d), is(1d));
        assertThat(lerp.lerp(0.2d), is(2d));
        assertThat(lerp.lerp(0.3d), is(3d));
        assertThat(lerp.lerp(0.4d), is(4d));
        assertThat(lerp.lerp(0.5d), is(5d));
        assertThat(lerp.lerp(0.6d), is(6d));
        assertThat(lerp.lerp(0.7d), is(7d));
        assertThat(lerp.lerp(0.8d), is(8d));
        assertThat(lerp.lerp(0.9d), is(9d));
        assertThat(lerp.lerp(1d), is(10d));
        assertThat(lerp.lerp(2d), is(10d));

//        for (int i = 0; i < 11; i++) {
//            System.out.println(lerp.gaussian(i, 10, 5, 2, 0));
//        }

        double[] values = new double[] { 0.4393693362340742,
                                        1.353352832366127,
                                        3.2465246735834974,
                                        6.065306597126334,
                                        8.824969025845954,
                                        10.0,
                                        8.824969025845954,
                                        6.065306597126334,
                                        3.2465246735834974,
                                        1.353352832366127,
                                        0.4393693362340742, };

        LERP tester = new LERP(values);
        double value = 0;
        for (int i = 0; i < 100; i++) {
            System.out.println(tester.lerp(value));
            value += 0.01;
        }
    }
}
