package com.logginghub.logging.utils;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.utils.ValueStripper2.ValueStripper2ResultListener;
import com.logginghub.logging.utils.ValueStripper2.ValueStripper2ResultListener2;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.CollectionUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class TestValueStripper2 {

    private ValueStripper2 stripper = new ValueStripper2();

    private LogEvent eventWithNumbers = LogEventFactory.createLogEvent("Value one 345 value two 12.34 and finally value three 3");
    private LogEvent eventWithMix = LogEventFactory.createLogEvent("Value one 345 value two 'foo' and finally value three 3");
    private LogEvent eventWithNastyChars = LogEventFactory.createLogEvent("Value one [345] value two {foo} and finally value three .?3");
    private LogEvent eventWithMixAndStuffOnEnd = LogEventFactory.createLogEvent("Value one 345 value two 'foo' and finally value three 3 and some more bits at the end");

    @Test public void test_with_metadata_non_numeric() {
        LogEvent eventWithMetadata = LogEventFactory.createLogEvent("Value one 345 value two Foo and finally value three 3");
        eventWithMetadata.getMetadata().put("a", "1");

        stripper.setPattern("Value one {one} value two [two] and finally value three {three}[[a]]");
        assertThat(stripper.getRegex(), is("Value one ([\\d-\\.,]*?) value two (.*?) and finally value three ([\\d-\\.,]*?)"));

        final Bucket<PatternisedLogEvent> patterisedEvents = new Bucket<PatternisedLogEvent>();
        stripper.addResultListener(new ValueStripper2ResultListener2() {
            @Override
            public void onNewPatternisedResult(PatternisedLogEvent event, boolean[] isNumeric) {
                patterisedEvents.add(event);
            }
        });

        stripper.onNewLogEvent(eventWithMetadata);

        assertThat(patterisedEvents.size(), is(1));

        PatternisedLogEvent patternisedLogEvent = patterisedEvents.get(0);
        String[] variables = patternisedLogEvent.getVariables();

        assertThat(variables.length, is(4));
        assertThat(variables[0], is("345"));
        assertThat(variables[1], is("Foo"));
        assertThat(variables[2], is("3"));
        assertThat(variables[3], is("1"));

        List<String> labels = stripper.getLabels();
        assertThat(labels.size(), is(4));
        assertThat(labels.get(0), is("one"));
        assertThat(labels.get(1), is("two"));
        assertThat(labels.get(2), is("three"));
        assertThat(labels.get(3), is("a"));

        assertThat(stripper.isNumericField(0), is(true));
        assertThat(stripper.isNumericField(1), is(false));
        assertThat(stripper.isNumericField(2), is(true));
        assertThat(stripper.isNumericField(3), is(false));

    }

    @Test public void test_with_metadata_numeric() {
        LogEvent eventWithMetadata = LogEventFactory.createLogEvent("Value one 345 value two Foo and finally value three 3");
        eventWithMetadata.getMetadata().put("a", "1");

        stripper.setPattern("Value one {one} value two [two] and finally value three {three}{{a}}");
        assertThat(stripper.getRegex(), is("Value one ([\\d-\\.,]*?) value two (.*?) and finally value three ([\\d-\\.,]*?)"));

        final Bucket<PatternisedLogEvent> patterisedEvents = new Bucket<PatternisedLogEvent>();
        stripper.addResultListener(new ValueStripper2ResultListener2() {
            @Override
            public void onNewPatternisedResult(PatternisedLogEvent event, boolean[] isNumeric) {
                patterisedEvents.add(event);
            }
        });

        stripper.onNewLogEvent(eventWithMetadata);

        assertThat(patterisedEvents.size(), is(1));

        PatternisedLogEvent patternisedLogEvent = patterisedEvents.get(0);
        String[] variables = patternisedLogEvent.getVariables();

        assertThat(variables.length, is(4));
        assertThat(variables[0], is("345"));
        assertThat(variables[1], is("Foo"));
        assertThat(variables[2], is("3"));
        assertThat(variables[3], is("1"));

        List<String> labels = stripper.getLabels();
        assertThat(labels.size(), is(4));
        assertThat(labels.get(0), is("one"));
        assertThat(labels.get(1), is("two"));
        assertThat(labels.get(2), is("three"));
        assertThat(labels.get(3), is("a"));

        assertThat(stripper.isNumericField(0), is(true));
        assertThat(stripper.isNumericField(1), is(false));
        assertThat(stripper.isNumericField(2), is(true));
        assertThat(stripper.isNumericField(3), is(true));

    }

    @Test public void test_with_metadata_in_middle() {
        LogEvent eventWithMetadata = LogEventFactory.createLogEvent("Value one 345 value two Foo and finally value three 3");
        eventWithMetadata.getMetadata().put("a", "1");

        stripper.setPattern("Value one {one} value two [two]{{a}} and finally value three {three}");
        assertThat(stripper.getRegex(), is("Value one ([\\d-\\.,]*?) value two (.*?) and finally value three ([\\d-\\.,]*?)"));

        final Bucket<PatternisedLogEvent> patterisedEvents = new Bucket<PatternisedLogEvent>();
        stripper.addResultListener(new ValueStripper2ResultListener2() {
            @Override
            public void onNewPatternisedResult(PatternisedLogEvent event, boolean[] isNumeric) {
                patterisedEvents.add(event);
            }
        });

        stripper.onNewLogEvent(eventWithMetadata);

        assertThat(patterisedEvents.size(), is(1));

        PatternisedLogEvent patternisedLogEvent = patterisedEvents.get(0);
        String[] variables = patternisedLogEvent.getVariables();

        assertThat(variables.length, is(4));
        assertThat(variables[0], is("345"));
        assertThat(variables[1], is("Foo"));
        assertThat(variables[2], is("3"));
        assertThat(variables[3], is("1"));

        List<String> labels = stripper.getLabels();
        assertThat(labels.size(), is(4));
        assertThat(labels.get(0), is("one"));
        assertThat(labels.get(1), is("two"));
        assertThat(labels.get(2), is("three"));
        assertThat(labels.get(3), is("a"));

        assertThat(stripper.isNumericField(0), is(true));
        assertThat(stripper.isNumericField(1), is(false));
        assertThat(stripper.isNumericField(2), is(true));
        assertThat(stripper.isNumericField(3), is(true));

    }

    @Test public void test_with_metadata_at_start() {
        LogEvent eventWithMetadata = LogEventFactory.createLogEvent("Value one 345 value two Foo and finally value three 3");
        eventWithMetadata.getMetadata().put("a", "1");

        stripper.setPattern("{{a}}Value one {one} value two [two] and finally value three {three}");
        assertThat(stripper.getRegex(), is("Value one ([\\d-\\.,]*?) value two (.*?) and finally value three ([\\d-\\.,]*?)"));

        final Bucket<PatternisedLogEvent> patterisedEvents = new Bucket<PatternisedLogEvent>();
        stripper.addResultListener(new ValueStripper2ResultListener2() {
            @Override
            public void onNewPatternisedResult(PatternisedLogEvent event, boolean[] isNumeric) {
                patterisedEvents.add(event);
            }
        });

        stripper.onNewLogEvent(eventWithMetadata);

        assertThat(patterisedEvents.size(), is(1));

        PatternisedLogEvent patternisedLogEvent = patterisedEvents.get(0);
        String[] variables = patternisedLogEvent.getVariables();

        assertThat(variables.length, is(4));
        assertThat(variables[0], is("345"));
        assertThat(variables[1], is("Foo"));
        assertThat(variables[2], is("3"));
        assertThat(variables[3], is("1"));

        List<String> labels = stripper.getLabels();
        assertThat(labels.size(), is(4));
        assertThat(labels.get(0), is("one"));
        assertThat(labels.get(1), is("two"));
        assertThat(labels.get(2), is("three"));
        assertThat(labels.get(3), is("a"));

        assertThat(stripper.isNumericField(0), is(true));
        assertThat(stripper.isNumericField(1), is(false));
        assertThat(stripper.isNumericField(2), is(true));
        assertThat(stripper.isNumericField(3), is(true));

    }


    @Test public void test_escaping() {
        stripper.setPattern("[non-numeric]");
        assertThat(stripper.getRegex(), is("(.*?)"));

        stripper.setPattern("{numeric}");
        assertThat(stripper.getRegex(), is("([\\d-\\.,]*?)"));

        stripper.setPattern("|[[non-numeric]|]");
        assertThat(stripper.getRegex(), is("\\[(.*?)\\]"));
        
        stripper.setPattern("\\[[non-numeric]\\]");
        assertThat(stripper.getRegex(), is("\\[(.*?)\\]"));

        stripper.setPattern("|[{numeric}|]");
        assertThat(stripper.getRegex(), is("\\[([\\d-\\.,]*?)\\]"));

        stripper.setPattern(".[non-numeric]");
        assertThat(stripper.getRegex(), is("\\.(.*?)"));

        stripper.setPattern("||[non-numeric]");
        assertThat(stripper.getRegex(), is("\\|(.*?)"));

        stripper.setPattern("|}|[{numeric}|]");
        assertThat(stripper.getRegex(), is("\\}\\[([\\d-\\.,]*?)\\]"));
    }

    @Test public void test_numeric_with_comma() {
        stripper.setPattern("{numeric}");
        assertThat(stripper.patternise(event("1,322.23")), is(new String[] { "1,322.23" }));
    }

    @Test public void test_escaping_non_numeric_field() {
        stripper.setPattern("Value one |[{valueOne}|] value two |{[valueTwo]|} and finally value three .?{valueThree}");
        assertThat(stripper.getStartsWith(), is("Value one ["));
        
        String[] patternise = stripper.patternise(eventWithNastyChars);
        assertThat(patternise, is(new String[] { "345", "foo", "3" }));
        assertThat(stripper.getLabels().get(0), is("valueOne"));
        assertThat(stripper.getLabels().get(1), is("valueTwo"));
        assertThat(stripper.getLabels().get(2), is("valueThree"));

        assertThat(stripper.isNumericField(0), is(true));
        assertThat(stripper.isNumericField(1), is(false));
        assertThat(stripper.isNumericField(2), is(true));
    }

    @Test public void test_non_numeric_field() {
        stripper.setPattern("Value one {valueOne} value two [valueTwo] and finally value three {valueThree}");
        String[] patternise = stripper.patternise(eventWithMix);
        assertThat(patternise, is(new String[] { "345", "'foo'", "3" }));
        assertThat(stripper.getLabels().get(0), is("valueOne"));
        assertThat(stripper.getLabels().get(1), is("valueTwo"));
        assertThat(stripper.getLabels().get(2), is("valueThree"));

        assertThat(stripper.isNumericField(0), is(true));
        assertThat(stripper.isNumericField(1), is(false));
        assertThat(stripper.isNumericField(2), is(true));
    }
    
    @Test public void test_non_numeric_field_with_fixed_at_end() {
        stripper.setPattern("Value one {valueOne} value two [valueTwo] and finally value three {valueThree} and some more bits at the end");
        
        String[] fixed = CollectionUtils.toArray(stripper.getFixedElements());
        assertThat(fixed, is(new String[] { "Value one ", " value two ", " and finally value three ", " and some more bits at the end" }));
        
        String[] patternise = stripper.patternise(eventWithMixAndStuffOnEnd);
        assertThat(patternise, is(new String[] { "345", "'foo'", "3" }));
        assertThat(stripper.getLabels().get(0), is("valueOne"));
        assertThat(stripper.getLabels().get(1), is("valueTwo"));
        assertThat(stripper.getLabels().get(2), is("valueThree"));

        assertThat(stripper.isNumericField(0), is(true));
        assertThat(stripper.isNumericField(1), is(false));
        assertThat(stripper.isNumericField(2), is(true));
        
        assertThat(stripper.depatternise(patternise), is("Value one 345 value two 'foo' and finally value three 3 and some more bits at the end"));
    }

    @Test public void test_patternised_result() {
        stripper.setPattern("Value one {valueOne} value two [valueTwo] and finally value three {valueThree}");

        final Bucket<String[]> bucket = new Bucket<String[]>();
        ValueStripper2ResultListener listener = new ValueStripper2ResultListener() {
            @Override public void onNewResult(String label, boolean isNumeric, String value, LogEvent entry) {}

            @Override public void onNewPatternisedResult(String[] labels, String[] patternVariables, boolean[] isNumeric, LogEvent entry) {
                bucket.add(patternVariables);
            }
        };

        stripper.addResultListener(listener);
        assertThat(stripper.match(eventWithMix), is(true));

        assertThat(bucket.size(), is(1));
        assertThat(bucket.get(0), is(new String[] { "345", "'foo'", "3" }));
    }

    @Test public void test_patternised_result_with_numeric_only() {
        stripper.setPattern("Value one {valueOne} value two {valueTwo} and finally value three {valueThree}");

        final Bucket<String[]> bucket = new Bucket<String[]>();
        ValueStripper2ResultListener listener = new ValueStripper2ResultListener() {
            @Override public void onNewResult(String label, boolean isNumeric, String value, LogEvent entry) {}

            @Override public void onNewPatternisedResult(String[] labels, String[] patternVariables, boolean[] isNumeric, LogEvent entry) {
                bucket.add(patternVariables);
            }
        };

        stripper.addResultListener(listener);
        assertThat(stripper.match(eventWithMix), is(false));
    }

    @Test public void test() {

        stripper.setPattern("Value one {valueOne} value two {valueTwo} and finally value three {valueThree}");
        assertThat(stripper.getStartsWith(), is("Value one "));

        assertThat(stripper.getRegex(), is("Value one ([\\d-\\.,]*?) value two ([\\d-\\.,]*?) and finally value three ([\\d-\\.,]*?)"));       
        String[] labels = CollectionUtils.toArray(stripper.getLabels());
        assertThat(labels, is(new String[] { "valueOne", "valueTwo", "valueThree" }));

        List<String> fixedElements = stripper.getFixedElements();
        String[] fixed = CollectionUtils.toArray(fixedElements);
        assertThat(fixed, is(new String[] { "Value one ", " value two ", " and finally value three " }));
        
        final List<String> resultLabels = new ArrayList<String>();
        final List<String> resultValues = new ArrayList<String>();
        final List<LogEvent> resultEvents = new ArrayList<LogEvent>();

        stripper.addResultListener(new ValueStripper2ResultListener() {
            public void onNewResult(String label, boolean isNumeric, String value, LogEvent entry) {
                resultLabels.add(label);
                resultValues.add(value);
                resultEvents.add(entry);
            }

            @Override public void onNewPatternisedResult(String[] labels, String[] patternVariables, boolean[] isNumeric, LogEvent entry) {}
        });

        stripper.onNewLogEvent(eventWithNumbers);

        assertThat(resultLabels.size(), is(3));
        assertThat(resultValues.size(), is(3));
        assertThat(resultEvents.size(), is(3));

        assertThat(resultLabels.get(0), is("valueOne"));
        assertThat(resultLabels.get(1), is("valueTwo"));
        assertThat(resultLabels.get(2), is("valueThree"));

        assertThat(resultValues.get(0), is("345"));
        assertThat(resultValues.get(1), is("12.34"));
        assertThat(resultValues.get(2), is("3"));

        assertThat(resultEvents.get(0), is(sameInstance(eventWithNumbers)));
        assertThat(resultEvents.get(1), is(sameInstance(eventWithNumbers)));
        assertThat(resultEvents.get(2), is(sameInstance(eventWithNumbers)));
    }
    
    @Test public void test_depaternise() {
        validate("Fixed 4", "Fixed {value}");
        validate("Fixed [4]", "Fixed \\[{value}\\]");
        validate("4", "{value}");
        validate("4 Fixed", "{value} Fixed");
        validate(" 4 Fixed ", " {value} Fixed ");
        validate("Fixed 4 middle ten end", "Fixed {value} middle [another] end");
        validate(" 1 2 3 4 ", "[value] {value} {value} [value]");
        validate(" 4 Fixed ", "[everything]");
    }

    private void validate(String input, String pattern) {
        stripper.setPattern(pattern);
        String[] patternised = stripper.patternise(event(input));
        assertThat(patternised, is(not(nullValue())));
        String depatternised = stripper.depatternise(patternised);
        assertThat(depatternised, is(input));
    }

    private LogEvent event(String string) {
        return LogEventFactory.createLogEvent(string);
    }

}
