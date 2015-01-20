package com.logginghub.messaging.level3;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.directives.MethodInvocationAttachListenerRequestMessage;
import com.logginghub.messaging.directives.MethodInvocationDettachListenerRequestMessage;
import com.logginghub.messaging.directives.MethodInvocationRequestMessage;
import com.logginghub.messaging.directives.ServiceRequestMessage;
import com.logginghub.messaging.directives.ServiceResponseMessage;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.messaging.netty.MessageListener;
import com.logginghub.messaging.netty.RequestMessageContext;
import com.logginghub.messaging.netty.ServerHandler;
import com.logginghub.messaging.netty.ServerMessageListener;
import com.logginghub.utils.logging.Logger;

public class ObjectControllerMessageBinder implements MessageListener, ServerMessageListener {

    private ObjectController objectController;
    private ServiceRepository serviceRepository;

    private static final Logger logger = Logger.getLoggerFor(ObjectControllerMessageBinder.class);

    public ObjectControllerMessageBinder(ObjectController objectController, ServiceRepository serviceRepository) {
        this.objectController = objectController;
        this.serviceRepository = serviceRepository;
    }

    public <T> void onNewMessage(Object message, ServerHandler receivedFrom) {
        processMessage(null, message, receivedFrom);
    }
    
    public <T> void onNewMessage(MessageWrapper original, Object payload, RequestMessageContext context) {
        processMessage(original, payload, context);
    }

    public <T> void onNewMessage(Object message, Level1MessageSender sender) {
        processMessage(null, message, sender);
    }

    private void processMessage(MessageWrapper wrapper, Object message, Level1MessageSender sender) {
        logger.debug("Processing message '{}'", message);

        if (message instanceof ServiceRequestMessage) {
            ServiceRequestMessage serviceRequestMessage = (ServiceRequestMessage) message;
            processServiceRequest(serviceRequestMessage, sender);
        }
        else if (message instanceof MethodInvocationRequestMessage) {
            MethodInvocationRequestMessage methodInvocationRequestMessage = (MethodInvocationRequestMessage) message;
            processMethodInvocationRequest(methodInvocationRequestMessage, sender);
        }
        else if (message instanceof MethodInvocationAttachListenerRequestMessage) {
            MethodInvocationAttachListenerRequestMessage methodInvocationAttachListenerRequestMessage = (MethodInvocationAttachListenerRequestMessage) message;
            processMethodInvocationAttachListenerRequest(methodInvocationAttachListenerRequestMessage, sender);
        }
        else if (message instanceof MethodInvocationDettachListenerRequestMessage) {
            MethodInvocationDettachListenerRequestMessage request = (MethodInvocationDettachListenerRequestMessage) message;
            processMethodInvocationDettachListenerRequest(request, sender);
        }
    }

    protected void processServiceRequest(ServiceRequestMessage serviceRequestMessage, Level1MessageSender sender) {
        logger.debug("Processing service request '{}' from '{}'", serviceRequestMessage, sender);
        String serviceName = serviceRequestMessage.getServiceName();
        Service service = serviceRepository.getService(serviceName);

        ServiceResponseMessage serviceResponseMessage;
        if (service != null) {
            serviceResponseMessage = new ServiceResponseMessage(serviceName, service.getServiceInterface().getName());
        }
        else {
            serviceResponseMessage = new ServiceResponseMessage(serviceName, null);
        }
        sender.send(serviceResponseMessage);
    }

    protected void processMethodInvocationDettachListenerRequest(MethodInvocationDettachListenerRequestMessage request, final Level1MessageSender sender) {
        logger.trace("Method invocation deettach listener request received {}", request);
        objectController.handle(request, new Level1MessageSender() {
            public void send(String deliverToChannel, String replyToChannel, Object message) {
                if (deliverToChannel != null) {
                    sender.send(new MessageWrapper(deliverToChannel, replyToChannel, message));
                }
                else {
                    sender.send(message);
                }
            }

            public void send(Object message) {
                sender.send(message);
            }
        });
    }

    protected void processMethodInvocationAttachListenerRequest(MethodInvocationAttachListenerRequestMessage request, final Level1MessageSender sender) {
        logger.trace("Method invocation attach listener request received {}", request);
        objectController.handle(request, new Level1MessageSender() {
            public void send(String deliverToChannel, String replyToChannel, Object message) {
                if (deliverToChannel != null) {
                    sender.send(new MessageWrapper(deliverToChannel, replyToChannel, message));
                }
                else {
                    send(message);
                }
            }

            public void send(Object message) {
                sender.send(message);
            }
        });
    }

    protected void processMethodInvocationRequest(MethodInvocationRequestMessage methodInvocationRequestMessage, final Level1MessageSender sender) {
        logger.trace("Method invocation request received {}", methodInvocationRequestMessage);
        objectController.handle(methodInvocationRequestMessage, sender);
    }



}
