package com.logginghub.utils.module;

public interface ServiceDiscovery {
    public <T> T findService(Class<T> type);
    public <T> T findService(Class<T> type, String name);
    public <T> T findService(Class<T> type, Class<?> genericType);
    public <T> T findService(Class<T> type, Class<?> genericType, String name);
}
