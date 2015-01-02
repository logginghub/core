package com.logginghub.utils;

/**
 * Interface for generic handler - if it "handles" the object passed in, it's job is done. Useful
 * for one-shot objects, like request-reply message processors.
 * 
 * @author James
 * 
 * @param <T>
 */
public interface Handler<T> {
    boolean handle(T t);
}
