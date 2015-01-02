package com.logginghub.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.observable.ObservableProperty;

public class ReflectionUtils {
    /**
     * Class to encapsulate an attempt - we can't just return null if we didn't get the item as this
     * is a valid result.
     * 
     * @author James
     * 
     */
    private static class AttemptStatus {
        Object value;
        boolean success;
    }

    @SuppressWarnings("unchecked") public static <T> T getFieldValue(Field field, Object object) {
        AttemptStatus status = new AttemptStatus();

        attemptDirect(field, object, status);
        if (!status.success) {
            attemptDirectForced(field, object, status);
            if (!status.success) {
                attemptViaAccessor(field, object, status);

                if (!status.success) {
                    throw new RuntimeException(String.format("We failed to get the value for field '%s' from object '%s' (type '%s'), we tried to access it via the field directly and then via any suitable accessors.",
                                                             field.getName(),
                                                             object,
                                                             object.getClass().getSimpleName()));
                }
            }
        }

        return (T) status.value;
    }

    private static void attemptViaAccessor(Field field, Object object, AttemptStatus status) {
        String[] accessorNames = getPotentialAccessorNamesForField(field);

        for (String potentialAccessor : accessorNames) {
            Method method = findNoArgsMethod(potentialAccessor, object);
            if (method != null) {
                attemptViaMethod(method, object, status);
                if (status.success) {
                    break;
                }
            }
        }
    }

    public static Method findNoArgsMethod(String potentialAccessor, Object object) {
        Method method;

        Class<?> c = object.getClass();

        try {
            method = c.getMethod(potentialAccessor, new Class<?>[] {});
        }
        catch (SecurityException e) {
            method = null;
        }
        catch (NoSuchMethodException e) {
            method = null;
        }

        return method;
    }

    public static String[] getPotentialAccessorNamesForField(Field field) {
        String fieldNamePlain = field.getName();
        String fieldNameStripped = stripFieldPrefixes(fieldNamePlain);
        String fieldName = StringUtils.leadingUppercase(fieldNameStripped);

        String[] potentials;

        if (field.getType().equals(Boolean.TYPE) || field.getType().equals(Boolean.class)) {
            potentials = new String[] { "is" + fieldName, "get" + fieldName, "will" + fieldName };
        }
        else {
            potentials = new String[] { "get" + fieldName };
        }

        return potentials;
    }

    public static String stripFieldPrefixes(String fieldName) {
        String stripped;

        if (fieldName.startsWith("m_")) {
            stripped = fieldName.substring(2);
        }
        else if (fieldName.startsWith("_")) {
            stripped = fieldName.substring(1);
        }
        else {
            stripped = fieldName;
        }

        return stripped;
    }

    private static void attemptDirectForced(Field field, Object object, AttemptStatus status) {
        try {
            field.setAccessible(true);
            attemptDirect(field, object, status);
        }
        catch (SecurityException se) {
            status.success = false;
        }
    }

    private static void attemptDirect(Field field, Object object, AttemptStatus status) {
        try {
            status.value = field.get(object);
            status.success = true;
        }
        catch (IllegalArgumentException e) {
            status.success = false;
        }
        catch (IllegalAccessException e) {
            status.success = false;
        }
    }

    private static void attemptViaMethod(Method method, Object object, AttemptStatus status) {
        try {
            status.value = method.invoke(object, new Object[] {});
            status.success = true;
        }
        catch (InvocationTargetException e) {
            status.success = false;
        }
        catch (IllegalArgumentException e) {
            status.success = false;
        }
        catch (IllegalAccessException e) {
            status.success = false;
        }
    }

    public static List<String> getFieldNames(Class<? extends Object> c) {
        List<String> names = new ArrayList<String>();

        Field[] declaredFields = c.getDeclaredFields();
        for (Field field : declaredFields) {
            names.add(field.getName());
        }

        Field[] fields = c.getFields();
        for (Field field : fields) {
            names.add(field.getName());
        }

        return names;
    }

