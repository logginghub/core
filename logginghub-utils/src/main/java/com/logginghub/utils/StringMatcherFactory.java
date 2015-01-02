package com.logginghub.utils;

public class StringMatcherFactory {

    public enum MatcherType {
        StartsWith,
        Contains,
        EndsWith,
        Wildcard,
        // TODO : implement the +/- google style filter
        // GoogleStyle,
        Regex
    };

    public static StringMatcher createMatcher(MatcherType type, final String text) {
        StringMatcher matcher;

        switch (type) {

            case Contains: {
                matcher = new StringMatcher() {
                    public boolean matches(String input) {
                        if (input != null && text != null) {
                            return input.contains(text);
                        }
                        else {
                            return false;
                        }
                    }
                };
                break;
            }
            case EndsWith: {
                matcher = new StringMatcher() {
                    public boolean matches(String input) {
                        if (input != null && text != null) {
                            return input.endsWith(text);
                        }
                        else {
                            return false;
                        }
                    }
                };
                break;
            }
            case StartsWith: {
                matcher = new StringMatcher() {
                    public boolean matches(String input) {
                        if (input != null && text != null) {
                            return input.startsWith(text);
                        }
                        else {
                            return false;
                        }
                    }
                };
                break;
            }
            case Wildcard: {
                matcher = new WildcardOrRegexMatcher(text, false);
                break;
            }
            case Regex: {
                matcher = new WildcardOrRegexMatcher(text, true);
                break;
            }
            default: {
                throw new IllegalArgumentException(StringUtils.format("Unknown type '{}'", type));
            }
        }

        return matcher;
    }

}
