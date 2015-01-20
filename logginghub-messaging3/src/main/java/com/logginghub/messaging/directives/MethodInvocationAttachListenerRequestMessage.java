package com.logginghub.messaging.directives;

public class MethodInvocationAttachListenerRequestMessage {

    private String targetObjectID;
    private String methodName;
    private String responseChannel;

    public MethodInvocationAttachListenerRequestMessage() {}

    public MethodInvocationAttachListenerRequestMessage(String serviceName, String methodName, String responseChannel) {
        super();
        this.targetObjectID = serviceName;
        this.methodName = methodName;
        this.responseChannel = responseChannel;
    }

    public String getTargetObjectID() {
        return targetObjectID;
    }
    
    public void setTargetObjectID(String targetObjectID) {
        this.targetObjectID = targetObjectID;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getResponseChannel() {
        return responseChannel;
    }

    public void setResponseChannel(String responseChannel) {
        this.responseChannel = responseChannel;
    }

    public String getDestinationChannelID() {
        return null;
    }

    @Override public String toString() {
        return "MethodInvocationAttachListenerRequestMessage [targetObjectID=" + targetObjectID + ", methodName=" + methodName + ", responseChannel=" + responseChannel + "]";
    }
    
    

}
