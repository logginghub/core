package com.logginghub.messaging.directives;

public class MethodInvocationDettachListenerRequestMessage {

    private String targetObjectID;
    private String listenerObjectID;
    private String methodName;


    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public String getMethodName() {
        return methodName;
         
    }

    public String getTargetObjectID() {
        return targetObjectID;
    }

    public void setTargetObjectID(String targetObjectID) {
        this.targetObjectID = targetObjectID;
    }

    public String getListenerObjectID() {
        return listenerObjectID;
    }

    public void setListenerObjectID(String listenerObjectID) {
        this.listenerObjectID = listenerObjectID;
    }

    @Override public String toString() {
        return "MethodInvocationDettachListenerRequestMessage [targetObjectID=" + targetObjectID + ", listenerObjectID=" + listenerObjectID + ", methodName=" + methodName + "]";
    }

    
    
    
}
