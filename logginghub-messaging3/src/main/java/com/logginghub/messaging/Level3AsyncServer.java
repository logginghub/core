package com.logginghub.messaging;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.level3.ObjectController;
import com.logginghub.messaging.level3.ObjectControllerMessageBinder;
import com.logginghub.messaging.level3.ServiceRepository;
import com.logginghub.messaging.netty.Level2Messaging;
import com.logginghub.messaging.netty.RequestMessageContext;
import com.logginghub.messaging.netty.ServerHandler;
import com.logginghub.messaging.netty.WrappedMessageListener;
import com.logginghub.utils.logging.Logger;

public class Level3AsyncServer extends Level2AsyncServer {

    private static final Logger logger = Logger.getLoggerFor(Level3AsyncServer.class);
    private ServiceRepository serviceRepository = new ServiceRepository();

    private ObjectController objectController = new ObjectController(serviceRepository);
    private ObjectControllerMessageBinder messageBinder;

    public Level3AsyncServer(String string) {
        super(string);

        init();
    }

    private void init() {
        messageBinder = new ObjectControllerMessageBinder(objectController, serviceRepository);
    }

    public Level3AsyncServer(int port, String string) {
        super(port, string);
        init();
    }

    public Level3AsyncServer() {
        super();
        init();
    }

    public <T> void register(String string, Class<T> serviceInterface, T serviceProvider) {
        serviceRepository.registerService(string, serviceInterface, serviceProvider);
    }
   
    @Override public <T> void onNewMessage(Object message, ServerHandler receivedFrom) {
        // Do the level 2 stuff
        super.onNewMessage(message, receivedFrom);

        // Our bit is just checking to see if this is a service request intended for the server
        if (isWrapped(message)) {
            MessageWrapper messageWrapper = (MessageWrapper) message;
            Object payload = messageWrapper.getPayload();

            if (isWrappedMessageIntededForUs(messageWrapper)) {
                RequestMessageContext context = new RequestMessageContext(messageWrapper, this, receivedFrom);
                messageBinder.onNewMessage(messageWrapper, payload, context);
            }
        }
    }

   
}
