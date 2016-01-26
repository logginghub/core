package com.logginghub.utils.observable;

/**
 * Marker interface for something that is bindable
 */
public interface BindableToModel<T extends Observable> {
    void bind(T t);
    void unbind(T t);
}
