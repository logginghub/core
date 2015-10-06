package com.logginghub.utils;

/**
 * Created by james on 02/10/15.
 */
public class TimeKey {
    public int year;
    public int month;
    public int day;
    public int hour;
    public int minute;
    public int second;
    public int millisecond;

    public static TimeKey from(int year, int month, int day, int hour, int minute) {
        TimeKey key = new TimeKey();
        key.year = year;
        key.month = month;
        key.day = day;
        key.hour = hour;
        key.minute = minute;
        key.second = 0;
        key.millisecond = 0;
        return key;
    }

    public static TimeKey from(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        TimeKey key = new TimeKey();
        key.year = year;
        key.month = month;
        key.day = day;
        key.hour = hour;
        key.minute = minute;
        key.second = second;
        key.millisecond = millisecond;
        return key;
    }

    public static TimeKey from(int year, int month, int day, int hour) {
        TimeKey key = new TimeKey();
        key.year = year;
        key.month = month;
        key.day = day;
        key.hour = hour;
        key.minute = 0;
        key.second = 0;
        key.millisecond = 0;
        return key;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TimeKey{");
        sb.append("year=").append(year);
        sb.append(", month=").append(month);
        sb.append(", day=").append(day);
        sb.append(", hour=").append(hour);
        sb.append(", minute=").append(minute);
        sb.append(", second=").append(second);
        sb.append(", millisecond=").append(millisecond);
        sb.append('}');
        return sb.toString();
    }
}
