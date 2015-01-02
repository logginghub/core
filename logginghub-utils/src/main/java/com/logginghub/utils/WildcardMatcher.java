package com.logginghub.utils;


public class WildcardMatcher {
    private String value;
    private String[] split;
    private boolean endingWildcard;
    private boolean startingWildcard;
    private boolean caseSensitive = false;
    private boolean emptyMatches = false;

    public WildcardMatcher(String value) {
        setValue(value);
    }

    public WildcardMatcher() {}

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean matches(String toCheck) {
        if (split.length == 1 && split[0].isEmpty() && emptyMatches) {
            return true;
        }
        else {
            boolean passes = wildcardCheck(toCheck);
            return passes;
        }
    }

    public boolean wildcardCheck(String toCheck) {
        if (!caseSensitive) {
            toCheck = toCheck.toLowerCase();
        }

        // Iterate over the cards.
        for (int i = 0; i < split.length; i++) {
            String portion = split[i];

            int idx = toCheck.indexOf(portion);

            if (i == 0 && !startingWildcard && idx != 0) {
                // This means its the first portion, the value hasn't started
                // with a wildcard, and the string was found some way into the
                // text. So this isn't a match.
                return false;
            }

            if (i == split.length && !endingWildcard && (idx + portion.length() != toCheck.length())) {
                // Ok this one means we've reached the end of the things we
                // needed to check but haven't reached the end of the string
                // yet, and thelast token wasn't a wildcard. So we haven't
                // matched.
                return false;
            }

            // Card not detected in the text.
            if (idx == -1) {
                return false;
            }

            // Move ahead, towards the right of the text.
            toCheck = toCheck.substring(idx + portion.length());
        }

        if (toCheck.length() > 0 && !endingWildcard) {
            // We've still got some stuff on the end, and there is no ending
            // wildcard :(
            return false;
        }

        return true;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (!caseSensitive) {
            value = value.toLowerCase();
        }
        this.value = value;
        this.split = value.split("\\*");
        this.startingWildcard = value.startsWith("*");
        this.endingWildcard = value.endsWith("*");
    }

    @Override public String toString() {
        return StringUtils.reflectionToString(this);
    }

    public static boolean matches(String pattern, String string) {
        WildcardMatcher matcher = new WildcardMatcher(pattern);
        return matcher.matches(string);

    }
    
    public void setEmptyMatches(boolean emptyMatches) {
        this.emptyMatches = emptyMatches;
    }

    public boolean isEmptyMatches() {
        return emptyMatches;
    }
    

}
