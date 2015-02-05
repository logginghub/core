package com.logginghub.utils.sof;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 04/02/15.
 */
public class SofHelper {

    public static List<String> readStringList(int field, SofReader reader) throws SofException {
        final String[] strings = reader.readStringArray(field);
        List<String> list = new ArrayList<String>();
        Collections.addAll(list, strings);
        return list;
    }

    public static <T extends SerialisableObject> List<T> readList(int field, Class<T> clazz, SofReader reader) throws SofException {
        List<T> list = new ArrayList<T>();
        T[] items = (T[]) reader.readObjectArray(field, clazz);
        Collections.addAll(list, items);
        return list;
    }

    public static Map<String, String> readStringMap(int field1, int field2, SofReader reader) throws SofException {

        String[] keys = reader.readStringArray(field1);
        String[] values = reader.readStringArray(field2);

        Map<String, String> map = new HashMap<String, String>();

        for(int k = 0; k < keys.length; k++) {
            map.put(keys[k], values[k]);
        }

        return map;
    }

    public static void write(int field, Collection<String> arguments, SofWriter writer) throws SofException {
        final String[] strings = arguments.toArray(new String[arguments.size()]);
        writer.write(field, strings);
    }

    public static void write(int field1, int field2, Map<String, String> map, SofWriter writer) throws SofException {
        write(field1, map.keySet(), writer);
        write(field2, map.values(), writer);
    }
}
