package com.logginghub.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.logginghub.utils.filter.Filter;

public class CollectionUtils {

    public static <T extends Comparable<T>> List<T> toReverseSortedList(Collection<T> collection) {
        List<T> list = new ArrayList<T>();
        list.addAll(collection);
        Collections.sort(list, Collections.reverseOrder());
        return list;

    }

    public static <T extends Comparable<T>> List<T> toSortedList(Collection<T> collection) {
        List<T> list = new ArrayList<T>();
        list.addAll(collection);
        Collections.sort(list);
        return list;
    }

    public static <T> List<T> filter(List<T> list, Filter<T> filter) {
        List<T> filtered = new ArrayList<T>();
        for (T t : list) {
            if (filter.passes(t)) {
                filtered.add(t);
            }
        }
        return filtered;
    }

    public static <T> List<T> newArrayList(T... items) {
        List<T> list = new ArrayList<T>();
        for (T t : items) {
            list.add(t);
        }

        return list;
    }

    public static <A, B> List<B> transform(List<A> values, Function<A, B> function) {
        ArrayList<B> results = new ArrayList<B>();
        for (A a : values) {
            results.add(function.apply(a));
        }
        return results;

    }

    public static <T> void addAll(T[] array, List<T> list) {
        for (T t : array) {
            list.add(t);
        }
    }

    public static <T extends Comparable<T>> List<T> toReverseSortedList(Collection<T> values, int top) {
        List<T> list = toReverseSortedList(values);
        List<T> topList = extractFirst(top, list);
        return topList;
    }

    public static <T extends Comparable<T>> List<T> extractFirst(int numberToExtract, List<T> list) {
        List<T> topList = new ArrayList<T>();
        for (int i = 0; i < numberToExtract && i < list.size(); i++) {
            topList.add(list.get(i));
        }
        return topList;
    }

    public static <T extends Comparable<T>> List<T> toSortedList(Collection<T> values, int top) {

        List<T> list = toSortedList(values);

        List<T> topList = new ArrayList<T>();
        for (int i = 0; i < top && i < list.size(); i++) {
            topList.add(list.get(i));
        }

        return topList;
    }

    public static <T> Set<T> newHashSet(T... values) {
        Set<T> set = new HashSet<T>();
        for (T t : values) {
            set.add(t);
        }
        return set;
    }

    public static List<String> toList(String[] split) {
        List<String> list = new ArrayList<String>();
        for (String string : split) {
            list.add(string);
        }
        return list;

    }

    public static String[] toArray(List<String> strings) {
        String[] array = new String[strings.size()];
        return strings.toArray(array);
    }
    
    public static <T>  T[] toArray(List<T> object, Class<T> clazz) {
        @SuppressWarnings("unchecked") T[] array = (T[]) Array.newInstance(clazz, object.size());
        object.toArray(array);
        return array;
    }

    public static File[] toFileArray(List<File> files) {
        File[] array = new File[files.size()];
        return files.toArray(array);
    }
}
