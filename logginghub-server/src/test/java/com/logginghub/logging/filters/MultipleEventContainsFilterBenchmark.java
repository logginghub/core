package com.logginghub.logging.filters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.filters.MultipleEventContainsFilter;
import com.logginghub.utils.MicroBenchmark;

@Ignore // Benchmarks
public class MultipleEventContainsFilterBenchmark
{
    @Test public void test()
    {
        MultipleEventContainsFilter filter = new MultipleEventContainsFilter("", false);
        filter.setEventContainsString("+one +two -three");

        DefaultLogEvent eventA = new DefaultLogEvent();
        eventA.setMessage("one two three");
        assertThat(filter.passes(eventA), is(false));

        eventA.setMessage("one two four");
        assertThat(filter.passes(eventA), is(true));

        eventA.setSourceApplication("one");
        eventA.setSourceClassName("two");
        eventA.setMessage("three");
        assertThat(filter.passes(eventA), is(false));

        eventA.setMessage("four");
        assertThat(filter.passes(eventA), is(true));
    }

    @Test public void testPerformanceContains()
    {
        final MultipleEventContainsFilter filter = new MultipleEventContainsFilter("", false);        
        filter.setEventContainsString("+one +two -three");
        
        final DefaultLogEvent eventA = new DefaultLogEvent();
        eventA.setMessage("one two four");
        
        MicroBenchmark.runAndDump(new Runnable()
        {
            public void run()
            {
                assertThat(filter.passes(eventA), is(true));       
            }
        });
    }
    
    @Test public void testPerformanceRegex()
    {
        final MultipleEventContainsFilter filter = new MultipleEventContainsFilter("", true);
        filter.setEventContainsString("+.*one.* +.*two.* -.*three.*");
        
        final DefaultLogEvent eventA = new DefaultLogEvent();
        eventA.setMessage("one two four");
        
        MicroBenchmark.runAndDump(new Runnable()
        {
            public void run()
            {
                assertThat(filter.passes(eventA), is(true));       
            }
        });
    }

}
