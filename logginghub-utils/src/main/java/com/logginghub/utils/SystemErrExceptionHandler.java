package com.logginghub.utils;

public class SystemErrExceptionHandler implements ExceptionHandler
{
    public void handleException(String action, Throwable t) {
        System.err.println(action);
        t.printStackTrace(System.err);
    }
}
