package com.logginghub.utils.sof;

import com.logginghub.utils.StringUtils;

public class SofPartialDecodeException extends SofException {
    private static final long serialVersionUID = 1L;

    public SofPartialDecodeException() {}


    public SofPartialDecodeException(Throwable t, String message, Object... objects) {
        super(StringUtils.format(message, objects), t);
    }

    public SofPartialDecodeException(String message, Object... objects) {
        super(StringUtils.format(message, objects));
    }

    public SofPartialDecodeException(Throwable cause) {
        super(cause);
    }

    public SofPartialDecodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
