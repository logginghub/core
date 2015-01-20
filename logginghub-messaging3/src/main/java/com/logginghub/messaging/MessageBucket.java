package com.logginghub.messaging;

import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.messaging.netty.MessageListener;
import com.logginghub.messaging.netty.ServerHandler;
import com.logginghub.messaging.netty.ServerMessageListener;
import com.logginghub.utils.Bucket;

public class MessageBucket extends Bucket<Object> implements MessageListener, ServerMessageListener{
    public void onNewMessage(Object message, Level1MessageSender sender) {
        add(message);
    }

    public <T> void onNewMessage(Object message, ServerHandler receivedFrom) {
        add(message);
    }

}
