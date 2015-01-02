package com.logginghub.utils;

@Deprecated
/**
 * @author James
 * @deprecated Shouldn't be used in released code!
 */
public class Debug {

    @Deprecated
    public static void out(String message, Object... params) {
        StringUtils.out("[debug] " + message, params);
    }
    
    @Deprecated
    public static void err(String message, Object... params) {
        StringUtils.out("[debug] " + message, params);
    }
    
    @Deprecated
    public static void outSameLine(String message, Object... params) {
        StringUtils.outSameLine("[debug] " + message, params);
    }

    public static void outWithStack(String string, Object... params) {
        out(string, params);
        Exception e = new Exception();
        e.fillInStackTrace();
        e.printStackTrace(System.out);
    }

}