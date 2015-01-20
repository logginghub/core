package com.logginghub.messaging.level3;

import java.util.HashMap;
import java.util.Map;

import com.logginghub.utils.logging.Logger;

public class ServiceRepository {

    private static final Logger logger = Logger.getLoggerFor(ServiceRepository.class);
    private Map<String, Service> registeredServices = new HashMap<String, Service>();
    private Map<Object, String> namesToMappedObject = new HashMap<Object, String>();

    static int count =0;
    public ServiceRepository() {
        logger.trace("New service repository constructed : " + count++);
    }

    public void registerService(String serviceName, Class<?> serviceInterface, Object object) {
        Service value = new Service(serviceInterface, object);
        registeredServices.put(serviceName, value);
        namesToMappedObject.put(object, serviceName);
        logger.debug("Registered service '{}' with class '{}' and instance '{}'", serviceName, serviceInterface.getName(), object);
    }

    public Service getService(String name) {
        Service object = registeredServices.get(name);
        return object;
    }

    public String getNameForObject(Object object) {
        return namesToMappedObject.get(object);
    }

}
