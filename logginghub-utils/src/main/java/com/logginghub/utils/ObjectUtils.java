package com.logginghub.utils;


public class ObjectUtils {

    public static <T> T instantiate(String classname) {
        return ReflectionUtils.instantiate(classname);
    }

    public static <T> T instantiate(Class<T> clazz) {
        return ReflectionUtils.instantiate(clazz);
    }

    public static <T> Class<T> getClassQuietly(String classname) {
        return ReflectionUtils.getClassQuietly(classname);
    }

}
