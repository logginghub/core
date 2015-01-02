package com.logginghub.utils.module;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/*
 * A service discovery instance that just returns proxies - useful if you just want to stub out the service discovery and use a module directly.
 */
public class ProxyServiceDiscovery implements ServiceDiscovery {

    public <T> T findService(Class<T> type) {
        return findService(type, (String) null);
    }

    public <T> T findService(Class<T> type, Class<?> generic) {
        return findService(type);
    }

    public <T> T findService(Class<T> type, Class<?> generic, String name) {
        return findService(type);
    }

    public <T> T findService(Class<T> type, String name) {

        @SuppressWarnings("unchecked") T proxy = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                                                            new Class<?>[] { type },
                                                                            new InvocationHandler() {
                                                                                public Object invoke(Object proxy, Method method, Object[] args)
                                                                                                throws Throwable {
                                                                                    return null;
                                                                                }
                                                                            });

        return proxy;

    }

}
