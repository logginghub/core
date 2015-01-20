package com.logginghub.logging.listeners;

/**
 * Generated values are produced by feeding LogEvents to ValueGenerator implementations. You need
 * to add a GeneratedValueListener to the ValueGenerator to listen to the values it produces.
 * @author James
 * @param <T>
 */
public interface GeneratedValueListener<T>
{
    public void onNewValue(T value);
}
