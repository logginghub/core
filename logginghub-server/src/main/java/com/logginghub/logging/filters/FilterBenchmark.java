package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.utils.Benchmarker;
import com.logginghub.utils.Benchmarker.Approach;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.logging.Logger;

/**
 * Quick benchmark to validate the case insensitve filters.
 */
public class FilterBenchmark {

    private final static String content = FileUtils.read("../logginghub-server/message.template");

    private final static LogEvent event = LogEventBuilder.create(0, Logger.info, content);

    public final static class CaseSensitive extends Approach {
        CaseSensitiveEventContainsFilter containsFilter = new CaseSensitiveEventContainsFilter("abc");
        @Override public void iterate() throws Exception {
            containsFilter.passes(event);
        }
    }

    public final static class CaseInsenstive extends Approach {
        CaseInsensitiveEventContainsFilter containsFilter = new CaseInsensitiveEventContainsFilter("abc");
        @Override public void iterate() throws Exception {
            containsFilter.passes(event);
        }
    }

    public final static class CaseInsenstiveAscii extends Approach {
        CaseInsensitiveAsciiEventContainsFilter containsFilter = new CaseInsensitiveAsciiEventContainsFilter("abc");
        @Override public void iterate() throws Exception {
            containsFilter.passes(event);
        }
    }


    public static void main(String[] args) {
        Benchmarker.benchmark(5000, FilterBenchmark.class);
    }
}
