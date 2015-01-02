package com.logginghub.utils.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.logginghub.utils.FactoryMapDecorator;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableDouble;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;

public class ReflectionBinder {

    private static final Logger logger = Logger.getLoggerFor(ReflectionBinder.class);

    public Bag fromObject(Object object) throws Exception {

        Bag bag = new Bag();
        if (object == null) {
            // TODO : mark it as null?
        }
        else {

            Object helper = null;
            Class<?> clazz = object.getClass();

            if (isDirectlyEncodable(clazz)) {
                logger.trace("Directly encoding object '{}' of class '{}' into buffer {}", object, clazz, helper);
                directlyEncode(clazz, object, bag);
            }
            else {
                logger.trace("Attempting to encode object '{}' of class '{}' into buffer {}", object, clazz, helper);
                Set<String> set = ignoredFieldsByClass.get(clazz);
                Field[] declaredFields = clazz.getDeclaredFields();
                for (Field field : declaredFields) {
                    if (isSerialisable(set, field)) {
                        field.setAccessible(true);
                        Object fieldInstance = field.get(object);
                        Class<?> fieldType = field.getType();

                        boolean isAmbiguous = false;

                        // if (fieldInstance != null) {
                        // Class<?> instanceType = fieldInstance.getClass();
                        // if (fieldType != instanceType) {
                        // // This is an ambiguous field, we need to encode
                        // // the object with its class name
                        // fieldType = instanceType;
                        // isAmbiguous = true;
                        // }
                        // }

                        logger.trace("Encoding field '{}' type '{}' value '{}' to buffer {}...", field.getName(), fieldType.getSimpleName(), fieldInstance, helper);

                        // This is a little bit annoying as it doesn't recurse
                        // cleanly - due to the field type? Can't we just read
                        // that from the object anyway now we've got the null
                        // byte?
                        // if (isDirectlyEncodable(fieldType)) {
                        if (fieldInstance == null) {
                            // helper.writeByte((byte) 0);
                        }
                        else {
                            // logger.trace("... directly encoding field");
                            // if (isAmbiguous) {
                            // // helper.writeByte((byte) 2);
                            // encodeWithClass(fieldInstance, helper);
                            // }
                            // else {
                            bag.put(field.getName(), fieldInstance);
                            // }
                        }
                        // }
                        // else {
                        // if (isAmbiguous) {
                        // // helper.writeByte((byte) 2);
                        // encodeWithClass(fieldInstance, helper);
                        // }
                        // else {
                        // encode(fieldInstance, helper);
                        // }
                        // }
                    }
                }
            }

        }

        return bag;

    }

    private void directlyEncode(Class<?> fieldType, Object object, Bag bag) {
        bag.setValue(object);
    }

    private boolean isDirectlyEncodable(Class<?> clazz) {
        boolean isDirectlyEncodable = (clazz == String.class) ||
                                      (clazz == Date.class) ||
                                      (clazz == Integer.class || clazz == Integer.TYPE) ||
                                      (clazz == Short.class || clazz == Short.TYPE) ||
                                      (clazz == Boolean.class || clazz == Boolean.TYPE) ||
                                      (clazz == Byte.class || clazz == Byte.TYPE) ||
                                      (clazz == Long.class || clazz == Long.TYPE) ||
                                      (clazz == Character.class || clazz == Character.TYPE) ||
                                      (clazz == Float.class || clazz == Float.TYPE) ||
                                      (clazz == Double.class || clazz == Double.TYPE) ||
                                      (clazz == BigDecimal.class) ||
                                      (clazz == byte[].class) ||
                                      (clazz == String[].class) ||
                                      (clazz == int[].class) ||
                                      (clazz == Integer[].class) ||
                                      (clazz == double[].class) ||
                                      (clazz == Double[].class) ||
                                      (clazz == Object[].class);

        if (List.class.isAssignableFrom(clazz)) {
            isDirectlyEncodable = true;
        }
        else if (Set.class.isAssignableFrom(clazz)) {
            isDirectlyEncodable = true;
        }
        else if (Map.class.isAssignableFrom(clazz)) {
            isDirectlyEncodable = true;
        }
        else if (Enum.class.isAssignableFrom(clazz)) {
            isDirectlyEncodable = true;
        }

        return isDirectlyEncodable;
    }

