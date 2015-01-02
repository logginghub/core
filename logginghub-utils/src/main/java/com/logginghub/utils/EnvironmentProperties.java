package com.logginghub.utils;

public class EnvironmentProperties {

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    
    public static boolean getBoolean(String key, boolean defaultValue) {
        String result = getString(key);
        
        boolean booleanResult;
        if(result == null) {
            booleanResult = defaultValue;
        }else{
            booleanResult = Boolean.parseBoolean(result);
        }
        
        return booleanResult;
    }
    
    public static int getInteger(String key) {
        return getInteger(key, -1);
    }
    
    public static int getInteger(String key, int defaultValue) {
        String result = getString(key);
        
        int integerResult;
        if(result == null) {
            integerResult = defaultValue;
        }else{
            integerResult = Integer.parseInt(result);
        }
        
        return integerResult;
    }
    
    public static String getString(String key) {
        return getString(key, null);
    }
    
    public static String getString(String key, String defaultValue) {
        String result = System.getProperty(key);
        
        if(result == null) {
            result = System.getenv(key);
            
            if(result == null) {
                result = defaultValue;
            }
        }
        
        return result;
    }
}
