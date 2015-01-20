package com.logginghub.messaging.netty;

public interface Level2MessageSender extends Level1MessageSender {
    void send(String deliverToChannel, String replyToChannel, int responseID, Object payload);
}
