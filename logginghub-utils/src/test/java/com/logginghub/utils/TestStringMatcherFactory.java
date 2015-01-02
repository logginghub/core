package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.utils.StringMatcher;
import com.logginghub.utils.StringMatcherFactory;
import com.logginghub.utils.StringMatcherFactory.MatcherType;

public class TestStringMatcherFactory {

    @Test public void test_contains() throws Exception {
        StringMatcher matcher = StringMatcherFactory.createMatcher(MatcherType.Contains, "text");
        assertThat(matcher.matches("no match"), is(false));
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(""), is(false));

        assertThat(matcher.matches("text"), is(true));
        assertThat(matcher.matches("textstart"), is(true));
        assertThat(matcher.matches("endtext"), is(true));
        assertThat(matcher.matches("middletextmiddle"), is(true));
    }

    @Test public void test_starts_with() throws Exception {
        StringMatcher matcher = StringMatcherFactory.createMatcher(MatcherType.StartsWith, "text");
        assertThat(matcher.matches("no match"), is(false));
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(""), is(false));

        assertThat(matcher.matches("text"), is(true));
        assertThat(matcher.matches("textstart"), is(true));
        assertThat(matcher.matches("endtext"), is(false));
        assertThat(matcher.matches("middletextmiddle"), is(false));
    }

    @Test public void test_ends_with() throws Exception {
        StringMatcher matcher = StringMatcherFactory.createMatcher(MatcherType.EndsWith, "text");
        assertThat(matcher.matches("no match"), is(false));
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(""), is(false));

        assertThat(matcher.matches("text"), is(true));
        assertThat(matcher.matches("textstart"), is(false));
        assertThat(matcher.matches("endtext"), is(true));
        assertThat(matcher.matches("middletextmiddle"), is(false));
    }

    @Test public void test_wildcard_start() throws Exception {
        StringMatcher matcher = StringMatcherFactory.createMatcher(MatcherType.Wildcard, "text*");
        assertThat(matcher.matches("no match"), is(false));
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(""), is(false));

        assertThat(matcher.matches("text"), is(true));
        assertThat(matcher.matches("textstart"), is(true));
        assertThat(matcher.matches("endtext"), is(false));
        assertThat(matcher.matches("middletextmiddle"), is(false));
    }

    @Test public void test_wildcard_end() throws Exception {
        StringMatcher matcher = StringMatcherFactory.createMatcher(MatcherType.Wildcard, "*text");
        assertThat(matcher.matches("no match"), is(false));
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(""), is(false));

        assertThat(matcher.matches("text"), is(true));
        assertThat(matcher.matches("textstart"), is(false));
        assertThat(matcher.matches("endtext"), is(true));
        assertThat(matcher.matches("middletextmiddle"), is(false));
    }

    @Test public void test_wildcard_mid() throws Exception {
        StringMatcher matcher = StringMatcherFactory.createMatcher(MatcherType.Wildcard, "*text*");
        assertThat(matcher.matches("no match"), is(false));
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(""), is(false));

        assertThat(matcher.matches("text"), is(true));
        assertThat(matcher.matches("textstart"), is(true));
        assertThat(matcher.matches("endtext"), is(true));
        assertThat(matcher.matches("middletextmiddle"), is(true));
    }

    @Test public void test_wildcard_all() throws Exception {
        StringMatcher matcher = StringMatcherFactory.createMatcher(MatcherType.Wildcard, "*");
        assertThat(matcher.matches("no match"), is(true));
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(""), is(true));

        assertThat(matcher.matches("text"), is(true));
        assertThat(matcher.matches("textstart"), is(true));
        assertThat(matcher.matches("endtext"), is(true));
        assertThat(matcher.matches("middletextmiddle"), is(true));
    }

    @Test public void test_wildcard_empty() throws Exception {
        StringMatcher matcher = StringMatcherFactory.createMatcher(MatcherType.Wildcard, "");
        assertThat(matcher.matches("no match"), is(false));
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(""), is(true));

        assertThat(matcher.matches("text"), is(false));
        assertThat(matcher.matches("textstart"), is(false));
        assertThat(matcher.matches("endtext"), is(false));
        assertThat(matcher.matches("middletextmiddle"), is(false));
    }
    
    
    @Test public void test_regex_start() throws Exception {
        StringMatcher matcher = StringMatcherFactory.createMatcher(MatcherType.Regex, "text.*");
        assertThat(matcher.matches("no match"), is(false));
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(""), is(false));

        assertThat(matcher.matches("text"), is(true));
        assertThat(matcher.matches("textstart"), is(true));
        assertThat(matcher.matches("endtext"), is(false));
        assertThat(matcher.matches("middletextmiddle"), is(false));
    }

    @Test public void test_regex_end() throws Exception {
        StringMatcher matcher = StringMatcherFactory.createMatcher(MatcherType.Regex, ".*text");
        assertThat(matcher.matches("no match"), is(false));
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(""), is(false));

        assertThat(matcher.matches("text"), is(true));
        assertThat(matcher.matches("textstart"), is(false));
        assertThat(matcher.matches("endtext"), is(true));
        assertThat(matcher.matches("middletextmiddle"), is(false));
    }

    @Test public void test_regex_mid() throws Exception {
        StringMatcher matcher = StringMatcherFactory.createMatcher(MatcherType.Regex, ".*text.*");
        assertThat(matcher.matches("no match"), is(false));
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(""), is(false));

        assertThat(matcher.matches("text"), is(true));
        assertThat(matcher.matches("textstart"), is(true));
        assertThat(matcher.matches("endtext"), is(true));
        assertThat(matcher.matches("middletextmiddle"), is(true));
    }

    @Test public void test_regex_all() throws Exception {
        StringMatcher matcher = StringMatcherFactory.createMatcher(MatcherType.Regex, ".*");
        assertThat(matcher.matches("no match"), is(true));
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(""), is(true));

        assertThat(matcher.matches("text"), is(true));
        assertThat(matcher.matches("textstart"), is(true));
        assertThat(matcher.matches("endtext"), is(true));
        assertThat(matcher.matches("middletextmiddle"), is(true));
    }

    @Test public void test_regex_empty() throws Exception {
        StringMatcher matcher = StringMatcherFactory.createMatcher(MatcherType.Regex, "");
        assertThat(matcher.matches("no match"), is(false));
        assertThat(matcher.matches(null), is(false));
        assertThat(matcher.matches(""), is(true));

        assertThat(matcher.matches("text"), is(false));
        assertThat(matcher.matches("textstart"), is(false));
        assertThat(matcher.matches("endtext"), is(false));
        assertThat(matcher.matches("middletextmiddle"), is(false));
    }

}
