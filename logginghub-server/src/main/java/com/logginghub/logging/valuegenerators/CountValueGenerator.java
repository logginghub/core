package com.logginghub.logging.valuegenerators;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

public class CountValueGenerator extends FilteringValueGenerator<Integer> {
    public CountValueGenerator(Filter<LogEvent> filter) {
        super(filter);
    }

    private int count;

    @Override protected void onNewFilteredValue(LogEvent event) {
        count++;
    }

    public void reset() {
        count = 0;
    }

    public Integer getValue() {
        return count;
    }

}
