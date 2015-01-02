package com.logginghub.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class RegexFileFilter implements FileFilter {

    private Pattern pattern;

    public RegexFileFilter(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    @Override public boolean accept(File pathname) {
        String absolutePath = pathname.getAbsolutePath();
        return pattern.matcher(absolutePath).matches();
    }

}
