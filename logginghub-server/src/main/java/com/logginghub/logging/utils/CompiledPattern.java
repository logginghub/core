package com.logginghub.logging.utils;

import com.logginghub.logging.api.patterns.Pattern;

/**
 * Created by james on 21/11/14.
 */
public class CompiledPattern {

    private final Pattern pattern;
    private final ValueStripper2 valueStripper;

    public CompiledPattern(Pattern pattern) {
        this.pattern = pattern;
        this.valueStripper = new ValueStripper2(pattern.getPattern());
    }

    public Pattern getPattern() {
        return pattern;
    }

    public ValueStripper2 getValueStripper() {
        return valueStripper;
    }
}
