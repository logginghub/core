package com.logginghub.messaging.directives;

import java.util.Arrays;


public class MethodInvocationRequestMessage {

    private String operation;
    private String targetObjectID;
    private Object[] parameters;
    private String[] parameterTypes;

    public MethodInvocationRequestMessage(String operation, String targetObjectID, Object[] parameters, String[] parameterTypes) {
        this.operation = operation;
        this.targetObjectID = targetObjectID;
        this.parameters = parameters;
        this.parameterTypes = parameterTypes;
    }

    public MethodInvocationRequestMessage() {

    }
    
    public void setDestinationObjectID(String destinationObjectID) {
        this.targetObjectID = destinationObjectID;
    }

    public String getTargetObjectID() {
        return targetObjectID;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public void setParameterTypes(Class<?>[] parameterTypes2) {
        parameterTypes = new String[parameterTypes2.length];
        for (int i = 0; i < parameterTypes2.length; i++) {
            parameterTypes[i] = parameterTypes2[i].getName();
        }
    }

    public Class<?>[] getParameterTypesAsClasses() throws ClassNotFoundException {

        Class<?>[] classes;

        if (this.parameterTypes != null) {
            classes = new Class<?>[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                classes[i] = getClass(parameterTypes[i]);
            }
        }
        else {
            classes = null;
        }
        return classes;
    }

    private Class<?> getClass(String name) throws ClassNotFoundException {

        if (name.equals("byte")) return byte.class;
        if (name.equals("short")) return short.class;
        if (name.equals("int")) return int.class;
        if (name.equals("long")) return long.class;
        if (name.equals("char")) return char.class;
        if (name.equals("float")) return float.class;
        if (name.equals("double")) return double.class;
        if (name.equals("boolean")) return boolean.class;
        if (name.equals("void")) return void.class;
        return Class.forName(name);
    }

    @Override public String toString() {
        return "MethodInvocationRequestMessage [operation=" +
               operation +
               ", targetObjectID=" +
               targetObjectID +
               ", parameters=" +
               Arrays.toString(parameters) +
               ", parameterTypes=" +
               Arrays.toString(parameterTypes) +
               "]";
    }

    
}
