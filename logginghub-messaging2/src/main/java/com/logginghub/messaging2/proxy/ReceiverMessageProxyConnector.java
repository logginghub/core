package com.logginghub.messaging2.proxy;

import java.lang.reflect.Method;

import com.logginghub.messaging2.api.Message;
import com.logginghub.messaging2.api.MessageListener;
import com.logginghub.messaging2.api.MessagingInterface;

public class ReceiverMessageProxyConnector implements MessageListener {

    private final Object instance;
    private final MessagingInterface kryoClient;
    private final String objectID;

    private ReceiverMessageProxyConnector(Object instance, MessagingInterface kryoClient, String objectID) {
        this.instance = instance;
        this.kryoClient = kryoClient;
        this.objectID = objectID;
    }

    public void attach() {
        kryoClient.addMessageListener(this);
    }

    public void detach() {
        kryoClient.removeMessageListener(this);
    }

    public static ReceiverMessageProxyConnector bind(Object instance, MessagingInterface kryoClient, String objectID) {
        ReceiverMessageProxyConnector connector = new ReceiverMessageProxyConnector(instance, kryoClient, objectID);
        connector.attach();
        return connector;
    }

    public void onNewMessage(Message message) {

        if (message instanceof MethodInvocationRequestMessage) {
            MethodInvocationRequestMessage request = (MethodInvocationRequestMessage) message;

            if (request.getTargetObjectID().equals(objectID)) {

                MethodInvocationResponseMessage response = new MethodInvocationResponseMessage();

                try {
                    Class<?>[] parameterTypesAsClasses = request.getParameterTypesAsClasses();

                    // Go through the parameter list and have a look to see if
                    // there
                    // are any CallbackPlaceholders in the list... if there are
                    // we
                    // need to wire proxies into the arguments list to send the
                    // method calls back the other way.
                    Object[] parameters = request.getParameters();
                    for (int i = 0; i < parameters.length; i++) {
                        Object param = parameters[i];

                        if (param instanceof CallbackPlaceholder) {

                            CallbackPlaceholder callbackPlaceholder = (CallbackPlaceholder) param;
                            Class<?> parameterClass = parameterTypesAsClasses[i];

//                            Object remoteInstance = SenderMessageProxy.newInstance(parameterClass,
//                                                                                   kryoClient,
//                                                                                   message.getSource(),
//                                                                                   callbackPlaceholder.getObjectID());
//                            parameters[i] = remoteInstance;
                        }
                    }

                    Method method = instance.getClass().getMethod(request.getOperation(), parameterTypesAsClasses);
                    method.setAccessible(true);
                    Object result = method.invoke(instance, request.getParameters());
                    response.setPayload(result);
                }
                catch (Exception e) {
                    response.setThrowable(e);
                }

                kryoClient.sendResponse(request.getSourceID(), request.getRequestID(), response);
            }
        }
    }
}
