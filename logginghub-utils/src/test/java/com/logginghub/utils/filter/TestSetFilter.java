package com.logginghub.utils.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.utils.filter.SetFilter;

public class TestSetFilter {

    private SetFilter<String> filter = new SetFilter<String>();
    @Test public void testPasses() throws Exception {
        
        filter.add("a", "b", "c");        
        filter.setWhitelist(true);
        
        assertThat(filter.passes("a"), is(true));
        assertThat(filter.passes("b"), is(true));
        assertThat(filter.passes("c"), is(true));
        assertThat(filter.passes("d"), is(false));
        
        filter.setWhitelist(false);        
        assertThat(filter.passes("a"), is(false));
        assertThat(filter.passes("b"), is(false));
        assertThat(filter.passes("c"), is(false));
        assertThat(filter.passes("d"), is(true));
    }

}
