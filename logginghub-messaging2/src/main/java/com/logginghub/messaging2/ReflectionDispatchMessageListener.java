package com.logginghub.messaging2;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.logginghub.messaging2.api.Message;
import com.logginghub.messaging2.api.MessageListener;
import com.logginghub.messaging2.messages.BasicMessage;
import com.logginghub.messaging2.messages.RequestMessage;
import com.logginghub.messaging2.messages.ResponseMessage;
import com.logginghub.utils.NamedThreadFactory;
import com.logginghub.utils.logging.Logger;

public class ReflectionDispatchMessageListener implements MessageListener {

    
    private static final Logger logger = Logger.getLoggerFor(ReflectionDispatchMessageListener.class);
    private final Object targetInstance;
    private Class<? extends Object> instanceClass;
    private Map<Class<?>, Method> methodCache = new HashMap<Class<?>, Method>();
    private final MessageSender messageSender;
    private final String clientID;
    private ExecutorService executor = Executors.newCachedThreadPool(new NamedThreadFactory("ReflectionDispatchMessageListener-worker-"));

    // TODO : this shouldn't refer to the hub directly, as long as its something
    // capable of sending messages that should suffice.
    public ReflectionDispatchMessageListener(String clientID, MessageSender messageSender, Object targetInstance) {
        this.clientID = clientID;
        this.messageSender = messageSender;
        this.targetInstance = targetInstance;
        this.instanceClass = targetInstance.getClass();
    }

    public void stop() {
        executor.shutdownNow();
    }

    public void onNewMessage(String destinationID, final Message message) {
        executor.execute(new Runnable() {
            public void run() {
                handleMessage(message);
            }
        });
    }

    public void handleMessage(Message message) {
        Object payload = message.getPayload();
        Class<?> payloadClass = payload.getClass();

        Method method = methodCache.get(payloadClass);
        if (method == null) {
            Method[] methods = instanceClass.getMethods();
            for (Method potentialMethod : methods) {
                Class<?>[] parameterTypes = potentialMethod.getParameterTypes();
                if (parameterTypes.length > 0) {
                    if (parameterTypes[0].equals(payloadClass)) {
                        method = potentialMethod;
                        methodCache.put(payloadClass, method);
                        break;
                    }
                }
            }
        }

        if (method == null) {
            throw new RuntimeException("Couldn't find any method that could handle payload class " +
                                       payloadClass.getName() +
                                       " on instance " +
                                       targetInstance +
                                       " of class " +
                                       instanceClass.getName());
        }

        try {
            Object result = method.invoke(targetInstance, payload);

            if (message instanceof ResponseMessage) {
                // We can't send back a response to a response - so nothing more
                // to do. This will happen when using the
                // ReflectionMessageListener on both sides of the conversation.
            }
            else if (message instanceof RequestMessage) {
                // Remember we are low level now - we have to build the response
                // message ourselves.
                RequestMessage requestMessage = (RequestMessage) message;
                ResponseMessage responseMessage = new ResponseMessage(requestMessage.getRequestID(), result);
                responseMessage.setDestination(requestMessage.getSource());
                responseMessage.setSource(this.clientID);
                messageSender.sendMessage(responseMessage.getDestination(), responseMessage);
            }
        }
        catch (Exception e) {
            
            logger.warning(e, "Failed to handle message '{}'", message);
            
            if (message instanceof RequestMessage) {
                // The other end was expecting a message back
                if (message instanceof RequestMessage) {
                    // Remember we are low level now - we have to build the
                    // response message ourselves.
                    RequestMessage requestMessage = (RequestMessage) message;
                    ResponseMessage responseMessage = new ResponseMessage(requestMessage.getRequestID(), e);
                    messageSender.sendMessage(requestMessage.getSourceID(), responseMessage);
                }
                else {
                    // TODO : I doubt this will work, we need some sort of
                    // generic failure handler in the messaging framework
                    BasicMessage responseMessage = new BasicMessage(e, clientID, message.getSource());
                    messageSender.sendMessage(message.getSource(), responseMessage);
                }
            }
        }
    }

    public void onNewMessage(Message message) {
        onNewMessage(message.getDestination(), message);
    }

}
