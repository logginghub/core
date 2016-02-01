package com.logginghub.utils;


import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ObjectUtils {

    public static <T> Class<T> getClassQuietly(String classname) {
        return ReflectionUtils.getClassQuietly(classname);
    }

    public static <T> T instantiate(Class<T> clazz) {
        return ReflectionUtils.instantiate(clazz);
    }

    public static <T> T instantiate(String classname) {
        return ReflectionUtils.instantiate(classname);
    }

    public static String recursiveDump(Object object) {
        StringBuilder stringBuilder = new StringBuilder();

        Set<String> ignoreFields = new HashSet<String>();
        ignoreFields.add("logger");
        ignoreFields.add("parent");
        ignoreFields.add("listeners");
        ignoreFields.add("this");
        ignoreFields.add("this$0");
        ignoreFields.add("this$1");
        ignoreFields.add("this$2");

        Set<Class> toStringTypes = new HashSet<Class>();
        toStringTypes.add(Class.class);
        toStringTypes.add(Field.class);

        Set<Object> visitedInstances = new HashSet<Object>();

        recursiveDump(object, stringBuilder, 0, ignoreFields, toStringTypes, visitedInstances);
        return stringBuilder.toString();
    }


    public static void recursiveDump(Object object,
                                     StringBuilder stringBuilder,
                                     int indent,
                                     Set<String> ignoreFields,
                                     Set<Class> toStringTypes,
                                     Set<Object> visitedInstances) {

        if (visitedInstances.contains(object)) {
            stringBuilder.append("[already visited " + System.identityHashCode(object));
        } else {

            visitedInstances.add(object);

            //            Out.out("-----------------------------");
            //            Out.out(stringBuilder);

            String indentString = StringUtils.repeat("    ", indent);
            String subindentString = StringUtils.repeat("    ", indent + 1);

            if (object instanceof Map) {
                Map map = (Map) object;

                stringBuilder.append(object.getClass().getSimpleName()).append(" {");

                Set<Entry> set = map.entrySet();
                for (Entry entry : set) {

                    Object key = entry.getKey();
                    Object data = entry.getValue();

                    stringBuilder.append(indentString);
                    recursiveDump(key, stringBuilder, indent + 1, ignoreFields, toStringTypes, visitedInstances);
                    stringBuilder.append(" : ");
                    recursiveDump(data, stringBuilder, indent + 1, ignoreFields, toStringTypes, visitedInstances);
                    stringBuilder.append(StringUtils.newline);
                }

                stringBuilder.append("}");

            } else if (object instanceof Collection) {

                stringBuilder.append("(").append(object.getClass().getSimpleName()).append(") [").append(StringUtils.newline);

                for (Object o : (Collection) object) {

                    recursiveDump(o, stringBuilder, indent + 1, ignoreFields, toStringTypes, visitedInstances);
                    stringBuilder.append(StringUtils.newline);
                }

                stringBuilder.append(indentString).append("]");

            } else {

                if (object == null) {
                    stringBuilder.append("null");
                } else {

                    stringBuilder.append(indentString).append(object.getClass().getSimpleName()).append(" { ").append(StringUtils.newline);
                    List<Field> allFields = ReflectionUtils.getAllFields(object.getClass());
                    for (Field fieldName : allFields) {
                        if (!ignoreFields.contains(fieldName.getName()) && !Modifier.isStatic(fieldName.getModifiers())) {
                            stringBuilder.append(subindentString);
                            stringBuilder.append(fieldName.getName());
                            stringBuilder.append(" : ");
                            stringBuilder.append("(").append(fieldName.getType().getSimpleName()).append(") : ");

                            Object result = ReflectionUtils.getFieldValue(fieldName, object);

                            Class fieldNameClass = fieldName.getType();
                            if (toStringTypes.contains(fieldNameClass)) {
                                stringBuilder.append(result);
                            }else if(result == null) {
                                stringBuilder.append("null");
                            }
                            else {

                                if (result instanceof Boolean ||
                                    result instanceof Long ||
                                    result instanceof Integer ||
                                    result instanceof Short ||
                                    result instanceof Byte ||
                                    result instanceof Character ||
                                    result instanceof Double ||
                                    result instanceof Float || result instanceof Enum) {

                                    stringBuilder.append(result);

                                } else if (result instanceof String) {
                                    stringBuilder.append("'").append(result).append("'");
                                } else if (result instanceof char[]) {
                                    stringBuilder.append(Arrays.toString((char[]) result));
                                } else {
                                    recursiveDump(result, stringBuilder, indent + 1, ignoreFields, toStringTypes, visitedInstances);
                                }

                            }

                            stringBuilder.append(StringUtils.newline);
                        }
                    }
                    stringBuilder.append(indentString).append(" }");
                }


            }

        }
    }

}
