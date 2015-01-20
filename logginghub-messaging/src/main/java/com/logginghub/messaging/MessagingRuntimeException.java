package com.logginghub.messaging;

public class MessagingRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public MessagingRuntimeException()
    {
        super();
    }

    public MessagingRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public MessagingRuntimeException(String message)
    {
        super(message);
    }

    public MessagingRuntimeException(Throwable cause)
    {
        super(cause);
    }
}
