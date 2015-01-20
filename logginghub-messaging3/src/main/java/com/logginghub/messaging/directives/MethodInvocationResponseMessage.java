package com.logginghub.messaging.directives;

import com.logginghub.utils.StacktraceUtils;

public class MethodInvocationResponseMessage {
    
    private boolean success;
    private Object returnValue;
    private String throwableMessage;
    private String throwableTrace;

    public void setSuccess(boolean success) {
        this.success = success;
    }
    public boolean isSuccess() {
        return success;
    }
    
    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }
    
    public Object getReturnValue() {
        return returnValue;
    }
    
    public void setThrowableMessage(String throwableMessage) {
        this.throwableMessage = throwableMessage;
    }
    
    public void setThrowableTrace(String throwableTrace) {
        this.throwableTrace = throwableTrace;
    }
    
    public String getThrowableMessage() {
        return throwableMessage;
    }
    
    public String getThrowableTrace() {
        return throwableTrace;
    }

    public void setThrowable(Throwable throwable) {
        this.throwableMessage = throwable.getMessage();
        this.throwableTrace = StacktraceUtils.getStackTraceAsString(throwable);
    }
    @Override public String toString() {
        return "MethodInvocationResponseMessage [success=" + success + ", returnValue=" + returnValue + ", throwableMessage=" + throwableMessage + ", throwableTrace=" + throwableTrace + "]";
    }
    
    
}
