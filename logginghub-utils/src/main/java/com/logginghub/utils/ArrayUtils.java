package com.logginghub.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArrayUtils {

    public static <T> void reverse(T[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            T temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    public static byte[] doubleSize(byte[] readBuffer) {
        byte[] newBuffer = new byte[readBuffer.length * 2];
        System.arraycopy(readBuffer, 0, newBuffer, 0, readBuffer.length);
        return newBuffer;
    }

    public static <T> Set<T> toSet(T[] array) {
        Set<T> set = new HashSet<T>();
        for (T t : array) {
            set.add(t);
        }

        return set;
    }

    public static <T> List<T> toList(T[] array) {
        List<T> list = new ArrayList<T>();
        for (T t : array) {
            list.add(t);
        }
        return list;
    }
}
