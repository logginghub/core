package com.logginghub.messaging.directives;

public class ServiceRequestMessage {

    private String serviceName;

    public ServiceRequestMessage(String serviceName) {
        super();
        this.serviceName = serviceName;
    }

    public ServiceRequestMessage() {}
    
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override public String toString() {
        return "ServiceRequestMessage [serviceName=" + serviceName + "]";
    }

    
    
    
}
