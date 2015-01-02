package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.utils.WildcardMatcher;

public class TestWildcardMatcher {

    @Test public void test_matches() throws Exception {

        WildcardMatcher matcher = new WildcardMatcher();

        matcher.setValue("foo");

        assertThat(matcher.matches("foo"), is(true));
        assertThat(matcher.matches("a foo"), is(false));
        assertThat(matcher.matches("foo a"), is(false));
        assertThat(matcher.matches("a foo b"), is(false));

        matcher.setValue("foo*");

        assertThat(matcher.matches("foo"), is(true));
        assertThat(matcher.matches("a foo"), is(false));
        assertThat(matcher.matches("foo a"), is(true));
        assertThat(matcher.matches("a foo b"), is(false));

        matcher.setValue("*foo");

        assertThat(matcher.matches("foo"), is(true));
        assertThat(matcher.matches("a foo"), is(true));
        assertThat(matcher.matches("foo a"), is(false));
        assertThat(matcher.matches("a foo b"), is(false));

        matcher.setValue("*foo*");

        assertThat(matcher.matches("foo"), is(true));
        assertThat(matcher.matches("a foo"), is(true));
        assertThat(matcher.matches("foo a"), is(true));
        assertThat(matcher.matches("a foo b"), is(true));

        // TODO : this is broken!!
        // matcher.setValue("f*o");
        // assertThat(matcher.matches("foo"), is(true));
        // assertThat(matcher.matches("a foo"), is(false));
        // assertThat(matcher.matches("foo a"), is(false));
        // assertThat(matcher.matches("a foo b"), is(false));
        
    }

}
