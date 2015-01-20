package com.logginghub.messaging;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.directives.ServiceRequestMessage;
import com.logginghub.messaging.directives.ServiceResponseMessage;
import com.logginghub.messaging.level3.AsyncChannelServiceProxy;
import com.logginghub.messaging.level3.AsyncNoChannelServiceProxy;
import com.logginghub.messaging.level3.ObjectController;
import com.logginghub.messaging.level3.ObjectControllerMessageBinder;
import com.logginghub.messaging.level3.ServiceRepository;
import com.logginghub.utils.Timeout;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

public class Level3AsyncClient extends Level2AsyncClient {

    private final Logger logger = Logger.getNewLoggerFor(Level3AsyncClient.class);
    private ServiceRepository serviceRepository = new ServiceRepository();
    private ObjectController objectController;
    private String homeChannel;

    public Level3AsyncClient(String string) {
        init();
        setName(string);
    }

    public Level3AsyncClient() {
        super();
        init();
    }

    public void init() {
        objectController = new ObjectController(serviceRepository);
        ObjectControllerMessageBinder binder = new ObjectControllerMessageBinder(objectController, serviceRepository);
        addMessageListener(binder);
    }

    @Override public void setName(String name) {
        super.setName(name);
        logger.setThreadContextOverride(name);
    }

    public <T> void getService(final String serviceName, final ServiceListener<T> serviceListener) {
        getService(serviceName, serviceListener, Timeout.defaultTimeout);
    }
    
    public <T> void getService(final String serviceName, final ServiceListener<T> serviceListener, final Timeout timeout) {

        ServiceRequestMessage serviceRequestMessage = new ServiceRequestMessage(serviceName);
        sendRequest(serviceRequestMessage, new Notification(), new ResponseListener<ServiceResponseMessage>() {
            public void onResponse(final ServiceResponseMessage response) {
                // Thread here so we dont block the netty response thread
                WorkerThread.execute("Notification Thread", new Runnable() {
                    public void run() {
                        try {
                            @SuppressWarnings("unchecked") T t = (T) buildServiceInstance(serviceName, response, timeout);
                            serviceListener.onServiceAvailable(t);
                        }
                        catch (Exception e) {
                            // TODO: handle
                            logger.warning(e);
                        }
                    }
                });
            }

            public void onResponseFailure(Throwable reason) {
                serviceListener.onServiceFailure(reason);
            }

            public void onResponseTimeout() {
                serviceListener.onServiceTimeout();
            }
        });

    }

    public <T> void getService(final String channel, final String serviceName, final ServiceListener<T> serviceListener) {

        ServiceRequestMessage serviceRequestMessage = new ServiceRequestMessage(serviceName);
        sendRequest(channel, serviceRequestMessage, new Notification(), new ResponseListener<ServiceResponseMessage>() {
            public void onResponse(ServiceResponseMessage response) {
                try {
                    @SuppressWarnings("unchecked") T t = (T) buildChannelBasedServiceInstance(channel, serviceName, response);
                    serviceListener.onServiceAvailable(t);
                }
                catch (Exception e) {
                    // TODO: handle
                    logger.warning(e);
                }
            }

            public void onResponseFailure(Throwable reason) {
                serviceListener.onServiceFailure(reason);
            }

            public void onResponseTimeout() {
                serviceListener.onServiceTimeout();
            }
        });

    }

    protected Object buildServiceInstance(String serviceName, ServiceResponseMessage response, Timeout timeout) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException,
                    InvocationTargetException, NoSuchMethodException, ClassNotFoundException {

        String serviceType = response.getServiceType();
        logger.trace("Creating service proxy for interface class name '{}'", serviceType);
        Class<?> serviceClass = Class.forName(serviceType);

        AsyncNoChannelServiceProxy proxy = new AsyncNoChannelServiceProxy(serviceClass, serviceName, this, timeout);

        Class<?> proxyClass = Proxy.getProxyClass(Level3BlockingClient.class.getClassLoader(), new Class[] { serviceClass });
        Object newInstance = proxyClass.getConstructor(new Class[] { InvocationHandler.class }).newInstance(new Object[] { proxy });
        return newInstance;
    }

    protected Object buildChannelBasedServiceInstance(String channel, String serviceName, ServiceResponseMessage response) throws IllegalArgumentException, SecurityException, InstantiationException,
                    IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {

        String serviceType = response.getServiceType();
        logger.trace("Creating service proxy for interface class name '{}'", serviceType);
        Class<?> serviceClass = Class.forName(serviceType);

        AsyncChannelServiceProxy proxy = new AsyncChannelServiceProxy(channel, serviceClass, serviceName, this);

        Class<?> proxyClass = Proxy.getProxyClass(Level3BlockingClient.class.getClassLoader(), new Class[] { serviceClass });
        Object newInstance = proxyClass.getConstructor(new Class[] { InvocationHandler.class }).newInstance(new Object[] { proxy });
        return newInstance;
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

    public void register(String string, Class<?> serviceInterface, Object serviceProvider) {
        serviceRepository.registerService(string, serviceInterface, serviceProvider);
    }

    public <T> ServiceNotification<T> getService(String serviceName, Class<T> service) {
        ServiceNotification<T> notification = new ServiceNotification<T>();
        getService(serviceName, notification, Timeout.defaultTimeout);
        return notification;
    }
    
    public <T> ServiceNotification<T> getService(String serviceName, Class<T> service, Timeout timeout) {
        ServiceNotification<T> notification = new ServiceNotification<T>();
        getService(serviceName, notification, timeout);
        return notification;
    }
    
    public <T> ServiceNotification<T> getService(String channel, String serviceName, Class<T> service) {
        ServiceNotification<T> notification = new ServiceNotification<T>();
        getService(channel, serviceName, notification);
        return notification;
    }

    

    // public void send(String deliverToChannel, String replyToChannel, Object
    // message) {
    // MessageWrapper wrapper = new MessageWrapper();
    // wrapper.setDeliverToChannel(deliverToChannel);
    // wrapper.setReplyToChannel(replyToChannel);
    // wrapper.setPayload(message);
    // send(message);
    // }

}
