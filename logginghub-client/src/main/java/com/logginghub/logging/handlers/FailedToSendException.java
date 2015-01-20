package com.logginghub.logging.handlers;

public class FailedToSendException extends RuntimeException
{
    public FailedToSendException(String string)
    {
        super(string);
    }
}
