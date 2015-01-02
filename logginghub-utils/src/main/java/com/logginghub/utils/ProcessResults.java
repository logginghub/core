package com.logginghub.utils;

import java.io.Serializable;

public class ProcessResults implements Serializable {
    private static final long serialVersionUID = 1L;

    private String stdOut;
    private String stdError;
    private int returnCode;

    public String getOutput() {
        return stdOut;
    }

    public void setOutput(String output) {
        stdOut = output;
    }

    public String getError() {
        return stdError;
    }

    public void setError(String error) {
        stdError = error;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }
}
