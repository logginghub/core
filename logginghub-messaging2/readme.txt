


I've got a bit confused about the tier involved in the design already. The 'public' api tier concepts are :

	* Destination ID - this is the string topic name to send the message to
	* Object payload - this is an arbitrary object to be sent to the destination. When the destination's 
					   message listener fires, this is the object that will be provided. But what if the 
					   receiver wants to know about the message that carried the payload?
	
	* MessageListener
	
		The MessageListener interface provider listeners with access to incoming Messages. Calling 
		getPayload will give access to the underlying object.
	
	* Source ID
	
		All messages carry a source ID, which is the topic name that can be used to send a message
		back to the sender.
	
	* Blocking request-response messages
		
		If you want to do blocking request response, you can send a payload object using the request 
		response API. This will generate a unique request ID and send a specific kind of message to the
		destination. The receiver needs to be able to send a response back which matches the request, so 
		they need some exposure to the request ID and source ID.
		
	* Subscriptions
	
		Clients can subscribe to arbitrary string channel IDs. They can request a response is sent back
		via that channel (very handy for sending back streaming stuff for example) in their own payload 
		messages.  You can subscribe using the global message listener, or attach a particular listener
		to a given topic ID.
	
	* Proxy interfaces
	
		To make request response easier, you can do RMI style calls by calling this on the consumer :
		
			myRemoteInterface = ClientMessageProxy.newInstance(ClassOrInterface.class, kryoClientInstance, destinationID);
			
		and this on the provider :
		
			ServerMessageProxyConnector.bind(myInstanceThatImplementsTheInterface, kryoClient);
	