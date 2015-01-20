package com.logginghub.messaging2;

import com.logginghub.messaging2.api.Message;
import com.logginghub.messaging2.api.MessageListener;

public interface Hub {

    void connect(String destinationID, MessageListener client);

    void sendMessage(String destinationID, Message message);

    void disconnect(String destinationID);

}
