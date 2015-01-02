package com.logginghub.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

public class WildCardFileFilter implements FileFilter {
    private Set<String> matchingFilters = new HashSet<String>();
    private WildcardMatcher matcher = new WildcardMatcher();

    public WildCardFileFilter(String... wildCardPatters) {
        for (String string : wildCardPatters) {
            matchingFilters.add(string);
        }
    }

    public boolean accept(File file) {
        String absolutePath = file.getAbsolutePath();

        for (String filter : matchingFilters) {
            matcher.setValue(filter);
            if (matcher.matches(absolutePath)) {
                return true;
            }
        }

        return false;
    }

}
