package com.logginghub.messaging.netty;

import com.logginghub.messaging.MessagingChannel;
import com.logginghub.messaging.directives.MessageWrapper;

/**
 * If you want to send a message in response, use this interface to insure the
 * context is maintained. Implementors should do their best to match up to/from
 * IDs and request/response IDs.
 * 
 * @author James
 * 
 */
public interface RequestContext {
    void reply(Object response);
    MessageWrapper getIncommingMessageWrapper();
}
