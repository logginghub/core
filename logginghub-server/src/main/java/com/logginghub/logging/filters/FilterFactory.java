package com.logginghub.logging.filters;

import com.logginghub.utils.filter.Filter;

/**
 * We've got a few different types of string contains filter, and we want to abstract away the exact instance from the point they are created.
 */
public class FilterFactory {

    private final boolean caseSensitive;
    private final boolean unicode;

    public FilterFactory(boolean caseSensitive, boolean unicode) {
        this.caseSensitive = caseSensitive;
        this.unicode = unicode;
    }

    public Filter createFilter(String value) {

        if(caseSensitive) {
            return new CaseSensitiveEventContainsFilter(value);
        }else if(unicode) {
            return new CaseInsensitiveEventContainsFilter(value);
        }else{
            return new CaseInsensitiveAsciiEventContainsFilter(value);
        }

    }

}
