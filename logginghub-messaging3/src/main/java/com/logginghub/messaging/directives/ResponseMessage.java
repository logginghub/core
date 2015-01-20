package com.logginghub.messaging.directives;

public class ResponseMessage {
    private boolean success;

    public ResponseMessage(boolean success) {
        this.success = success;
    }

    public ResponseMessage() {}

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override public String toString() {
        return "ResponseMessage [success=" + success + "]";
    }

    
    
}