    public <T> T toObject(Bag bag, Class<T> clazz) throws IllegalArgumentException, IllegalAccessException {

        logger.trace("Decoding instance of class '{}'", clazz.getName());

        T newInstance = null;
        if (bag.hasValue()) {
            newInstance = (T) bag.getValue();
        }
        else {

            byte nullByte = 1;// helper.readByte();
            if (nullByte == 0) {
                newInstance = null;
            }
            else if (nullByte == 1) {
                // if (isDirectlyEncodable(clazz)) {
                // newInstance = (T) directlyDecode(clazz, helper);
                // }
                // else {
                logger.trace("Instantiating new instance of class '{}'", clazz.getName());
                newInstance = ReflectionUtils.instantiate(clazz);

                Set<String> set = ignoredFieldsByClass.get(clazz);
                Field[] declaredFields = clazz.getDeclaredFields();
                for (Field field : declaredFields) {
                    if (isSerialisable(set, field)) {
                        field.setAccessible(true);
                        Class<?> fieldType = field.getType();
                        // Object fieldValue = decode(fieldType, helper);
                        // logger.trace("Decoded field '{}' type '{}' value '{}'",
                        // field.getName(), fieldType.getSimpleName(),
                        // fieldValue);

                        String name = field.getName();
                        Object value = bag.get(name);
                        applyToField(newInstance, field, value);
                    }
                }
                // }
            }
            // else if (nullByte == 2) {
            // newInstance = decode(helper);
            // }
            // else {
            // throw new
            // RuntimeException(String.format("Bad encoding, unknown null byte value "
            // + nullByte));
            // }
        }

        return newInstance;
    }

    private void applyToField(Object newInstance, Field field, Object value) throws IllegalArgumentException, IllegalAccessException {

        Class<?> type = field.getType();
        if (type == ObservableInteger.class) {
            value = new ObservableInteger(Integer.parseInt(value.toString()));
        }
        else if (type == ObservableLong.class) {
            value = new ObservableLong(Long.parseLong(value.toString()));
        }
        else if (type == ObservableDouble.class) {
            value = new ObservableDouble(Double.parseDouble(value.toString()));
        }
        else if (type == ObservableProperty.class) {
            value = new ObservableProperty<Object>(value);
        }

        field.set(newInstance, value);
    }

    private FactoryMapDecorator<Class<?>, Set<String>> ignoredFieldsByClass = new FactoryMapDecorator<Class<?>, Set<String>>(new HashMap<Class<?>, Set<String>>()) {
        @Override protected Set<String> createNewValue(Class<?> key) {
            return new HashSet<String>();
        }
    };

    private boolean isSerialisable(Set<String> ignoredFieldNames, Field field) {
        boolean isPersistable;

        if (Modifier.isStatic(field.getModifiers())) {
            isPersistable = false;
        }
        else if (ignoredFieldNames.contains(field.getName())) {
            isPersistable = false;
        }
        else if (field.getName().startsWith("this$")) {
            isPersistable = false;
        }
        else {
            isPersistable = true;
        }

        return isPersistable;

    }

    // public void encodeWithClass(Object object, Object helper) throws
    // IllegalArgumentException, IllegalAccessException {
    // String name;
    // if (object == null) {
    // // No real reason, it'll just work, but its crazy inefficient!
    // name = String.class.getName();
    // }
    // else {
    // name = object.getClass().getName();
    // }
    // helper.writeString(name);
    // encode(object, helper);
    // }
    //
    // @SuppressWarnings("unchecked") public <T> T decode(Object helper) throws
    // ClassNotFoundException, IllegalArgumentException, IllegalAccessException
    // {
    // String classname = helper.readString();
    // Class<?> clazz = Class.forName(classname);
    // logger.trace("Attempting to decode field of class '{}'", classname);
    // return (T) decode(clazz, helper);
    // }

}
