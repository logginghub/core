package com.logginghub.utils.sof;

import com.logginghub.utils.StringUtils;

public class SofException extends Exception {
    private static final long serialVersionUID = 1L;

    public SofException() {}


    public SofException(Throwable t, String message, Object... objects) {
        super(StringUtils.format(message, objects), t);
    }

    public SofException(String message, Object... objects) {
        super(StringUtils.format(message, objects));
    }

    public SofException(Throwable cause) {
        super(cause);
    }

    public SofException(String message, Throwable cause) {
        super(message, cause);
    }
}
