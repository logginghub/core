package com.logginghub.logging.utils;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.Is;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValueStripper2 implements LogEventListener {

    /**
     * @deprecated Use @ValueStripper2ResultListener2
     * @author james
     */
    @Deprecated    
    public interface ValueStripper2ResultListener {
        void onNewResult(String label, boolean isNumeric, String value, LogEvent entry);
        void onNewPatternisedResult(String[] labels, String[] patternVariables, boolean[] isNumeric, LogEvent entry);
    }
    
    public interface ValueStripper2ResultListener2 {
        void onNewPatternisedResult(PatternisedLogEvent event, boolean[] isNumeric);
    }

    private static final Logger logger = Logger.getLoggerFor(ValueStripper2.class);
    private static final String floatingPointNumbersMatcher = "([\\d-\\.,]*?)";
    private static final String anythingMatcher = "(.*?)";

    private Filter<LogEvent> filter;

    private String regex;
    private Pattern pattern;

    private List<String> fixedElements = new ArrayList<String>();
    private List<String> labelsx;
    private String[] labelsArray;
    private boolean[] isNumericArray;
    private List<Boolean> isNumericField = new ArrayList<Boolean>();

    private boolean debug;

    private List<ValueStripper2ResultListener> oldListeners = new CopyOnWriteArrayList<ValueStripper2ResultListener>();
    private List<ValueStripper2ResultListener2> listeners = new CopyOnWriteArrayList<ValueStripper2ResultListener2>();

    private boolean matchAnything = false;
    private int[] matcherIndices = null;
    private int labelCount;

    private List<Boolean> layout;
    private String patternName;
    private int patternID;
    private String rawPattern;
    private String startsWith = null;

    public ValueStripper2() {}

    public ValueStripper2(String pattern) {
        setPattern(pattern);
    }

    /**
     * Only generate results for the match capture group indicies provided
     * 
     * @param matcherIndices
     */
    public void setMatcherIndices(int... matcherIndices) {
        this.matcherIndices = matcherIndices;
    }

    public void setRegex(String regex) {
        this.regex = regex;
//        System.out.println(regex);
        this.pattern = Pattern.compile(regex, Pattern.DOTALL);
    }

    // public void setMatchAnything(boolean matchAnything) {
    // this.matchAnything = matchAnything;
    // }
    //
    // public boolean isMatchAnything() {
    // return matchAnything;
    // }

    public void setLabels(List<String> labels) {
        this.labelsx = labels;
        this.labelsArray = StringUtils.toArray(labels);
        this.labelCount = labels.size();
    }

    public void onNewLogEvent(LogEvent entry) {
        match(entry);
    }

    public boolean match(LogEvent entry) {
        logger.finer("Matching event '{}'", entry);

        boolean match = false;
        if (pattern == null) {
            logger.warn("Attempting to match a log entry against a value stripper with no pattern configured, something has gone wrong");
        }
        else {

            if (filter == null || filter.passes(entry)) {
                String message = entry.getMessage();

                if (startsWith == null || message.startsWith(startsWith)) {

                    if (debug) {
                        logger.fine("Message is : '{}'", message);
                        logger.fine("Pattern is : '{}'", regex);
                    }

                    Matcher matcher = pattern.matcher(message);
                    if (matcher.matches()) {
                        logger.finer("Message '{}' matched pattern '{}'", message, getRegex());
                        if (debug) {
                            logger.fine("Start matched line [{}]", message);

                            for (int i = 0; i < matcher.groupCount(); i++) {
                                String label = labelsx.get(i);
                                logger.fine("Group " + i + " : " + label + " : '" + matcher.group(i + 1) + "'");
                            }
                        }

                        match = true;

                        if (matcherIndices == null) {

                            String[] variables = new String[labelsx.size()];
                            for (int i = 0; i < variables.length; i++) {
                                variables[i] = matcher.group(i + 1);
                            }

                            if (logger.willLog(Logger.fine)) {
                                logger.fine("Pattern match '{}' : {} = {}", patternName, Arrays.toString(labelsArray), Arrays.toString(variables));
                            }
                            
                            PatternisedLogEvent event = fromEntry(entry, variables);

                            fireNewResult(event, isNumericArray);
                            fireNewResult(variables, isNumericArray, entry);

                            int index = 1;
                            for (String label : labelsx) {
                                String labelValue = matcher.group(index);
                                fireNewResult(label, isNumericField(index - 1), labelValue, entry);
                                index++;
                            }
                        }
                        else {
                            for (int i : matcherIndices) {
                                int index = matcherIndices[i];
                                String label = labelsx.get(index);
                                String labelValue = matcher.group(index);
                                fireNewResult(label, isNumericField(index), labelValue, entry);
                            }
                        }
                    }
                }
            }
        }

        return match;
    }

    private PatternisedLogEvent fromEntry(LogEvent entry, String[] variables) {
        PatternisedLogEvent event = new PatternisedLogEvent();
        event.setChannel(entry.getChannel());
        event.setLevel(entry.getLevel());
        event.setLoggerName(entry.getLoggerName());
        event.setPatternID(patternID);
        event.setPid(entry.getPid());
        event.setSequenceNumber(entry.getSequenceNumber());
        event.setSourceAddress(entry.getSourceAddress());
        event.setSourceApplication(entry.getSourceApplication());
        event.setSourceClassName(entry.getSourceClassName());
        event.setSourceHost(entry.getSourceHost());
        event.setSourceMethodName(entry.getSourceMethodName());
        event.setThreadName(entry.getThreadName());
        event.setTime(entry.getOriginTime());
        event.setVariables(variables);
        return event;
    }

    public void setFilter(Filter<LogEvent> filter) {
        this.filter = filter;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void addResultListener(ValueStripper2ResultListener listener) {
        oldListeners.add(listener);
    }

    public void removeResultListener(ValueStripper2ResultListener listener) {
        oldListeners.remove(listener);
    }
    
    public void addResultListener(ValueStripper2ResultListener2 listener) {
        listeners.add(listener);
    }

    public void removeResultListener(ValueStripper2ResultListener2 listener) {
        listeners.remove(listener);
    }

    private void fireNewResult(PatternisedLogEvent event, boolean[] isNumeric) {
        for (ValueStripper2ResultListener2 listener : listeners) {
            listener.onNewPatternisedResult(event, isNumeric);
        }
    }
    
    private void fireNewResult(String label, boolean isNumeric, String value, LogEvent entry) {
        for (ValueStripper2ResultListener listener : oldListeners) {
            listener.onNewResult(label, isNumeric, value, entry);
        }
    }

    private void fireNewResult(String[] variables, boolean[] isBoolean, LogEvent entry) {
        for (ValueStripper2ResultListener listener : oldListeners) {
            listener.onNewPatternisedResult(labelsArray, variables, isBoolean, entry);
        }
    }

    // public void setPattern(String string) {
    // setPattern(string, true);
    // }

    public String getRawPattern() {
        return rawPattern;
    }

    public void setPattern(String string) {
        Is.notNull(string, "Pattern string cannot be null");
        this.rawPattern = string;
        boolean escaped = false;
        boolean inTag = false;
        boolean isNumeric = false;

        this.startsWith = null;

        String input = string;

        layout = new ArrayList<Boolean>();
        fixedElements = new ArrayList<String>();

        StringBuilder regex = new StringBuilder();
        StringBuilder label = new StringBuilder();
        StringBuilder fixed = new StringBuilder();

        List<String> labels = new ArrayList<String>();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '{' || c == '[') {
                if (escaped) {
                    if (inTag) {
                        label.append(c);
                    }
                    else {
                        if (isRegexChar(c)) {
                            regex.append("\\");
                            regex.append(c);
                        }
                        else {
                            regex.append(c);
                        }
                        fixed.append(c);
                    }
                    escaped = false;
                }
                else {
                    if (c == '[') {
                        // This is the non-numeric field tag, so we need to
                        // switch modes to match anything
                        matchAnything = true;
                        isNumeric = false;
                    }
                    else {
                        matchAnything = false;
                        isNumeric = true;
                    }

                    if (startsWith == null) {
                        startsWith = fixed.toString();
                    }

                    inTag = true;

                    fixedElements.add(fixed.toString());
                    fixed = new StringBuilder();
                    layout.add(true);
                }
            }
            else if (c == '}' || c == ']') {
                if (escaped) {
                    if (inTag) {
                        label.append(c);
                    }
                    else {
                        if (isRegexChar(c)) {
                            regex.append("\\");
                            regex.append(c);
                        }
                        else {
                            regex.append(c);
                        }

                        fixed.append(c);
                    }
                    escaped = false;
                }
                else {
                    inTag = false;

                    if (label.length() == 0) {
                        // This just means throw this bit away
                        regex.append(".*?");
                    }
                    else {
                        if (matchAnything) {
                            regex.append(anythingMatcher);
                        }
                        else {
                            regex.append(floatingPointNumbersMatcher);
                        }

                        labels.add(label.toString());
                        layout.add(false);
                        label = new StringBuilder();
                        isNumericField.add(isNumeric);
                    }
                }
            }
            else if (c == '|' || c == '\\') {
                if (escaped) {
                    if (isRegexChar(c)) {
                        regex.append("\\");
                        regex.append(c);
                    }
                    else {
                        regex.append(c);
                    }
                    escaped = false;
                }
                else {
                    escaped = true;
                }
            }
            else {
                if (inTag) {
                    label.append(c);
                }
                else {
                    if (isRegexChar(c)) {
                        regex.append("\\");
                        regex.append(c);
                        fixed.append(c);
                    }
                    else {
                        regex.append(c);
                        fixed.append(c);
                    }
                }
            }
        }

        if (fixed.length() > 0) {
            fixedElements.add(fixed.toString());
            layout.add(true);
        }

        setLabels(labels);
        this.isNumericArray = new boolean[labels.size()];
        for (int i = 0; i < this.isNumericArray.length; i++) {
            this.isNumericArray[i] = isNumericField.get(i);
        }

        String regexString = regex.toString();
        setRegex(regexString);
    }

    private boolean isRegexChar(char c) {
        boolean isRegex = false;
        switch (c) {
            case '.':
            case '?':
            case '*':
            case '[':
            case ']':
            case '\\':
            case '^':
            case '$':
            case '|':
            case '+':
            case '(':
            case ')':
            case '{':
            case '}':
                isRegex = true;

        }

        return isRegex;

    }

    public String getRegex() {
        return this.regex;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public List<String> getLabels() {
        return labelsx;
    }

    public void clearListeners() {
        oldListeners.clear();
    }

    public String[] patternise(LogEvent entry) {

        String[] variables = null;

        if (filter == null || filter.passes(entry)) {
            String message = entry.getMessage();
            if (startsWith == null || message.startsWith(startsWith)) {
                Matcher matcher = pattern.matcher(message);
                if (matcher.matches()) {
                    variables = new String[labelCount];
                    for (int i = 0; i < variables.length; i++) {
                        variables[i] = matcher.group(i + 1);
                    }
                }
            }
        }

        return variables;

    }

    public PatternisedLogEvent patternise2(LogEvent entry) {

        PatternisedLogEvent patternised = null;

        if (filter == null || filter.passes(entry)) {
            String message = entry.getMessage();
            if (startsWith == null || message.startsWith(startsWith)) {
                Matcher matcher = pattern.matcher(message);
                if (matcher.matches()) {
                    patternised = new PatternisedLogEvent(entry.getLevel(),
                                                          entry.getOriginTime(),
                                                          entry.getSequenceNumber(),
                                                          patternID,
                                                          entry.getSourceApplication());

                    String[] variableArray = new String[labelsArray.length];
                    for (int i = 0; i < labelsArray.length; i++) {
                        String variable = matcher.group(i + 1);
                        variableArray[i] = variable;
                    }

                    patternised = fromEntry(entry, variableArray);
                }
            }
        }

        return patternised;
    }

    public boolean isNumericField(int i) {
        return isNumericField.get(i);
    }

    public List<String> getFixedElements() {
        return fixedElements;
    }

    public String depatternise(String[] patternised) {

        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = fixedElements.iterator();

        int variableIndex = 0;
        for (boolean isFixed : layout) {
            if (isFixed) {
                builder.append(iterator.next());
            }
            else {
                builder.append(patternised[variableIndex]);
                variableIndex++;
            }
        }

        return builder.toString();

    }

    public String depatternise(PatternisedLogEvent patternisedLogEvent) {
        return depatternise(patternisedLogEvent.getVariables());
    }

    public String depatternise(Map<String, String> variableMap) {

        StringBuilder builder = new StringBuilder();

        Iterator<String> fixedIterator = fixedElements.iterator();
        Iterator<String> variableIterator = labelsx.iterator();

        for (boolean isFixed : layout) {
            if (isFixed) {
                builder.append(fixedIterator.next());
            }
            else {
                String variableName = variableIterator.next();
                String value = variableMap.get(variableName);
                builder.append(value);
            }
        }

        return builder.toString();

    }

    public void setPatternID(int patternID) {
        this.patternID = patternID;
    }

    public int getPatternID() {
        return patternID;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    public String getPatternName() {
        return patternName;
    }

    public String getStartsWith() {
        return startsWith;
    }

    public void setStartsWith(String startsWith) {
        this.startsWith = startsWith;
    }

    public boolean isDebug() {
        return this.debug;

    }

    @Override public String toString() {
        return "ValueStripper2 [patternName=" + patternName + ", pattern=" + pattern + "]";
    }

    public int getLabelIndex(String string) {
        int index = labelsx.indexOf(string);
        return index;
    }

}
