package com.logginghub.utils;

import java.util.regex.Pattern;

public class WildcardOrRegexMatcher implements StringMatcher {

    private Pattern pattern;
    private WildcardMatcher wildcard;

    public WildcardOrRegexMatcher(String pattern, boolean isRegex) {
        if (isRegex) {
            this.pattern = Pattern.compile(pattern);
        }
        else {
            this.wildcard = new WildcardMatcher(pattern);
        }
    }

    public boolean matches(String string) {
        boolean matches;
        if (string == null) {
            matches = false;
        }
        else {

            if (pattern != null) {
                matches = pattern.matcher(string).matches();
            }
            else {
                matches = wildcard.matches(string);
            }
        }
        return matches;
    }

}
