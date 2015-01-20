package com.logginghub.messaging;

public interface SocketServerListener
{
    void onAccepted(MessagingServerSocketHandler handler);
    void onMessage(Message message, MessagingServerSocketHandler source);
}
