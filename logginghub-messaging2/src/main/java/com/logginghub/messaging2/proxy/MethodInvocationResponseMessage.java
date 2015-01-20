package com.logginghub.messaging2.proxy;

import com.logginghub.messaging2.messages.ResponseMessage;
import com.logginghub.utils.StacktraceUtils;


public class MethodInvocationResponseMessage extends ResponseMessage {
    
    private String throwableMessage;
    private String throwableTrace;

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
}
