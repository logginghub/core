package com.logginghub.logging.frontend;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A reflection listener that calls a method on an instance when the action
 * occurs.
 * 
 */
public class ReflectionDispatchActionListener implements ActionListener {
    private String methodName;
    private Object instance;
    private Object[] arguments;

    public ReflectionDispatchActionListener(String string, Object instance) {
        this.methodName = string;
        this.instance = instance;
    }

    public ReflectionDispatchActionListener(String method, Object[] args, Object instance) {
        this(method, instance);
        this.arguments = args;
    }

    public void actionPerformed(ActionEvent e) {
        Method method = null;
        try {
            Class<? extends Object> c = instance.getClass();
            method = c.getMethod(this.methodName, getArgumentClasses());
            method.invoke(instance, arguments);
        }
        catch (SecurityException e1) {
            throw new RuntimeException("SecurityException caught dispatching method " + method + " on instance " + instance, e1);
        }
        catch (IllegalArgumentException e1) {
            throw new RuntimeException("IllegalArgumentException caught dispatching method " + method + " on instance " + instance + ". Method was " + method, e1);
        }
        catch (NoSuchMethodException e1) {
            throw new RuntimeException("NoSuchMethodException caught dispatching method " + method + " on instance " + instance, e1);
        }
        catch (IllegalAccessException e1) {
            throw new RuntimeException("IllegalAccessException caught dispatching method " + method + " on instance " + instance, e1);
        }
        catch (InvocationTargetException e1) {
            throw new RuntimeException("InvocationTargetException caught dispatching method " + method + " on instance " + instance, e1);
        }
    }

    private Class<?>[] getArgumentClasses() {
        Class<?>[] classes;

        if (arguments == null) {
            classes = new Class<?>[] {};
        }
        else {
            classes = new Class<?>[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                classes[i] = arguments[i].getClass();
            }
        }
        return classes;
    }
}