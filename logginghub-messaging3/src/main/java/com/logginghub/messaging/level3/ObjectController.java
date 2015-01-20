package com.logginghub.messaging.level3;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.logginghub.messaging.Level3BlockingClient;
import com.logginghub.messaging.directives.MethodInvocationAttachListenerRequestMessage;
import com.logginghub.messaging.directives.MethodInvocationAttachListenerResponseMessage;
import com.logginghub.messaging.directives.MethodInvocationDettachListenerRequestMessage;
import com.logginghub.messaging.directives.MethodInvocationDettachListenerResponseMessage;
import com.logginghub.messaging.directives.MethodInvocationRequestMessage;
import com.logginghub.messaging.directives.MethodInvocationResponseMessage;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.logging.Logger;

public class ObjectController {

    private Map<String, Object> listenerProxyInstancesByObjectID = new HashMap<String, Object>();
    private static final Logger logger = Logger.getLoggerFor(ObjectController.class);

    private ServiceRepository serviceRepository;

    public ObjectController(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public void handle(MethodInvocationRequestMessage methodInvocationRequestMessage, Level1MessageSender messageSender) {
        logger.trace("Method invocation request received {}", methodInvocationRequestMessage);

        String serviceName = methodInvocationRequestMessage.getTargetObjectID();
        Service service = serviceRepository.getService(serviceName);

        MethodInvocationResponseMessage response = new MethodInvocationResponseMessage();
        if (service != null) {

            try {
                Object result = ReflectionUtils.invoke(service.getProviderInstance(),
                                                       methodInvocationRequestMessage.getOperation(),
                                                       methodInvocationRequestMessage.getParameterTypesAsClasses(),
                                                       methodInvocationRequestMessage.getParameters());
                logger.debug("Invoked method '{}' on object '{}' with params '{}'",
                             methodInvocationRequestMessage.getOperation(),
                             service.getProviderInstance(),
                             methodInvocationRequestMessage.getParameters());
                response.setSuccess(true);
                response.setReturnValue(result);
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            response.setSuccess(false);
            response.setThrowableMessage("Service not found exception");
        }

        messageSender.send(response);
    }

    public void handle(MethodInvocationAttachListenerRequestMessage request, Level1MessageSender messageSender) {

        logger.trace("Method invocation attach listener request received {}", request);

        String serviceName = request.getTargetObjectID();
        Service service = serviceRepository.getService(serviceName);
        if (service != null) {

            ServiceListenerProxy proxy = new ServiceListenerProxy(messageSender, request.getResponseChannel(), request.getResponseChannel());

            Method method = ReflectionUtils.findFirstMethod(service.getServiceInterface(), request.getMethodName());

            Class<?> class1 = method.getParameterTypes()[0];
            Class<?> proxyClass = Proxy.getProxyClass(Level3BlockingClient.class.getClassLoader(), new Class[] { class1 });
            Object proxyInstance;
            try {
                proxyInstance = proxyClass.getConstructor(new Class[] { InvocationHandler.class }).newInstance(new Object[] { proxy });
                method.invoke(service.getProviderInstance(), proxyInstance);
                listenerProxyInstancesByObjectID.put(request.getResponseChannel(), proxyInstance);
            }
            catch (Exception e) {
                logger.warning(e);
                throw new RuntimeException(e);
            }

            MethodInvocationAttachListenerResponseMessage response = new MethodInvocationAttachListenerResponseMessage();
            messageSender.send(response);
        }
    }

    public void handle(MethodInvocationDettachListenerRequestMessage request, Level1MessageSender messageSender) {
        logger.trace("Method invocation dettach listener request received {}", request);

        String serviceName = request.getTargetObjectID();
        Service service = serviceRepository.getService(serviceName);
        if (service != null) {

            Method method = ReflectionUtils.findFirstMethod(service.getServiceInterface(), request.getMethodName());

            Object proxyInstance = listenerProxyInstancesByObjectID.get(request.getListenerObjectID());

            try {
                method.invoke(service.getProviderInstance(), proxyInstance);
            }
            catch (Exception e) {
                logger.warning(e);
                throw new RuntimeException(e);
            }

            MethodInvocationDettachListenerResponseMessage response = new MethodInvocationDettachListenerResponseMessage();
            messageSender.send(response);
        }
    }
}
