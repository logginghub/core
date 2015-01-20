package com.logginghub.messaging.level3;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

import com.logginghub.messaging.Level3AsyncClient;
import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.directives.MethodInvocationAttachListenerRequestMessage;
import com.logginghub.messaging.directives.MethodInvocationAttachListenerResponseMessage;
import com.logginghub.messaging.directives.MethodInvocationDettachListenerRequestMessage;
import com.logginghub.messaging.directives.MethodInvocationDettachListenerResponseMessage;
import com.logginghub.messaging.directives.MethodInvocationRequestMessage;
import com.logginghub.messaging.directives.MethodInvocationResponseMessage;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.messaging.netty.MessageListener;
import com.logginghub.utils.logging.Logger;

public class AsyncChannelServiceProxy implements InvocationHandler {

    // TODO : the only difference here is the sendRequest bits - extract that out into a sender interface and get rid of this class
    
    private Class<?> serviceClass;
    private Level3AsyncClient client;
    private String serviceName;

    private static final Logger logger = Logger.getLoggerFor(AsyncChannelServiceProxy.class);

    private ServiceRepository repo = new ServiceRepository();
    private ObjectController objectController = new ObjectController(repo);
    private String channel;

    public AsyncChannelServiceProxy(String channel, Class<?> serviceClass, String serviceName, Level3AsyncClient level3AsyncClient) {
        this.channel = channel;
        this.serviceClass = serviceClass;
        this.serviceName = serviceName;
        this.client = level3AsyncClient;

    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.trace("Service '{}' method invoked : method '{}' with args {}", serviceName, method.getName(), args);

        if (ServiceProxyHelper.isLikelyListenerMethod(method, args)) {
            if (method.getName().startsWith("add")) {
                String responseChannel = UUID.randomUUID().toString();
                client.subscribe(responseChannel);

                MethodInvocationAttachListenerRequestMessage request = new MethodInvocationAttachListenerRequestMessage();
                request.setTargetObjectID(serviceName);
                request.setMethodName(method.getName());
                request.setResponseChannel(responseChannel);

                repo.registerService(responseChannel, args[0].getClass(), args[0]);

                client.addMessageListener(responseChannel, new MessageListener() {
                    public void onNewMessage(Object message, Level1MessageSender sender) {
                        if (message instanceof MessageWrapper) {
                            MessageWrapper messageWrapper = (MessageWrapper) message;
                            message = messageWrapper.getPayload();
                        }

                        if (message instanceof MethodInvocationRequestMessage) {
                            MethodInvocationRequestMessage request = (MethodInvocationRequestMessage) message;
                            objectController.handle(request, sender);
                        }
                    }
                });

                // The must block so we can do this asynchronously
                MethodInvocationAttachListenerResponseMessage response = (MethodInvocationAttachListenerResponseMessage) client.sendRequest(channel, request).awaitResponse();

                // TODO : handle response?
                // TODO : remove message listener on remove listener
            }
            else {
                String objectID = repo.getNameForObject(args[0]);

                MethodInvocationDettachListenerRequestMessage request = new MethodInvocationDettachListenerRequestMessage();
                request.setTargetObjectID(serviceName);
                request.setListenerObjectID(objectID);
                request.setMethodName(method.getName());
                
                MethodInvocationDettachListenerResponseMessage response = (MethodInvocationDettachListenerResponseMessage) client.sendRequest(channel, request).awaitResponse();

                // TODO : handle response?
                // TODO : remove message listener on remove listener
            }

            return null;
        }
        else {

            MethodInvocationRequestMessage request = new MethodInvocationRequestMessage();
            request.setDestinationObjectID(serviceName);
            request.setOperation(method.getName());
            request.setParameters(args);
            request.setParameterTypes(method.getParameterTypes());

            logger.trace("Sending invocation request message {}...", request);
            MethodInvocationResponseMessage response = (MethodInvocationResponseMessage) client.sendRequest(channel, request).awaitResponse();
            logger.trace("... response received : {}", response);

            Object returnValue;
            if (response.isSuccess()) {
                returnValue = response.getReturnValue();
                logger.trace("Invocation was succesful, returning result '{}'", returnValue);
            }
            else {
                logger.debug("Invocation was succesful, but the method threw an exception '{}'", response.getThrowableMessage());
                throw new RuntimeException(response.getThrowableMessage());
            }

            return returnValue;
        }

    }
}
