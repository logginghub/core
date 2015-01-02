package com.logginghub.utils.module;

import java.util.HashMap;
import java.util.Map;

import com.logginghub.utils.ExceptionPolicy;
import com.logginghub.utils.Pair;
import com.logginghub.utils.logging.Logger;

public class ConfigurableServiceDiscovery implements ServiceDiscovery {

    private Map<Class<?>, Object> boundInstances = new HashMap<Class<?>, Object>();
    // jshaw - this used to be set to log, but that doesn't work so well for unit tests.
    private ExceptionPolicy exceptionPolicy = new ExceptionPolicy(ExceptionPolicy.Policy.RethrowOnAny);
    private Map<Pair<Class<?>, String>, Object> namedBoundInstances = new HashMap<Pair<Class<?>, String>, Object>();
    private Map<ClassAndGenericKey, Object> namedGenericInstances = new HashMap<ClassAndGenericKey, Object>();
    
    public static class ClassAndGenericKey {
        Class<?> generic;
        String name;
        Class<?> type;

        public ClassAndGenericKey(Class<?> type, Class<?> generic, String name) {
            super();
            this.type = type;
            this.generic = generic;
            this.name = name;
        }

        @Override public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ClassAndGenericKey other = (ClassAndGenericKey) obj;
            if (generic == null) {
                if (other.generic != null) {
                    return false;
                }
            }
            else if (!generic.equals(other.generic)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            }
            else if (!name.equals(other.name)) {
                return false;
            }
            if (type == null) {
                if (other.type != null) {
                    return false;
                }
            }
            else if (!type.equals(other.type)) {
                return false;
            }
            return true;
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((generic == null) ? 0 : generic.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

    }

    private static final Logger logger = Logger.getLoggerFor(ConfigurableServiceDiscovery.class);

    public <T> void bind(Class<T> type, Class<?> generic, String name, T providerInstance) {
        ClassAndGenericKey key = new ClassAndGenericKey(type, generic, name);
        namedGenericInstances.put(key, providerInstance);
    }

    public <T> void bind(Class<T> type, Class<?> generic, T providerInstance) {
        bind(type, generic, null, providerInstance);
    }

    public <T> void bind(Class<T> type, String name, T instance) {
        namedBoundInstances.put(new Pair<Class<?>, String>(type, name), instance);
    }

    public <T> void bind(Class<T> type, T instance) {
        boundInstances.put(type, instance);
    }

    public <T> T findService(Class<T> type) {
        return findService(type, (String)null);
    }

    public <T> T findService(Class<T> type, Class<?> generic) {
        ClassAndGenericKey key = new ClassAndGenericKey(type, generic, null);
        T instance = (T) namedGenericInstances.get(key);
        if (instance == null) {
            exceptionPolicy.handle("Service discovery failed for class '{}' with generic type '{}'", type.getName(), generic.getName());
        }

        return instance;
    }

    public <T> T findService(Class<T> type, Class<?> generic, String name) {
        ClassAndGenericKey key = new ClassAndGenericKey(type, generic, name);
        T instance = (T) namedGenericInstances.get(key);
        if (instance == null) {
            exceptionPolicy.handle("Service discovery failed for class '{}' with generic type '{}' and name '{}'", type.getName(), generic.getName(), name);
        }

        return instance;
    }

    @SuppressWarnings("unchecked") public <T> T findService(Class<T> type, String name) {

        T instance;
        if (name == null || name.length() == 0) {
            instance = (T) boundInstances.get(type);
        }
        else {
            Pair<Class<?>, String> key = new Pair<Class<?>, String>(type, name);
            instance = (T) namedBoundInstances.get(key);
        }

        if (instance == null) {
            exceptionPolicy.handle("Service discovery failed for class '{}' and name '{}'", type.getName(), name);
        }

        return instance;

    }

    public ExceptionPolicy getExceptionPolicy() {
        return exceptionPolicy;
    }

    public void setExceptionPolicy(ExceptionPolicy exceptionPolicy) {
        this.exceptionPolicy = exceptionPolicy;
    }

}
