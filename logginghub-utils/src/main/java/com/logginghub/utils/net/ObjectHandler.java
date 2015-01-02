package com.logginghub.utils.net;

public interface ObjectHandler<T>
{
    public void handle(T t, ObjectSocketHandler source);
}
