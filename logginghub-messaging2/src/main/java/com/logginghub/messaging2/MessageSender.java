package com.logginghub.messaging2;

import com.logginghub.messaging2.api.Message;

public interface MessageSender {

    void sendMessage(String destination, Message message);

}
