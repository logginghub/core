package com.logginghub.utils;


public class MainUtils {

    public static int getIntArgument(String[] args, int index, int defaultValue) {
        int value;

        if (args.length > index) {
            value = Integer.parseInt(args[index]);
        }else{
            value = defaultValue;
        }
        
        return value;
    }

    public static String getStringArgument(String[] args, int index, String defaultValue) {
        String value;
        
        if (args.length > index) {
            value = args[index];
        }else{
            value = defaultValue;
        }
        
        return value;         
    }

    public static boolean getBooleanArgument(String[] args, int index, boolean defaultValue) {
        boolean value;

        if (args.length > index) {
            value = Boolean.parseBoolean(args[index]);
        }else{
            value = defaultValue;
        }
        
        return value;
    }

}
