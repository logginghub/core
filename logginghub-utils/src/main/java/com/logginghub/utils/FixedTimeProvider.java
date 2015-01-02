package com.logginghub.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class FixedTimeProvider implements TimeProvider {
    private long time;

    public FixedTimeProvider(long time) {
        super();
        this.time = time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public String formatUTC() {
        Date date = new Date(time);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String format = df.format(date);
        return format;
    }

    public FixedTimeProvider setTime(String string) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date parse = df.parse(string);
            setTime(parse.getTime());
        }
        catch (ParseException e) {
            throw new RuntimeException(String.format("Bad date format '%s'", string), e);
        }
        return this;
    }

    public void setTimeSeconds(int i) {
        setTime(i * 1000);
    }

    public void increment(long milliseconds) {
        time += milliseconds;
    }
}
