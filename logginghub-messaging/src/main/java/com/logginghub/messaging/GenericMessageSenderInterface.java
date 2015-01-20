package com.logginghub.messaging;

import java.io.Serializable;

import com.logginghub.messaging.Message;

/**
 * This interface should allow people to use the client and server messaging
 * interfaces inter-changably. It has a combination of methods that should be
 * supportable by both client and server messaging.
 * 
 * @author James
 */
public interface GenericMessageSenderInterface {

	void reply(Message message, Serializable response);

	int getClientID();

}
