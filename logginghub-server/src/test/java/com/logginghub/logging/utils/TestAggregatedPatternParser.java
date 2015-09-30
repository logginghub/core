package com.logginghub.logging.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.utils.AggregatedPatternParser.Node;

public class TestAggregatedPatternParser {

    private AggregatedPatternParser parser = new AggregatedPatternParser();

    @Rule public ExpectedException exception = ExpectedException.none();

    private Pattern pattern = new Pattern();
    
    @Before public void setup() {
        pattern.setCleanup(false);
        pattern.setDebug(false);
        pattern.setName("pattern1");
        pattern.setPattern("This {is} a {pattern} with {some} labels");
        pattern.setPatternId(1);
    }
    
    @Test public void test_parse_success_1() throws Exception {

        parser.parse("This is a pattern {0} with some {event.loggerName} and some {1} bits {event.time}", pattern.getPattern());

        List<Node> nodes = parser.getNodes();
        assertThat(nodes.size(), is(8));
        assertThat(nodes.get(0).toString(), is("This is a pattern "));
        assertThat(nodes.get(1).toString(), is("{0}"));
        assertThat(nodes.get(2).toString(), is(" with some "));
        assertThat(nodes.get(3).toString(), is("{loggerName}"));
        assertThat(nodes.get(4).toString(), is(" and some "));
        assertThat(nodes.get(5).toString(), is("{1}"));
        assertThat(nodes.get(6).toString(), is(" bits "));
        assertThat(nodes.get(7).toString(), is("{time}"));

    }

    @Test public void test_parse_success_2() throws Exception {

        parser.parse("This is a pattern {0} with some {event.loggerName} and some {1} bits {event.time} and end bit", pattern.getPattern());

        List<Node> nodes = parser.getNodes();
        assertThat(nodes.size(), is(9));
        assertThat(nodes.get(0).toString(), is("This is a pattern "));
        assertThat(nodes.get(1).toString(), is("{0}"));
        assertThat(nodes.get(2).toString(), is(" with some "));
        assertThat(nodes.get(3).toString(), is("{loggerName}"));
        assertThat(nodes.get(4).toString(), is(" and some "));
        assertThat(nodes.get(5).toString(), is("{1}"));
        assertThat(nodes.get(6).toString(), is(" bits "));
        assertThat(nodes.get(7).toString(), is("{time}"));
        assertThat(nodes.get(8).toString(), is(" and end bit"));

    }

    @Test public void test_parse_success_label_index() throws Exception {

        parser.parse("{0}", pattern.getPattern());

        List<Node> nodes = parser.getNodes();
        assertThat(nodes.size(), is(1));
        assertThat(nodes.get(0).toString(), is("{0}"));
    }
    
    @Test public void test_parse_success_label_name() throws Exception {

        parser.parse("{pattern}", pattern.getPattern());

        List<Node> nodes = parser.getNodes();
        assertThat(nodes.size(), is(1));
        assertThat(nodes.get(0).toString(), is("{1}"));
    }

    @Test public void test_parse_success_3() throws Exception {

        parser.parse("{event.pid}", pattern.getPattern());

        List<Node> nodes = parser.getNodes();
        assertThat(nodes.size(), is(1));
        assertThat(nodes.get(0).toString(), is("{pid}"));
    }

    @Test public void test_parse_fail_1() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Failed to parse pattern string 'This is a pattern {0 foo' - failed at 19 - unclosed pattern brace detected");
        parser.parse("This is a pattern {0 foo", pattern.getPattern());
    }

    @Test public void test_parse_fail_2() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Failed to parse pattern string '' - no pattern was detected");
        parser.parse("", pattern.getPattern());
    }

    @Test public void test_parse_fail_3() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Failed to parse pattern string 'hello' - failed at 0 - couldn't find any pattern variables?");
        parser.parse("hello", pattern.getPattern());
    }

    @Test public void test_parse_fail_4() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Failed to parse pattern string '{hello}' - failed at 7, attempting to parse token 'hello' - it wasn't a pattern variable, log event field or an integer");
        parser.parse("{hello}", pattern.getPattern());
    }

    @Test public void test_format() throws Exception {
    
        PatternisedLogEvent patternisedLogEvent1 = new PatternisedLogEvent();
        patternisedLogEvent1.setLoggerName("test logger name");
        patternisedLogEvent1.setSourceHost("test source host");
        patternisedLogEvent1.setVariables(new String[] {"a", "b", "c"});
        
        parser.parse("This is a pattern {0} with some {event.loggerName} and some {pattern} bits {event.sourceHost}", pattern.getPattern());
        assertThat(parser.format(patternisedLogEvent1), is("This is a pattern a with some test logger name and some b bits test source host"));
    
    }

}
