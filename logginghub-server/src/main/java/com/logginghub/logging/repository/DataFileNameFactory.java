package com.logginghub.logging.repository;

import java.text.DateFormat;
import java.util.Date;

import com.logginghub.utils.FileDateFormat;

/**
 * Ensures the various tools use the same format for data files.
 * 
 * @author James
 * 
 */
public class DataFileNameFactory {

    private static final String LOGDATA = ".logdata";
    private static final String LOGDATA_WRITING = ".logdata.writing";
    
    private static ThreadLocal<DateFormat> dateFormats = new ThreadLocal<DateFormat>() {
        protected DateFormat initialValue() {
            return new FileDateFormat();
        };
    };

    public static String getWritingTemporaryFilename(String prefix, long start) {
        String filename = String.format("%s%s%s", prefix, dateFormats.get().format(new Date(start)), LOGDATA_WRITING);
        return filename;
    }
    
    public static String getFinishedFilename(String prefix, long start) {
        String filename = String.format("%s%s%s", prefix, dateFormats.get().format(new Date(start)), LOGDATA);
        return filename;
    }

    public static String extractDatePart(String prefix, String name) {
        
        String datePart;
        if(name.endsWith(LOGDATA)){
            datePart = name.substring(prefix.length(), name.length() - LOGDATA.length());
        }else if(name.endsWith(LOGDATA_WRITING)){
            datePart = name.substring(prefix.length(), name.length() - LOGDATA_WRITING.length());
        }else{
            throw new RuntimeException(String.format("Couldn't strip the date part from name '%s', it didn't look like a properly formatted name", name));
        }
        
        return datePart;
         
    }

}
