package com.logginghub.messaging.level3;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

import com.logginghub.messaging.Level3BlockingClient;
import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.directives.MethodInvocationAttachListenerRequestMessage;
import com.logginghub.messaging.directives.MethodInvocationDettachListenerRequestMessage;
import com.logginghub.messaging.directives.MethodInvocationRequestMessage;
import com.logginghub.messaging.directives.MethodInvocationResponseMessage;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.messaging.netty.MessageListener;
import com.logginghub.utils.logging.Logger;

public class ChannelSpecificServiceProxy implements InvocationHandler {

    private static final Logger logger = Logger.getLoggerFor(ChannelSpecificServiceProxy.class);
    private Class<?> serviceClass;
    private String serviceName;
    private Level3BlockingClient client;
    private String channel;
    private boolean isWrapped;

    private ServiceRepository repo = new ServiceRepository();
    private ObjectController objectController = new ObjectController(repo);

    public ChannelSpecificServiceProxy(Class<?> serviceClass, String serviceName, String channel, Level3BlockingClient client) {
        this.serviceClass = serviceClass;
        this.serviceName = serviceName;
        this.channel = channel;
        this.isWrapped = channel != null;
        this.client = client;
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

                Object message = Level3BlockingClient.wrap(isWrapped, channel, client.getHomeChannel(), request);
                repo.registerService(responseChannel, args[0].getClass(), args[0]);

                client.addChannelMessageHandler(responseChannel, new MessageListener() {
                    public void onNewMessage(Object message, Level1MessageSender sender) {
                        if (message instanceof MessageWrapper) {
                            MessageWrapper messageWrapper = (MessageWrapper) message;
                            message = messageWrapper.getPayload();
                        }

                        if (message instanceof MethodInvocationRequestMessage) {
                            MethodInvocationRequestMessage request = (MethodInvocationRequestMessage) message;
                            objectController.handle(request, client);
                        }
                    }
                });

                client.send(message);
                client.receiveNext(client.getHomeChannel());
            }
            else {
                String objectID = repo.getNameForObject(args[0]);

                MethodInvocationDettachListenerRequestMessage request = new MethodInvocationDettachListenerRequestMessage();
                request.setTargetObjectID(serviceName);
                request.setListenerObjectID(objectID);
                request.setMethodName(method.getName());

                Object message = Level3BlockingClient.wrap(isWrapped, channel, client.getHomeChannel(), request);
                client.send(message);
                client.receiveNext(client.getHomeChannel());

            }

            return null;
        }
        else {

            MethodInvocationRequestMessage invocationRequestMessage = new MethodInvocationRequestMessage();
            invocationRequestMessage.setDestinationObjectID(serviceName);
            invocationRequestMessage.setOperation(method.getName());
            invocationRequestMessage.setParameters(args);
            invocationRequestMessage.setParameterTypes(method.getParameterTypes());

            Object message = Level3BlockingClient.wrap(isWrapped, channel, client.getHomeChannel(), invocationRequestMessage);
            logger.trace("Sending wrapped invocation request message {}...", message);
            client.send(message);

            logger.trace("... message sent, waiting for response...");
            MethodInvocationResponseMessage response = client.receiveNext(client.getHomeChannel());

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
