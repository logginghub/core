package com.logginghub.messaging;

import com.logginghub.messaging.MessagingClient.Status;
import com.logginghub.utils.Bucket;

public class MessageBucket extends Bucket<Message> implements
                MessagingClientClientListener
{

    public void onNewMessage(Message message)
    {
        add(message);
    }

    public void onStatusChanged(Status oldStatus, Status newStatus)
    {

    }

}