    public static void invokeIfMethodExists(String methodToCall, Object toCallOn, Object... arguments) {
        Method findFirstMethod = findFirstMethod(toCallOn.getClass(), methodToCall);
        if (findFirstMethod != null) {
            try {
                findFirstMethod.invoke(toCallOn, arguments);
            }
            catch (Exception e) {
                throw new RuntimeException(String.format("Failed to invoke method '%s' on instance of class '%s'", methodToCall, toCallOn.getClass()
                                                                                                                                         .getName()),
                                           e);
            }
        }
    }

    public static Object invoke(Method method, Object object) {
        try {
            return method.invoke(object, (Object[]) null);
        }
        catch (Exception e) {
            throw new RuntimeException(String.format("Failed to invoke method '%s' on instance of class '%s'", method, object.getClass().getName()));
        }
    }

    public static void invoke(String methodToCall, Object toCallOn) {
        Class<?> c = toCallOn.getClass();
        try {
            Method m = c.getMethod(methodToCall, (Class[]) null);
            m.invoke(toCallOn, (Object[]) null);
        }
        catch (Exception e) {
            throw new RuntimeException(String.format("Failed to invoke method '%s' on instance of class '%s'", methodToCall, c.getName()));
        }
    }

    public static List<Method> findMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {

        List<Method> methodsWithAnnotation = new ArrayList<Method>();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(annotationClass) != null) {
                methodsWithAnnotation.add(method);
            }
        }

        return methodsWithAnnotation;

    }

    public static List<Object> invoke(List<Method> beforeMethods, Object instance) {
        List<Object> results = new ArrayList<Object>();
        for (Method method : beforeMethods) {
            Object result;
            try {
                result = method.invoke(method, instance);
            }
            catch (Exception e) {
                throw new RuntimeException(StringUtils.format("Failed to invoke method {} on instance {} of class {}",
                                                              method,
                                                              instance,
                                                              instance.getClass().getName()), e);
            }
            results.add(result);
        }
        return results;
    }

    public static Method findFirstMethod(Class<?> clazz, String string) {

        Method found = null;
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(string)) {
                found = method;
                break;
            }
        }

        return found;

    }

    public static Object getField(String string, Object instance) throws SecurityException, NoSuchFieldException, IllegalArgumentException,
                    IllegalAccessException {
        Class<? extends Object> clazz = instance.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getName().equals(string)) {
                field.setAccessible(true);
                return field.get(instance);
            }
        }
        return null;
    }

    public static Field findField(String string, Object instance) throws SecurityException, NoSuchFieldException, IllegalArgumentException,
                    IllegalAccessException {
        Class<? extends Object> clazz = instance.getClass();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getName().equals(string)) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked") public static <T> T getFieldRuntime(String string, Object instance) {
        try {
            return (T) getField(string, instance);
        }
        catch (Exception e) {
            throw new RuntimeException(String.format("Failed to get field %s from object %s of class %s", string, instance, instance.getClass()
                                                                                                                                    .getName()), e);
        }
    }

    public static <T> T createNewInstance(String classname) {
        return instantiate(classname);
    }

    public static <T> T newInstance(String classname) {
        return instantiate(classname);
    }

    public static <T> T instantiate(String classname) {
        Class<T> clazz = getClassQuietly(classname);
        return instantiate(clazz);
    }

    public static <T> T instantiate(Class<T> clazz) {

        T newInstance;
        try {
            newInstance = clazz.newInstance();
        }
        catch (InstantiationException e) {
            throw FormattedRuntimeException.build("Failed to instantiate instance of class {}, does it have a default constructor?",
                                                  clazz.getName(),
                                                  e);
        }
        catch (IllegalAccessException e) {
            throw FormattedRuntimeException.build("Failed to instantiate instance of class {} due to access exception, is the default constructor public?",
                                                  clazz.getName(),
                                                  e);
        }
        return newInstance;
    }

    public static <T> Class<T> getClassQuietly(String classname) {
        try {
            @SuppressWarnings("unchecked") Class<T> clazz = (Class<T>) Class.forName(classname);
            return clazz;
        }
        catch (ClassNotFoundException e) {
            throw FormattedRuntimeException.build("Failed to file class {}, please double check the class name and classpath", classname, e);
        }
    }

    public static boolean isPrimativeType(Class<?> objectType) {
        boolean isBaseType;

        if (objectType == char[].class) {
            isBaseType = true;
        }
        else if (objectType == int[].class) {
            isBaseType = true;
        }
        else if (objectType == boolean[].class) {
            isBaseType = true;
        }
        else if (objectType == short[].class) {
            isBaseType = true;
        }
        else if (objectType == byte[].class) {
            isBaseType = true;
        }
        else if (objectType == long[].class) {
            isBaseType = true;
        }
        else if (objectType == float[].class) {
            isBaseType = true;
        }
        else if (objectType == double[].class) {
            isBaseType = true;
        }
        else if (objectType == Integer.TYPE) {
            isBaseType = true;
        }
        else if (objectType == Character.TYPE) {
            isBaseType = true;
        }
        else if (objectType == Boolean.TYPE) {
            isBaseType = true;
        }
        else if (objectType == Short.TYPE) {
            isBaseType = true;
        }
        else if (objectType == Byte.TYPE) {
            isBaseType = true;
        }
        else if (objectType == Long.TYPE) {
            isBaseType = true;
        }
        else if (objectType == Float.TYPE) {
            isBaseType = true;
        }
        else if (objectType == Double.TYPE) {
            isBaseType = true;
        }
        else {
            isBaseType = false;
        }

        return isBaseType;
    }

    public static Object invoke(Object target, String method, Object... params) {
        try {
            Method findFirstMethod = findFirstMethod(target.getClass(), method);
            if (findFirstMethod != null) {
                return findFirstMethod.invoke(target, params);
            }
            else {
                throw new RuntimeException(StringUtils.format("No method called '{}' found on target class '{}'", method, target.getClass()));
            }
        }
        catch (Exception e) {
            throw new RuntimeException(String.format("Method invocation failed for method name %s on class %s", method, target.getClass().getName()),
                                       e);
        }
    }

    public static Object invoke(Object instance, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
        try {
            Method method = instance.getClass().getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            Object result = method.invoke(instance, parameters);
            return result;
        }
        catch (Exception e) {
            throw new RuntimeException(String.format("Method invocation failed for method name %s on class %s", methodName, instance.getClass()
                                                                                                                                    .getName()), e);
        }

    }

    public static List<FieldAccessor> getDeclaredFieldAccessors(Object object, Class<?> stopAtSuperClass) {
        Class<?> type = object.getClass();
        List<Field> fields = getAllFields(type, stopAtSuperClass);

        List<FieldAccessor> accessors = new ArrayList<FieldAccessor>();
        for (Field field : fields) {
            accessors.add(new FieldAccessor(field, object));
        }

        return accessors;
    }

    public static List<FieldAccessor> getDeclaredFieldAccessors(Object object) {
        Class<?> type = object.getClass();
        List<Field> fields = getDeclaredFields(type);

        List<FieldAccessor> accessors = new ArrayList<FieldAccessor>();
        for (Field field : fields) {
            accessors.add(new FieldAccessor(field, object));
        }

        return accessors;
    }

    public static List<Field> getDeclaredFields(Class<?> type) {
        Field[] declaredFields = type.getDeclaredFields();
        List<Field> fields = new ArrayList<Field>();
        for (Field field : declaredFields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                fields.add(field);
            }
        }
        return fields;
    }

    @SuppressWarnings("unchecked") public static <T> List<T> extractAllFields(Object target, Class<?> clazz, Class<? extends T> fieldTypeClass) {

        List<T> results = new ArrayList<T>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {

            if (field.getType().equals(fieldTypeClass)) {
                try {
                    field.setAccessible(true);
                    results.add((T) field.get(target));
                }
                catch (Exception e) {
                    throw new FormattedRuntimeException(e, "Failed to access field '{}' of object '{}'", field, target);
                }
            }

        }

        return results;

    }

    private static List<Field> getAllFields(Class<?> type, Class<?> stopAtSuperClass) {
        List<Field> fields = new ArrayList<Field>();
        getAllFields(fields, type, stopAtSuperClass);
        return fields;
    }

    public static List<FieldAccessor> getAllFields(Object object) {
        Class<?> type = object.getClass();
        List<Field> fields = new ArrayList<Field>();
        getAllFields(fields, type);

        List<FieldAccessor> accessors = new ArrayList<FieldAccessor>();
        for (Field field : fields) {
            accessors.add(new FieldAccessor(field, object));
        }

        return accessors;
    }

    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        getAllFields(fields, type);
        return fields;
    }

    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            fields.add(field);
        }

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    public static List<Field> getAllFields(List<Field> fields, Class<?> type, Class<?> stopAtSuperType) {
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                fields.add(field);
            }
        }

        Class<?> superclass = type.getSuperclass();
        if (type.getSuperclass() != null && superclass != stopAtSuperType) {
            fields = getAllFields(fields, type.getSuperclass(), stopAtSuperType);
        }

        return fields;
    }

    public static void setField(String fieldName, Object instance, Object value) throws IllegalArgumentException, IllegalAccessException,
                    NoSuchFieldException {
        boolean found = false;

        List<Field> declaredFields = getAllFields(instance.getClass());
        for (Field field : declaredFields) {
            if (field.getName().equals(fieldName)) {
                boolean accessible = field.isAccessible();
                if (!accessible) {
                    field.setAccessible(true);
                }

                field.set(instance, value);

                if (!accessible) {
                    field.setAccessible(false);
                }

                found = true;
                break;
            }
        }

        if (!found) {
            throw new NoSuchFieldException(fieldName);
        }
    }

    public static void setFieldFromString(String fieldName, Object instance, String value) throws IllegalArgumentException, IllegalAccessException,
                    NoSuchFieldException {
        boolean found = false;

        List<Field> declaredFields = getAllFields(instance.getClass());
        for (Field field : declaredFields) {
            if (field.getName().equals(fieldName)) {
                boolean accessible = field.isAccessible();
                if (!accessible) {
                    field.setAccessible(true);
                }

                Class<?> type = field.getType();

                if (type == ObservableProperty.class) {

                    ObservableProperty observableProperty = (ObservableProperty) field.get(instance);
                    if (observableProperty != null) {
                        observableProperty.set(value);
                    }

                }
                else {
                    Object coercedValue = value;
                    if (type == Integer.TYPE) {
                        coercedValue = Integer.parseInt(value);
                    }
                    else if (type == Boolean.TYPE) {
                        coercedValue = Boolean.parseBoolean(value);
                    }
                    else if (type == Short.TYPE) {
                        coercedValue = Short.parseShort(value);
                    }
                    else if (type == Long.TYPE) {
                        coercedValue = Long.parseLong(value);
                    }
                    else if (type == Double.TYPE) {
                        coercedValue = Double.parseDouble(value);
                    }
                    else if (type == Float.TYPE) {
                        coercedValue = Float.parseFloat(value);
                    }
                    else if (type == Byte.TYPE) {
                        coercedValue = Byte.parseByte(value);
                    }

                    field.set(instance, coercedValue);

                    if (!accessible) {
                        field.setAccessible(false);
                    }
                }

                found = true;
                break;
            }
        }

        if (!found) {
            throw new NoSuchFieldException(fieldName);
        }

    }

    public static boolean classExists(String classname) {
        boolean exists;
        try {
            Class.forName(classname);
            exists = true;
        }
        catch (ClassNotFoundException cnfe) {
            exists = false;
        }

        return exists;
    }

}
