package com.logginghub.utils.observable;

/**
 * Marker interface for something that is bindable
 */
public interface BindableToController<T extends Controller> {
    void bind(T t);
    void unbind(T t);
}
