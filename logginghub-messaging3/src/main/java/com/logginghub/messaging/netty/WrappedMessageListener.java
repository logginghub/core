package com.logginghub.messaging.netty;

import com.logginghub.messaging.directives.MessageWrapper;

public interface WrappedMessageListener {
    <T>  void onNewMessage(MessageWrapper message, RequestContext requestContext, Level1MessageSender sender);
}
