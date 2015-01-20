package com.logginghub.messaging2.proxy;

// shawjam - can't get this working against current kryonet as it uses ASM 4.0 - cglib is stuck on an older version :(
public class SenderMessageProxy {
//implements MethodInterceptor {

    /*
    private final MessagingInterface kryoClient;
    private final String destinationChannelID;
    private AtomicInteger nextRequestID = new AtomicInteger();
    private final String destinationObjectID;

    public SenderMessageProxy(MessagingInterface kryoClient, String destinationChannelID, String destinationObjectID) {
        this.kryoClient = kryoClient;
        this.destinationChannelID = destinationChannelID;
        this.destinationObjectID = destinationObjectID;
    }

    @SuppressWarnings("unchecked") public static <T> T newInstance(Class<T> clazz,
                                                                   MessagingInterface kryoClient,
                                                                   String destinationChannelID,
                                                                   String destinationObjectID) {
        try {
            SenderMessageProxy interceptor = new SenderMessageProxy(kryoClient, destinationChannelID, destinationObjectID);
            Enhancer e = new Enhancer();
            e.setSuperclass(clazz);
            e.setCallback(interceptor);
            T bean = (T) e.create();
            return bean;
        }
        catch (Throwable e) {
            throw new RuntimeException("Failed to create messaging proxy", e);
        }
    }

    public Object intercept(Object instance, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {

        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {

            Class<?> parameterClass = parameterTypes[i];
            if (parameterClass.isAnnotationPresent(RemoteListener.class)) {

                String objectID = UUID.randomUUID().toString();
                ReceiverMessageProxyConnector.bind(arguments[i], kryoClient, objectID);

                CallbackPlaceholder callbackObject = new CallbackPlaceholder(objectID);
                arguments[i] = callbackObject;
            }
        }

        MethodInvocationRequestMessage genericMessage = new MethodInvocationRequestMessage();
        genericMessage.setDestination(destinationChannelID);
        genericMessage.setDestinationObjectID(destinationObjectID);
        genericMessage.setOperation(method.getName());
        genericMessage.setParameters(arguments);
        genericMessage.setParameterTypes(method.getParameterTypes());
        genericMessage.setSource(kryoClient.getDestinationID());
        genericMessage.setRequestID(kryoClient.allocateRequestID());

        boolean sendingBlocking = true;
        if (method.getDeclaringClass().isAnnotationPresent(RemoteListener.class)) {
            if (method.getReturnType() == Void.TYPE) {
                sendingBlocking = false;
            }
        }

        Object payload;
        if (sendingBlocking) {
            MethodInvocationResponseMessage responseMessage = kryoClient.sendRequest(destinationChannelID,
                                                                                     genericMessage,
                                                                                     500000,
                                                                                     TimeUnit.SECONDS);

            if (responseMessage.getThrowableMessage() != null || responseMessage.getThrowableTrace() != null) {
                // TODO : build a real exception with a proper exception!
                System.out.println(responseMessage.getThrowableTrace());
                throw new RuntimeException(responseMessage.getThrowableMessage());
            }
            else {
                payload = responseMessage.getPayload();
            }

        }
        else {
            kryoClient.send(destinationChannelID, genericMessage);
            payload = null;
        }

        return payload;
    }
    */
}
