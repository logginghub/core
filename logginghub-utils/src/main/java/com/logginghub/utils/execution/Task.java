package com.logginghub.utils.execution;

import java.io.Serializable;

public interface Task<T> extends Serializable
{
    T run();
}
