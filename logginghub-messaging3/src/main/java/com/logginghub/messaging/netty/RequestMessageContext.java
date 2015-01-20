package com.logginghub.messaging.netty;

import com.logginghub.messaging.Level2ResponseSender;
import com.logginghub.messaging.directives.MessageWrapper;

public class RequestMessageContext implements RequestContext, Level1MessageSender {

    private MessageWrapper incommingMessageWrapper;
    private Level2ResponseSender server;
    private ServerHandler serverHandler;

    public RequestMessageContext(MessageWrapper incommingMessageWrapper, Level2ResponseSender server, ServerHandler serverHandler) {
        this.incommingMessageWrapper = incommingMessageWrapper;
        this.server = server;
        this.serverHandler = serverHandler;
    }

    public void reply(Object response) {
        server.sendResponse(incommingMessageWrapper, response, serverHandler);
    }

    public void send(Object message) {
        reply(message);
    }

    public MessageWrapper getIncommingMessageWrapper() {
        return incommingMessageWrapper;        
    }
}
