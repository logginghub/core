package com.logginghub.messaging;

public interface MessagingServerSocketListener
{
    void onNewMessage(Message message, MessagingServerSocketHandler source);
    void onConnectionClosed(MessagingServerSocketHandler source);
}
