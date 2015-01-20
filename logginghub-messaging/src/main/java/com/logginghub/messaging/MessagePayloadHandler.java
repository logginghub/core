package com.logginghub.messaging;

public interface MessagePayloadHandler<PayloadType>
{
    public void handle(final PayloadType payload, final Message message, MessagingServerSocketHandler source);
}
