package com.logginghub.logging.frontend.analysis;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.frontend.analysis.ResultKeyBuilder;
import com.logginghub.logging.utils.ValueStripper2.ValueStripper2ResultListener;

public class ResultKeyBuilderTest {
    @Test public void test() throws UnknownHostException {
        String pattern = "{host}-{source}-{label}";
        ResultKeyBuilder builder = new ResultKeyBuilder(pattern);
        LogEvent event = LogEventFactory.createFullLogEvent1();

        final List<String> resultLabels = new ArrayList<String>();
        final List<String> resultValues = new ArrayList<String>();
        final List<LogEvent> resultEvents = new ArrayList<LogEvent>();

        builder.addResultListener(new ValueStripper2ResultListener() {

            public void onNewResult(String label, boolean isNumeric, String value, LogEvent entry) {
                resultLabels.add(label);
                resultValues.add(value);
                resultEvents.add(entry);
            }

            @Override public void onNewPatternisedResult(String[] labels, String[] patternVariables, boolean[] isNumeric, LogEvent entry) {}
        });

        builder.onNewResult("valueOne", true, "34.23", event);

        String hostName = InetAddress.getLocalHost().getHostName();

        assertThat(resultLabels.size(), is(1));
        assertThat(resultLabels.get(0), is(hostName + "-TestApplication-valueOne"));
        assertThat(resultValues.get(0), is("34.23"));
        assertThat(resultEvents.get(0), is(sameInstance(event)));
    }
}
