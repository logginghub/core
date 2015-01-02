package com.logginghub.utils;

public class FormattedRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FormattedRuntimeException() {}

    public static FormattedRuntimeException build(String message, Object... objects) {
        FormattedRuntimeException e;
        
        Object last = objects[objects.length-1];
        if(last instanceof Throwable) {
            Throwable t = (Throwable)last;
            
            Object[] rest = new Object[objects.length - 1];
            System.arraycopy(objects, 0, rest, 0, rest.length);
            
            e  = new FormattedRuntimeException(StringUtils.format(message, rest), t);
        }else{
            e  = new FormattedRuntimeException(StringUtils.format(message, objects));
        }
        
        return e;
    }
    
    public FormattedRuntimeException(Throwable t, String message, Object... objects) {
        super(StringUtils.format(message, objects), t);
    }
    
    public FormattedRuntimeException(String message, Object... objects) {
        super(StringUtils.format(message, objects));
    }

    public FormattedRuntimeException(Throwable cause) {
        super(cause);
    }

    public FormattedRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
