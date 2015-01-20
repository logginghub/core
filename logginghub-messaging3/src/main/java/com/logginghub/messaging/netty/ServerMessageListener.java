package com.logginghub.messaging.netty;


public interface ServerMessageListener {

    <T>  void onNewMessage(Object message, ServerHandler receivedFrom);

}
