package com.logginghub.logging.frontend.analysis;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.utils.ValueStripper2.ValueStripper2ResultListener;
import com.logginghub.utils.logging.Logger;

public class ResultKeyBuilder implements ValueStripper2ResultListener {

    private static final Logger logger = Logger.getLoggerFor(ResultKeyBuilder.class);
    private List<ValueStripper2ResultListener> listeners = new CopyOnWriteArrayList<ValueStripper2ResultListener>();

    private final String pattern;

    public ResultKeyBuilder(String pattern) {
        this.pattern = pattern;
    }

    public void addResultListener(ValueStripper2ResultListener listener) {
        listeners.add(listener);
    }

    public void onNewResult(String label, boolean isNumeric, String value, LogEvent entry) {
        logger.debug("New result received : label '{}' (numeric {}) value '{}' for log event '{}'", label, isNumeric, value, entry);
        boolean escaped = false;
        boolean inTag = false;

        StringBuilder patternText = new StringBuilder();
        StringBuilder tag = new StringBuilder();

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);

            if (c == '{') {
                if (escaped) {
                    if (inTag) {
                        tag.append(c);
                    }
                    else {
                        patternText.append(c);
                    }

                }
                else {
                    inTag = true;
                }
            }
            else if (c == '}') {
                if (escaped) {
                    if (inTag) {
                        tag.append(c);
                    }
                    else {
                        patternText.append(c);
                    }
                }
                else {
                    inTag = false;

                    String tagString = tag.toString();
                    if (tagString.equals("host")) {
                        patternText.append(entry.getSourceHost());
                    }
                    else if (tagString.equals("source")) {
                        patternText.append(entry.getSourceApplication());
                    }
                    else if (tagString.equals("label")) {
                        patternText.append(label);
                    }

                    tag = new StringBuilder();
                }
            }
            else if (c == '\\') {
                if (escaped) {
                    patternText.append(c);
                    escaped = false;
                }
                else {
                    escaped = true;
                }
            }
            else {
                if (inTag) {
                    tag.append(c);
                }
                else {
                    patternText.append(c);
                }
            }
        }

        logger.debug("Built label '{}' from event and pattern '{}'", patternText.toString(), pattern);

        fireNewResult(patternText.toString(), isNumeric, value, entry);
    }

    public void removeResultListener(ValueStripper2ResultListener listener) {
        listeners.remove(listener);
    }

    private void fireNewResult(String label, boolean isNumeric, String value, LogEvent entry) {
        for (ValueStripper2ResultListener listener : listeners) {
            listener.onNewResult(label, isNumeric, value, entry);
        }
    }

    @Override public void onNewPatternisedResult(String[] labels, String[] patternVariables, boolean[] isBoolean, LogEvent entry) {
        logger.trace("Ingnoring call to onNewPatternisedResult - we expect the individual entries to come through onNewResult now");
    }
}
