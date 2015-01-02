package com.logginghub.utils;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.logginghub.utils.FixedTimeProvider;
import com.logginghub.utils.Throttler;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class TestThrottler {

    @Test public void testOkToFire() {
        Throttler throttler = new Throttler(10, TimeUnit.SECONDS);
        FixedTimeProvider timeProvider = new FixedTimeProvider(0);
        throttler.setTimeProvider(timeProvider);
        
        assertThat(throttler.isOkToFire(), is(true));
        assertThat(throttler.isOkToFire(), is(false));
        timeProvider.setTimeSeconds(5);
        assertThat(throttler.isOkToFire(), is(false));
        timeProvider.setTimeSeconds(9);
        assertThat(throttler.isOkToFire(), is(false));
        timeProvider.setTimeSeconds(10);
        assertThat(throttler.isOkToFire(), is(true));
        assertThat(throttler.isOkToFire(), is(false));
        
    }

}
