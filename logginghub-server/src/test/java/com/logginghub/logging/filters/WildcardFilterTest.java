package com.logginghub.logging.filters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.filters.WildcardFilter;
import com.logginghub.logging.filters.WildcardFilter.EventField;

public class WildcardFilterTest
{
    @Test public void test()
    {
        WildcardFilter filter = new WildcardFilter("", EventField.Source);

        // Start and end wildcards
        filter.setValue("*Test*");
        assertThat(filter.wildcardCheck("Test"), is(true));
        assertThat(filter.wildcardCheck("MooTest"), is(true));
        assertThat(filter.wildcardCheck("TestMoo"), is(true));
        assertThat(filter.wildcardCheck("MooTestMoo"), is(true));

        assertThat(filter.wildcardCheck("Tost"), is(false));
        assertThat(filter.wildcardCheck("MooTost"), is(false));
        assertThat(filter.wildcardCheck("TostMoo"), is(false));
        assertThat(filter.wildcardCheck("MooTostMoo"), is(false));

        // Exact string
        filter.setValue("Test");
        assertThat(filter.wildcardCheck("Test"), is(true));
        assertThat(filter.wildcardCheck("MooTest"), is(false));
        assertThat(filter.wildcardCheck("TestMoo"), is(false));
        assertThat(filter.wildcardCheck("MooTestMoo"), is(false));

        assertThat(filter.wildcardCheck("Tost"), is(false));
        assertThat(filter.wildcardCheck("MooTost"), is(false));
        assertThat(filter.wildcardCheck("TostMoo"), is(false));
        assertThat(filter.wildcardCheck("MooTostMoo"), is(false));

        // Pure wildcard
        filter.setValue("*");
        assertThat(filter.wildcardCheck("Test"), is(true));
        assertThat(filter.wildcardCheck("MooTest"), is(true));
        assertThat(filter.wildcardCheck("TestMoo"), is(true));
        assertThat(filter.wildcardCheck("MooTestMoo"), is(true));

        assertThat(filter.wildcardCheck("Tost"), is(true));
        assertThat(filter.wildcardCheck("MooTost"), is(true));
        assertThat(filter.wildcardCheck("TostMoo"), is(true));
        assertThat(filter.wildcardCheck("MooTostMoo"), is(true));
        assertThat(filter.wildcardCheck("*"), is(true));
        assertThat(filter.wildcardCheck(""), is(true));
        assertThat(filter.wildcardCheck("   "), is(true));

        // Start wildcards
        filter.setValue("*Test");
        assertThat(filter.wildcardCheck("Test"), is(true));
        assertThat(filter.wildcardCheck("MooTest"), is(true));
        assertThat(filter.wildcardCheck("TestMoo"), is(false));
        assertThat(filter.wildcardCheck("MooTestMoo"), is(false));

        assertThat(filter.wildcardCheck("Tost"), is(false));
        assertThat(filter.wildcardCheck("MooTost"), is(false));
        assertThat(filter.wildcardCheck("TostMoo"), is(false));
        assertThat(filter.wildcardCheck("MooTostMoo"), is(false));

        // End wildcard
        filter.setValue("Test*");
        assertThat(filter.wildcardCheck("Test"), is(true));
        assertThat(filter.wildcardCheck("MooTest"), is(false));
        assertThat(filter.wildcardCheck("TestMoo"), is(true));
        assertThat(filter.wildcardCheck("MooTestMoo"), is(false));

        assertThat(filter.wildcardCheck("Tost"), is(false));
        assertThat(filter.wildcardCheck("MooTost"), is(false));
        assertThat(filter.wildcardCheck("TostMoo"), is(false));
        assertThat(filter.wildcardCheck("MooTostMoo"), is(false));

        // Middle wildcard
        filter.setValue("T*st");
        assertThat(filter.wildcardCheck("Test"), is(true));
        assertThat(filter.wildcardCheck("MooTest"), is(false));
        assertThat(filter.wildcardCheck("TestMoo"), is(false));
        assertThat(filter.wildcardCheck("MooTestMoo"), is(false));

        assertThat(filter.wildcardCheck("Tost"), is(true));
        assertThat(filter.wildcardCheck("MooTost"), is(false));
        assertThat(filter.wildcardCheck("TostMoo"), is(false));
        assertThat(filter.wildcardCheck("MooTostMoo"), is(false));

        // Start and Middle wildcard
        filter.setValue("*T*st");
        assertThat(filter.wildcardCheck("Test"), is(true));
        assertThat(filter.wildcardCheck("MooTest"), is(true));
        assertThat(filter.wildcardCheck("TestMoo"), is(false));
        assertThat(filter.wildcardCheck("MooTestMoo"), is(false));

        assertThat(filter.wildcardCheck("Tost"), is(true));
        assertThat(filter.wildcardCheck("MooTost"), is(true));
        assertThat(filter.wildcardCheck("TostMoo"), is(false));
        assertThat(filter.wildcardCheck("MooTostMoo"), is(false));

        // End and Middle wildcard
        filter.setValue("T*st*");
        assertThat(filter.wildcardCheck("Test"), is(true));
        assertThat(filter.wildcardCheck("MooTest"), is(false));
        assertThat(filter.wildcardCheck("TestMoo"), is(true));
        assertThat(filter.wildcardCheck("MooTestMoo"), is(false));

        assertThat(filter.wildcardCheck("Tost"), is(true));
        assertThat(filter.wildcardCheck("MooTost"), is(false));
        assertThat(filter.wildcardCheck("TostMoo"), is(true));
        assertThat(filter.wildcardCheck("MooTostMoo"), is(false));
        
        // Start and End and Middle wildcard
        filter.setValue("*T*st*");
        assertThat(filter.wildcardCheck("Test"), is(true));
        assertThat(filter.wildcardCheck("MooTest"), is(true));
        assertThat(filter.wildcardCheck("TestMoo"), is(true));
        assertThat(filter.wildcardCheck("MooTestMoo"), is(true));

        assertThat(filter.wildcardCheck("Tost"), is(true));
        assertThat(filter.wildcardCheck("MooTost"), is(true));
        assertThat(filter.wildcardCheck("TostMoo"), is(true));
        assertThat(filter.wildcardCheck("MooTostMoo"), is(true));
    }

}
