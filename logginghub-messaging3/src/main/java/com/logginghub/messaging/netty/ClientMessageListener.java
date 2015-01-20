package com.logginghub.messaging.netty;

import org.jboss.netty.channel.MessageEvent;

public interface ClientMessageListener {

    void onMessageReceived(MessageEvent e);

}
