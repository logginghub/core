package com.logginghub.utils.sof;

import com.logginghub.utils.StringUtils;

public class SofRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SofRuntimeException() {}


    public SofRuntimeException(Throwable t, String message, Object... objects) {
        super(StringUtils.format(message, objects), t);
    }

    public SofRuntimeException(String message, Object... objects) {
        super(StringUtils.format(message, objects));
    }

    public SofRuntimeException(Throwable cause) {
        super(cause);
    }

    public SofRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
