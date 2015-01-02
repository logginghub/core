package com.logginghub.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

public class FilenameContainsIgnoreFileFilter implements FileFilter {

    final Set<String> ignores = new HashSet<String>();
    
    public FilenameContainsIgnoreFileFilter() {}
    
    public FilenameContainsIgnoreFileFilter(Set<String> ignores) {
        this.ignores.addAll(ignores);
    }

    public void addIgnore(String ignorePartial) {
        ignores.add(ignorePartial);
    }

    public boolean accept(File file) {
        boolean accept = true;
        String absolutePath = file.getAbsolutePath();
        for (String string : ignores) {
            if (absolutePath.contains(string)) {
                accept = false;
                break;
            }
        }

        return accept;
    }
}