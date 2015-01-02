package com.logginghub.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class FileDateFormat extends SimpleDateFormat {
    private static final long serialVersionUID = 1L;

    public FileDateFormat() {
        super("yyyyMMdd.HHmmss");
        setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static long parseHelper(String string) {
        try {
            return new FileDateFormat().parse(string).getTime();
        }
        catch (ParseException e) {
            throw new RuntimeException(String.format("Failed to parse '%s'", string), e);
        }
    }
}
