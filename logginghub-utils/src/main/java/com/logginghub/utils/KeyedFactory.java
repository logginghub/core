package com.logginghub.utils;

public interface KeyedFactory<K, V> {
    V create(K key);
}