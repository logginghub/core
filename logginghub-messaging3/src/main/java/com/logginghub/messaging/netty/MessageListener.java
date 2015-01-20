package com.logginghub.messaging.netty;


public interface MessageListener {

    <T>  void onNewMessage(Object message, Level1MessageSender sender);

}
