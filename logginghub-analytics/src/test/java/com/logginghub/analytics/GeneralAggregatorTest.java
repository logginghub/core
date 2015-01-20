package com.logginghub.analytics;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.logginghub.analytics.GeneralAggregator;
import com.logginghub.analytics.model.GeneralAggregatedData;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class GeneralAggregatorTest {

    @Test public void test() {
        GeneralAggregator aggregator = new GeneralAggregator();
        List<Double> data = new ArrayList<Double>();
        
        data.add(21d);
        data.add(24d);
        data.add(28d);
        data.add(33d);
        data.add(35d);
        data.add(33d);
        data.add(30d);
        data.add(50d);
        data.add(60d);
        data.add(65d);
        
        GeneralAggregatedData aggregatedData = aggregator.aggregate("Series", data, 10, true);
        
        assertThat(aggregatedData.size(), is(7));
        
        assertThat(aggregatedData.get(0).getCount(), is(0));
        assertThat(aggregatedData.get(1).getCount(), is(0));
        assertThat(aggregatedData.get(2).getCount(), is(3));
        assertThat(aggregatedData.get(3).getCount(), is(4));
        assertThat(aggregatedData.get(4).getCount(), is(0));
        assertThat(aggregatedData.get(5).getCount(), is(1));
        assertThat(aggregatedData.get(6).getCount(), is(2));
        
        
    }

}
