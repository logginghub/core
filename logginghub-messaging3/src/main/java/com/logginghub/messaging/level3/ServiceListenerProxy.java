package com.logginghub.messaging.level3;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.directives.MethodInvocationRequestMessage;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.utils.logging.Logger;

public class ServiceListenerProxy implements InvocationHandler {

    private static final Logger logger = Logger.getLoggerFor(ServiceListenerProxy.class);
    private Level1MessageSender sender;
    private String destinationObjectID;
    private String destinationChannel;

    public ServiceListenerProxy(Level1MessageSender sender, String destinationChannel, String destinationObjectID) {
        this.sender = sender;
        this.destinationChannel = destinationChannel;
        this.destinationObjectID = destinationObjectID;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.debug("Service proxy listener invoked for method '{}'", method.getName());

        MethodInvocationRequestMessage methodInvocationRequestMessage = new MethodInvocationRequestMessage();
        methodInvocationRequestMessage.setDestinationObjectID(destinationObjectID);
        methodInvocationRequestMessage.setOperation(method.getName());
        methodInvocationRequestMessage.setParameterTypes(method.getParameterTypes());
        methodInvocationRequestMessage.setParameters(args);

        MessageWrapper wrapper = new MessageWrapper(destinationChannel, destinationChannel, methodInvocationRequestMessage);
        sender.send(wrapper);
        return null;
    }

}
