package com.logginghub.utils;

public interface Convertor<X, Y> {
    X convert(Y y);
}
