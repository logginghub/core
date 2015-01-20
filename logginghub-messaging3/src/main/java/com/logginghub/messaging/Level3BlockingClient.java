package com.logginghub.messaging;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.directives.ServiceRequestMessage;
import com.logginghub.messaging.directives.ServiceResponseMessage;
import com.logginghub.messaging.level3.ChannelSpecificServiceProxy;
import com.logginghub.messaging.level3.ObjectController;
import com.logginghub.messaging.level3.ObjectControllerMessageBinder;
import com.logginghub.messaging.level3.ServiceRepository;
import com.logginghub.utils.logging.Logger;

public class Level3BlockingClient extends Level2BlockingClient {

    private final Logger logger = Logger.getNewLoggerFor(Level3BlockingClient.class);

    private ServiceRepository serviceRepository = new ServiceRepository();
    private ObjectController objectController;

    private String homeChannel;

    public Level3BlockingClient() {
        objectController = new ObjectController(serviceRepository);
        ObjectControllerMessageBinder binder = new ObjectControllerMessageBinder(objectController, serviceRepository);
        addMessageListener(binder);
    }

    @Override public void setName(String name) {
        super.setName(name);
        logger.setThreadContextOverride(name);
    }
    
    public <T> T getService(String serviceName) throws IllegalArgumentException, SecurityException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException,
                    NoSuchMethodException {
        return getServiceInternal(false, null, serviceName);
    }

    public void register(String string, Class<?> serviceInterface, Object serviceProvider) {
        serviceRepository.registerService(string, serviceInterface, serviceProvider);
    }

    public <T> T getService(final String channel, final String serviceName) throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException,
                    InvocationTargetException, NoSuchMethodException {

        return getServiceInternal(true, channel, serviceName);
    }

    private <T> T getServiceInternal(final boolean isWrapped, final String channel, final String serviceName) throws ClassNotFoundException, IllegalArgumentException, SecurityException,
                    InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        ServiceRequestMessage serviceRequestMessage = new ServiceRequestMessage(serviceName);
        Object message = wrap(isWrapped, channel, getHomeChannel(), serviceRequestMessage);

        logger.trace("Sending service request message {}...", message);
        send(message);

        logger.trace("... message sent, waiting for response...");
        ServiceResponseMessage response = receiveNext(getHomeChannel());

        logger.trace("... response received : {}", response);

        String serviceType = response.getServiceType();
        logger.trace("Creating service proxy for interface class name '{}'", serviceType);
        Class<?> serviceClass = Class.forName(serviceType);

        ChannelSpecificServiceProxy proxy = new ChannelSpecificServiceProxy(serviceClass, serviceName, channel, this);

        Class<?> proxyClass = Proxy.getProxyClass(Level3BlockingClient.class.getClassLoader(), new Class[] { serviceClass });
        @SuppressWarnings("unchecked") T t = (T) proxyClass.getConstructor(new Class[] { InvocationHandler.class }).newInstance(new Object[] { proxy });
        return t;
    }

    public static Object wrap(final boolean isWrapped, final String deliverToChannel, String replyToChannel, Object baseMessage) {
        Object message;
        if (isWrapped) {
            MessageWrapper wrapper = new MessageWrapper(deliverToChannel, replyToChannel, baseMessage);
            message = wrapper;
        }
        else {
            message = baseMessage;
        }
        return message;
    }

    public String getHomeChannel() {
        return homeChannel;
    }
    
    public void setHomeChannel(String homeChannel) {
        subscribe(homeChannel);
        this.homeChannel = homeChannel;
    }

}
