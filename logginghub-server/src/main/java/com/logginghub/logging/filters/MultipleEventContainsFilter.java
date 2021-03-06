package com.logginghub.logging.filters;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.filter.Filter;

/**
 * An enhancement to the standard event contains filter, this one supports multiple search strings using google style + and - operators.
 *
 * @author James
 */
public class MultipleEventContainsFilter extends SwitchingAndOrFilter {
    private final FilterFactory filterFactory;
    private boolean useRegex = false;
    private String value;

    public MultipleEventContainsFilter(String value, boolean useRegex, FilterFactory filterFactory) {
        this.value = value;
        this.useRegex = useRegex;
        this.filterFactory = filterFactory;
        setEventContainsString(value);
    }

    public void setEventContainsString(String value) {
        this.value = value;

        clearFilters();
        if (looksLikeComplexSearch(value)) {
            value = fixComplexQuery(value);
            char currentModifier = ' ';
            boolean orFlag = false;
            StringBuilder currentPhrase = new StringBuilder();
            boolean escapeNext = false;
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);

                if ((c == '+' || c == '-' || c == ',') && !escapeNext) {
                    if (currentPhrase != null && currentPhrase.length() > 0) {
                        String phrase = currentPhrase.toString().trim();
                        smartAddFilter(currentModifier, phrase);
                    }
                    currentModifier = c;
                    currentPhrase = new StringBuilder();
                    escapeNext = false;

                    if (c == ',') {
                        orFlag = true;
                    }
                } else if (c == '\\') {
                    escapeNext = true;
                } else {
                    currentPhrase.append(c);
                    escapeNext = false;
                }
            }

            // Remember to add the last one
            if (currentPhrase != null) {
                String phrase = currentPhrase.toString().trim();
                smartAddFilter(currentModifier, phrase);
            }

            // Check for the or flag (un-escaped commas)
            if (orFlag) {
                setApplyAndLogic(false);
            }
        } else {
            Filter<LogEvent> filter;
            if (useRegex) {
                filter = new EventMatchesFilter(value);
            } else {
                filter = filterFactory.createFilter(value);
            }
            addFilter(filter);
        }
    }

    private boolean looksLikeComplexSearch(String value) {

        String[] tokens = value.split("\\s");

        boolean looksLikeComplexSearch = false;
        for (String string : tokens) {
            if (string.startsWith("+") || string.startsWith("-") || string.endsWith(",")) {
                looksLikeComplexSearch = true;
                break;
            }
        }

        if (!looksLikeComplexSearch) {
            tokens = value.split("^\\,");
            looksLikeComplexSearch = tokens.length > 0;
        }

        return looksLikeComplexSearch;
    }

    /**
     * Adds + to any tokens in a complex query that might be missing one to make the next parsing stage easier.
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
            } else {
                builder.append("+");
            }

            builder.append(string);
        }

        String fixed = builder.toString();
        return fixed;
    }

    private void smartAddFilter(char currentModifier, String phrase) {
        Filter<LogEvent> filter;
        if (useRegex) {
            filter = new EventMatchesFilter(phrase);
        } else {
            filter = filterFactory.createFilter(phrase);
        }

        if (currentModifier == '-') {
            addFilter(new NotFilter<LogEvent>(filter));
        } else {
            addFilter(filter);
        }
    }

    public void setUseRegex(boolean useRegex) {
        this.useRegex = useRegex;
        // Need to rebuild the filters
        setEventContainsString(value);
    }
}
