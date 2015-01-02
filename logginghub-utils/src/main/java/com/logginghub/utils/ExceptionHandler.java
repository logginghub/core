package com.logginghub.utils;

public interface ExceptionHandler
{
    ExceptionHandler noop = new ExceptionHandler() {
        public void handleException(String action, Throwable t) {}
    };

    void handleException(String action, Throwable t);
}
