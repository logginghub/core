package com.logginghub.messaging2.api;


public interface MessageListener {
    void onNewMessage(Message message);
}
