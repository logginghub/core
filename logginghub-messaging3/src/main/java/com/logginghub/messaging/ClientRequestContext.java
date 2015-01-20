package com.logginghub.messaging;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.messaging.netty.RequestContext;

public class ClientRequestContext implements RequestContext {

    private MessageWrapper incommingMessageWrapper;
    private Level1MessageSender sender;

    public ClientRequestContext(MessageWrapper incommingMessageWrapper, Level1MessageSender sender) {
        this.incommingMessageWrapper = incommingMessageWrapper;
        this.sender = sender;
    }

    public void reply(Object response) {
        MessageWrapper outgoingMessageWrapper = new MessageWrapper(incommingMessageWrapper.getReplyToChannel(), incommingMessageWrapper.getDeliverToChannel(), response);
        
        if(incommingMessageWrapper.getReplyToLocalChannel() != null){
            outgoingMessageWrapper.setDeliverToLocalChannel(incommingMessageWrapper.getReplyToLocalChannel());
        }
        
        outgoingMessageWrapper.setResponseID(incommingMessageWrapper.getResponseID());
        sender.send(outgoingMessageWrapper);
    }

    public void send(Object message) {
        reply(message);
    }

    public MessageWrapper getIncommingMessageWrapper() {
        return incommingMessageWrapper;        
    }

}
