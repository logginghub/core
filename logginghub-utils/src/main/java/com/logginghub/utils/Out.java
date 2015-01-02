package com.logginghub.utils;


public class Out {

    public static void out(String message, Object... params) {
        StringUtils.out(message, params);
    }
    
    public static void err(String message, Object... params) {
        StringUtils.err(message, params);
    }
    
    public static void outSameLine(String message, Object... params) {
        StringUtils.outSameLine(message, params);
    }

    public static void out(char c) {
        StringUtils.out(c);
    }

    public static void out() {
        StringUtils.out();
    }
    
    public static void out(Object object) {
        StringUtils.out(object.toString());
    }

    public static void div() {
        out("----------------------------------------------------------------------------------");
    }

}