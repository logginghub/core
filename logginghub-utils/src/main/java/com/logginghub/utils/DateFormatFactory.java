package com.logginghub.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class DateFormatFactory {

    public static TimeZone utc = TimeZone.getTimeZone("UTC");
    public static TimeZone local = TimeZone.getDefault();
    
    public static List<DateFormat> getAllFormats(TimeZone timezone) {
        List<DateFormat> list = new ArrayList<DateFormat>();
        list.add(getTimeThenDate(timezone));
        list.add(getDateThenTime(timezone));
        list.add(getFileSafeOrdered(timezone));
        list.add(getFileSafeOrderedWithMillis(timezone));
        list.add(getDateThenTimeWithMillis(timezone));
        list.add(getJustDate(timezone));
        list.add(getJustTime(timezone));
        list.add(getJustTimeNoSeconds(timezone));
        list.add(getTimeWithMillis(timezone));
        list.add(getDateWithShortStringMonth(timezone));
        return list;
    }

    private static DateFormat getDateWithShortStringMonth(TimeZone timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }

    public static DateFormat getTimeThenDate(TimeZone timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }

    public static DateFormat getDateThenTime(TimeZone timezone) {
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }
    
    public static SimpleDateFormat getUSDateThenTime(TimeZone timezone) {
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }

    public static DateFormat getDateThenTimeWithMillis(TimeZone timezone) {
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }

    public static DateFormat getJustTime(TimeZone timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");  
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }
    
    public static DateFormat getJustTimeNoSeconds(TimeZone timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat; 
    }

    public static DateFormat getJustTimeUTC() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        simpleDateFormat.setTimeZone(utc);
        return simpleDateFormat;
    }

    public static DateFormat getJustDate(TimeZone timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }

    public static DateFormat getFileSafeOrderedWithMillis(TimeZone timezone) {        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }
    
    public static DateFormat getNiceFileSafeOrderedWithMillis(TimeZone timezone) {        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss.SSS");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }


    public static DateFormat getFileSafeOrdered(TimeZone timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }

    public static DateFormat getTimeWithMillis(TimeZone timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }

    public static DateFormat getTimeWithoutMillis(TimeZone timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }

    public static DateFormat getDailyFolderSplit(TimeZone timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }
    
    public static DateFormat getHourlyFolderSplit(TimeZone timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/hh");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat;
    }


}
