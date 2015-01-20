package com.logginghub.messaging;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.netty.Level2MessageSender;

/**
 * A decorator that provides a convient way for level 2 senders to pass message
 * wrapper details back on downstream message sends.
 * 
 * @author James
 * 
 */
public class Level2Sender implements Level2MessageSender {

    private Object originalMessage;
    private Level2AsyncClient client;

    public Level2Sender(Object originalMessage, Level2AsyncClient client) {
        this.originalMessage = originalMessage;
        this.client = client;
    }

    public void send(Object message) {
        client.sendResponse(originalMessage, message);
    }

    public void send(String deliverToChannel, String replyToChannel, int responseID, Object payload) {
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setDeliverToChannel(deliverToChannel);
        wrapper.setReplyToChannel(replyToChannel);
        wrapper.setResponseID(responseID);
        wrapper.setPayload(payload);        
        client.send(wrapper);
    }

    public void send(String deliverToChannel, String replyToChannel, Object message) {}

}
