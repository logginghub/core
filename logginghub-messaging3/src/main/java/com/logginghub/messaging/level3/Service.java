package com.logginghub.messaging.level3;

public class Service {
    private Class<?> serviceInterface;
    private Object providerInstance;

    public Service(Class<?> serviceInterface, Object providerInstance) {
        super();
        this.serviceInterface = serviceInterface;
        this.providerInstance = providerInstance;
    }

    public Object getProviderInstance() {
        return providerInstance;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

}
