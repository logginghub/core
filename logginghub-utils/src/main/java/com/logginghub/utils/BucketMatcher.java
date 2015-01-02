package com.logginghub.utils;

public interface BucketMatcher<T>
{
    boolean matches(T t);
}
