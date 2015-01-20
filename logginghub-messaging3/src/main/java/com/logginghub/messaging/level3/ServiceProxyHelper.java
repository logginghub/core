package com.logginghub.messaging.level3;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ServiceProxyHelper {
    public static boolean isLikelyListenerMethod(Method method, Object[] args) {
        return method.getName().contains("Listener") &&
               (method.getName().startsWith("add") || method.getName().startsWith("remove")) &&
               args.length == 1 &&
               method.getReturnType() == Void.TYPE &&
               Modifier.isInterface(method.getParameterTypes()[0].getModifiers());
    }
}
