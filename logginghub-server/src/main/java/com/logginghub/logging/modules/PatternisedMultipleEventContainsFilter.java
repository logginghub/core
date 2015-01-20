package com.logginghub.logging.modules;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.filters.CompositeAndFilter;
import com.logginghub.logging.filters.NotFilter;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

public class PatternisedMultipleEventContainsFilter implements Filter<PatternisedLogEvent> {

    private static final Logger logger = Logger.getLoggerFor(CompositeAndFilter.class);
    private List<Filter<PatternisedLogEvent>> filters = new CopyOnWriteArrayList<Filter<PatternisedLogEvent>>();

    private boolean useRegex = false;
    private String value;

    public PatternisedMultipleEventContainsFilter(String value, boolean useRegex) {
        this.value = value;
        this.useRegex = useRegex;
        setEventContainsString(value);
    }

    public void setUseRegex(boolean useRegex) {
        this.useRegex = useRegex;
        // Need to rebuild the filters
        setEventContainsString(value);
    }

    public void setEventContainsString(String value) {
        this.value = value;

        clearFilters();
        if (looksLikeComplexSearch(value)) {
            value = fixComplexQuery(value);
            char currentModifier = ' ';
            StringBuilder currentPhrase = new StringBuilder();
            boolean escapeNext = false;
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);

                if ((c == '+' || c == '-') && !escapeNext) {
                    if (currentPhrase != null && currentPhrase.length() > 0) {
                        String phrase = currentPhrase.toString().trim();
                        smartAddFilter(currentModifier, phrase);
                    }
                    currentModifier = c;
                    currentPhrase = new StringBuilder();
                    escapeNext = false;
                }
                else if (c == '\\') {
                    escapeNext = true;
                }
                else {
                    currentPhrase.append(c);
                    escapeNext = false;
                }
            }

            // Remember to add the last one
            if (currentPhrase != null) {
                String phrase = currentPhrase.toString().trim();
                smartAddFilter(currentModifier, phrase);
            }
        }
        else {
            Filter<PatternisedLogEvent> filter;
            if (useRegex) {
                filter = new PatternisedEventMatchesFilter(value);
            }
            else {
                filter = new PatternisedEventContainsFilter(value);
            }
            addFilter(filter);
        }
    }

    /**
     * Adds + to any tokens in a complex query that might be missing one to make the next parsing
     * stage easier.
     * 
     * @param value
     * @return
     */
    private String fixComplexQuery(String value) {

        StringBuilder builder = new StringBuilder();
        String[] tokens = value.split("\\s");
        String pad = "";
        for (String string : tokens) {
            builder.append(pad);
            pad = " ";
            if (string.startsWith("+") || string.startsWith("-")) {
                // Fine, no need to fix
            }
            else {
                builder.append("+");
            }

            builder.append(string);
        }

        String fixed = builder.toString();
        return fixed;
    }

    private boolean looksLikeComplexSearch(String value) {

        String[] tokens = value.split("\\s");

        boolean looksLikeComplexSearch = false;
        for (String string : tokens) {
            if (string.startsWith("+") || string.startsWith("-")) {
                looksLikeComplexSearch = true;
                break;
            }
        }

        return looksLikeComplexSearch;
    }

    private void smartAddFilter(char currentModifier, String phrase) {
        Filter<PatternisedLogEvent> filter;
        if (useRegex) {
            filter = new PatternisedEventMatchesFilter(phrase);
        }
        else {
            filter = new PatternisedEventContainsFilter(phrase);
        }

        if (currentModifier == '+') {
            addFilter(filter);
        }
        else {
            addFilter(new NotFilter<PatternisedLogEvent>(filter));
        }
    }

    public void addFilter(Filter<PatternisedLogEvent> filter) {
        filters.add(filter);
    }

    public void removeFilter(Filter<PatternisedLogEvent> filter) {
        filters.remove(filter);
    }

    public void clearFilters() {
        filters.clear();
    }

    public boolean passes(PatternisedLogEvent event) {
        boolean passes = true;

        for (Filter<PatternisedLogEvent> filter : filters) {
            passes &= filter.passes(event);
            logger.trace("Checked filter [{}] '{}' against event '{}'", passes, filter, event);

            if (!passes) {
                break;
            }
        }

        return passes;
    }

    public List<Filter<PatternisedLogEvent>> getFilters() {
        return filters;
    }

    @Override public String toString() {
        return "CompositeAndFilter [filters=" + filters + "]";
    }

}
