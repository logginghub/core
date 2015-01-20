package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.modules.Indexifier;
import com.logginghub.utils.Bucket;

public class TestIndexifier {

    // TODO : test other periods
    private Indexifier indexifier = new Indexifier(1000);

    @Test public void testAddEvent() throws Exception {
        
        Bucket<HistoricalIndexElement> bucket = new Bucket<HistoricalIndexElement>();
        indexifier.addFinishedIntervalDestination(bucket);
        
        assertThat(indexifier.getEarliestTime(), is(Long.MAX_VALUE));
        assertThat(indexifier.getMaximumCount().getInfoCount(), is(0));
        assertThat(indexifier.getMaximumCount().getOtherCount(), is(0));
        assertThat(indexifier.getMaximumCount().getSevereCount(), is(0));
        assertThat(indexifier.getMaximumCount().getWarningCount(), is(0));
        assertThat(indexifier.getCounts().size(), is(0));
        assertThat(bucket.size(), is(0));

        indexifier.addEvent(LogEventFixture1.event1);

        assertThat(indexifier.getEarliestTime(), is(0L));
        assertThat(indexifier.getMaximumCount().getInterval(), is(1000L));
        assertThat(indexifier.getMaximumCount().getTime(), is(0L));
        assertThat(indexifier.getMaximumCount().getInfoCount(), is(1));
        assertThat(indexifier.getMaximumCount().getOtherCount(), is(0));
        assertThat(indexifier.getMaximumCount().getSevereCount(), is(0));
        assertThat(indexifier.getMaximumCount().getWarningCount(), is(0));
        assertThat(indexifier.getCounts().size(), is(1));
        assertThat(indexifier.getCounts().get(0L).getTime(), is(0L));
        assertThat(indexifier.getCounts().get(0L).getInterval(), is(1000L));
        assertThat(indexifier.getCounts().get(0L).getInfoCount(), is(1));
        assertThat(indexifier.getCounts().get(0L).getOtherCount(), is(0));
        assertThat(indexifier.getCounts().get(0L).getSevereCount(), is(0));
        assertThat(indexifier.getCounts().get(0L).getWarningCount(), is(0));
        assertThat(bucket.size(), is(0));

        indexifier.addEvent(LogEventFixture1.event2);

        assertThat(indexifier.getEarliestTime(), is(0L));        
        assertThat(indexifier.getMaximumCount().getInfoCount(), is(1));
        assertThat(indexifier.getMaximumCount().getOtherCount(), is(0));
        assertThat(indexifier.getMaximumCount().getSevereCount(), is(0));
        assertThat(indexifier.getMaximumCount().getWarningCount(), is(1));
        assertThat(indexifier.getCounts().size(), is(1));
        assertThat(indexifier.getCounts().get(0L).getInfoCount(), is(1));
        assertThat(indexifier.getCounts().get(0L).getOtherCount(), is(0));
        assertThat(indexifier.getCounts().get(0L).getSevereCount(), is(0));
        assertThat(indexifier.getCounts().get(0L).getWarningCount(), is(1));
        assertThat(bucket.size(), is(0));
        
        indexifier.addEvent(LogEventFixture1.event3);

        assertThat(indexifier.getEarliestTime(), is(0L));
        assertThat(indexifier.getMaximumCount().getInfoCount(), is(1));
        assertThat(indexifier.getMaximumCount().getOtherCount(), is(1));
        assertThat(indexifier.getMaximumCount().getSevereCount(), is(0));
        assertThat(indexifier.getMaximumCount().getWarningCount(), is(1));
        assertThat(indexifier.getCounts().size(), is(1));
        assertThat(indexifier.getCounts().get(0L).getInfoCount(), is(1));
        assertThat(indexifier.getCounts().get(0L).getOtherCount(), is(1));
        assertThat(indexifier.getCounts().get(0L).getSevereCount(), is(0));
        assertThat(indexifier.getCounts().get(0L).getWarningCount(), is(1));
        assertThat(bucket.size(), is(0));
        
        indexifier.addEvent(LogEventFixture1.event4);

        assertThat(indexifier.getEarliestTime(), is(0L));
        assertThat(indexifier.getMaximumCount().getInfoCount(), is(1));
        assertThat(indexifier.getMaximumCount().getOtherCount(), is(1));
        assertThat(indexifier.getMaximumCount().getSevereCount(), is(1));
        assertThat(indexifier.getMaximumCount().getWarningCount(), is(1));
        assertThat(indexifier.getCounts().size(), is(1));
        assertThat(indexifier.getCounts().get(0L).getInfoCount(), is(1));
        assertThat(indexifier.getCounts().get(0L).getOtherCount(), is(1));
        assertThat(indexifier.getCounts().get(0L).getSevereCount(), is(1));
        assertThat(indexifier.getCounts().get(0L).getWarningCount(), is(1));
        assertThat(bucket.size(), is(0));

        indexifier.addEvent(LogEventFixture1.event5);

        assertThat(indexifier.getEarliestTime(), is(0L));
        assertThat(indexifier.getMaximumCount().getInfoCount(), is(1));
        assertThat(indexifier.getMaximumCount().getOtherCount(), is(1));
        assertThat(indexifier.getMaximumCount().getSevereCount(), is(1));
        assertThat(indexifier.getMaximumCount().getWarningCount(), is(1));
        
        assertThat(indexifier.getCounts().size(), is(2));
        assertThat(indexifier.getCounts().get(0L).getTime(), is(0L));
        assertThat(indexifier.getCounts().get(0L).getInterval(), is(1000L));
        assertThat(indexifier.getCounts().get(0L).getInfoCount(), is(1));
        assertThat(indexifier.getCounts().get(0L).getOtherCount(), is(1));
        assertThat(indexifier.getCounts().get(0L).getSevereCount(), is(1));
        assertThat(indexifier.getCounts().get(0L).getWarningCount(), is(1));
        
        assertThat(indexifier.getCounts().get(1000L).getTime(), is(1000L));
        assertThat(indexifier.getCounts().get(1000L).getInterval(), is(1000L));
        assertThat(indexifier.getCounts().get(1000L).getInfoCount(), is(1));
        assertThat(indexifier.getCounts().get(1000L).getOtherCount(), is(0));
        assertThat(indexifier.getCounts().get(1000L).getSevereCount(), is(0));
        assertThat(indexifier.getCounts().get(1000L).getWarningCount(), is(0));
        
        assertThat(bucket.size(), is(1));
        assertThat(bucket.get(0).getInfoCount(), is(1));
        assertThat(bucket.get(0).getOtherCount(), is(1));
        assertThat(bucket.get(0).getSevereCount(), is(1));
        assertThat(bucket.get(0).getWarningCount(), is(1));

        indexifier.addEvent(LogEventFixture1.event15);

        assertThat(bucket.size(), is(2));
        assertThat(bucket.get(0).getInfoCount(), is(1));
        assertThat(bucket.get(0).getOtherCount(), is(1));
        assertThat(bucket.get(0).getSevereCount(), is(1));
        assertThat(bucket.get(0).getWarningCount(), is(1));
        
        assertThat(bucket.get(1).getInfoCount(), is(1));
        assertThat(bucket.get(1).getOtherCount(), is(0));
        assertThat(bucket.get(1).getSevereCount(), is(0));
        assertThat(bucket.get(1).getWarningCount(), is(0));
        
        
    }

}
