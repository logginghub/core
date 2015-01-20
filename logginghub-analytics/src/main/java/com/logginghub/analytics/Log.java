package com.logginghub.analytics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Log {

    private final String name;

    public static int ALL = 0;
    public static int TRACE = 200;
    public static int DEBUG = 300;
    public static int INFO = 500;
    public static int WARNING = 600;
    public static int FATAL = 900;
    public static int OFF = 1000;

    private static int globalLevel = OFF;

    private static List<Log> logs = new ArrayList<Log>();

    private String[] levels = new String[] { "off", "", "trace", "debug", "", "info", "warning", "", "", "fatal", "all" };

    private int level = OFF;

    private final String outputName;

    public Log(String name, String outputName) {
        this.name = name;
        this.outputName = outputName;
    }

    public static Log create(Object instance) {
        Log log =  new Log(instance.getClass().getName(), instance.getClass().getSimpleName());
        log.setLevel(globalLevel);
        synchronized (logs) {
            logs.add(log);    
        }
        
        return log;
    }

    public void setLevel(int level) {
        this.level = level; 
    }

    

    private void logInternal(int level, String format, Object[] objects) {
        System.out.println(String.format("%20s | %10s | %20s | %s", new Date(), formatLevel(level), outputName, String.format(format, objects)));
    }

    private String formatLevel(int level) {
        return levels[level / 100];
    }

    public static void setGlobalLevel(int level) {
        globalLevel = level;
        
        for (Log log : logs) {
            log.setLevel(level);
        }
    }
    
    public void debug(String format, Object... objects) {
        if (level <= DEBUG) {
            logInternal(DEBUG, format, objects);
        }
    }

    public void info(String format, Object... objects) {
        if (level <= INFO) {
            logInternal(INFO, format, objects);
        }
    }
    
    public void trace(String format, Object... objects) {
        if (level <= TRACE) {
            logInternal(TRACE, format, objects);
        }
    }

}
