package com.logginghub.messaging.netty;


public interface RequestContextServerMessageListener {
    <T>  void onNewMessage(Object message, ServerHandler receivedFrom, RequestContext requestContext);
}
