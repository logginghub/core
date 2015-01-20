package com.logginghub.logging.frontend.model;

public interface Matcher<T> {
    boolean matches(T t);
}
