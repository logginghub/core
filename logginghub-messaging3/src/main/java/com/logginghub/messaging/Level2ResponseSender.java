package com.logginghub.messaging;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.netty.ServerHandler;

public interface Level2ResponseSender {
    void sendResponse(MessageWrapper originalMessage, Object responseMessage, ServerHandler sender);
}
