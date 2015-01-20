package com.logginghub.logging.interfaces;


public interface QueueAwareLoggingMessageSender extends LoggingMessageSender {

    boolean isSendQueueEmpty();
         
}
