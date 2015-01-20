package com.logginghub.messaging2.encoding.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.logginghub.messaging2.encoding.encodable.ReadBuffer;
import com.logginghub.messaging2.encoding.encodable.WriteBuffer;
import com.logginghub.utils.FactoryMapDecorator;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.logging.Logger;

public class ReflectionSerialiser {

    private static final Logger logger = Logger.getLoggerFor(ReflectionSerialiser.class);

    private FactoryMapDecorator<Class<?>, Set<String>> ignoredFieldsByClass = new FactoryMapDecorator<Class<?>, Set<String>>(new HashMap<Class<?>, Set<String>>()) {
        @Override protected Set<String> createNewValue(Class<?> key) {
            return new HashSet<String>();
        }
    };

    @SuppressWarnings("unchecked") public <T> T decode(Class<T> clazz, ReadBuffer helper) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {

        logger.trace("Decoding instance of class '{}'", clazz.getName());

        T newInstance;

        byte nullByte = helper.readByte();
        if (nullByte == 0) {
            newInstance = null;
        }
        else if (nullByte == 1) {
            if (isDirectlyEncodable(clazz)) {
                newInstance = (T) directlyDecode(clazz, helper);
            }
            else {
                logger.trace("Instantiating new instance of class '{}'", clazz.getName());
                newInstance = ReflectionUtils.instantiate(clazz);

                Set<String> set = ignoredFieldsByClass.get(clazz);
                Field[] declaredFields = clazz.getDeclaredFields();
                for (Field field : declaredFields) {
                    if (isSerialisable(set, field)) {
                        field.setAccessible(true);
                        Class<?> fieldType = field.getType();
                        Object fieldValue = decode(fieldType, helper);
                        logger.trace("Decoded field '{}' type '{}' value '{}'", field.getName(), fieldType.getSimpleName(), fieldValue);
                        field.set(newInstance, fieldValue);
                    }
                }
            }
        }
        else if (nullByte == 2) {
            newInstance = decode(helper);
        }
        else {
            throw new RuntimeException(String.format("Bad encoding, unknown null byte value " + nullByte));
        }

        return newInstance;
    }

    public void encode(Object object, WriteBuffer helper) throws IllegalArgumentException, IllegalAccessException {

        if (object == null) {
            helper.writeByte((byte) 0);
        }
        else {
            helper.writeByte((byte) 1);

            Class<?> clazz = object.getClass();

            if (isDirectlyEncodable(clazz)) {
                logger.trace("Directly encoding object '{}' of class '{}' into buffer {}", object, clazz, helper);
                directlyEncode(clazz, object, helper);
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

                        if (fieldInstance != null) {
                            Class<?> instanceType = fieldInstance.getClass();
                            if (fieldType != instanceType) {
                                
                                // TODO : there is a bug here - base types always come out as ambiguous :/
                                
                                // This is an ambiguous field, we need to encode
                                // the object with its class name
                                fieldType = instanceType;
                                isAmbiguous = true;
                            }
                        }

                        logger.trace("Encoding field '{}' type '{}' value '{}' to buffer {}...", field.getName(), fieldType.getSimpleName(), fieldInstance, helper);

                        // This is a little bit annoying as it doesn't recurse
                        // cleanly - due to the field type? Can't we just read
                        // that from the object anyway now we've got the null
                        // byte?
                        if (isDirectlyEncodable(fieldType)) {
                            if (fieldInstance == null) {
                                helper.writeByte((byte) 0);
                            }
                            else {
                                logger.trace("... directly encoding field");
                                if (isAmbiguous) {
                                    helper.writeByte((byte) 2);
                                    encodeWithClass(fieldInstance, helper);
                                }
                                else {
                                    helper.writeByte((byte) 1);
                                    directlyEncode(fieldType, fieldInstance, helper);
                                }
                            }
                        }
                        else {
                            if (isAmbiguous) {
                                helper.writeByte((byte) 2);
                                encodeWithClass(fieldInstance, helper);
                            }
                            else {
                                encode(fieldInstance, helper);
                            }
                        }
                    }
                }
            }

        }
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

    private void directlyEncode(Class<?> fieldType, Object fieldInstance, WriteBuffer helper) throws IllegalArgumentException, IllegalAccessException {

        if (fieldType == String.class) {
            helper.writeString((String) fieldInstance);
        }
        else if (fieldType == Date.class) {
            helper.writeDate((Date) fieldInstance);
        }
        else if (fieldType == Integer.class || fieldType == Integer.TYPE) {
            helper.writeInt((Integer) fieldInstance);
        }
        else if (fieldType == Short.class || fieldType == Short.TYPE) {
            helper.writeShort((Short) fieldInstance);
        }
        else if (fieldType == Boolean.class || fieldType == Boolean.TYPE) {
            helper.writeBoolean((Boolean) fieldInstance);
        }
        else if (fieldType == Byte.class || fieldType == Byte.TYPE) {
            helper.writeByte((Byte) fieldInstance);
        }
        else if (fieldType == Long.class || fieldType == Long.TYPE) {
            helper.writeLong((Long) fieldInstance);
        }
        else if (fieldType == Character.class || fieldType == Character.TYPE) {
            helper.writeChar((Character) fieldInstance);
        }
        else if (fieldType == Float.class || fieldType == Float.TYPE) {
            helper.writeFloat((Float) fieldInstance);
        }
        else if (fieldType == Double.class || fieldType == Double.TYPE) {
            helper.writeDouble((Double) fieldInstance);
        }
        else if (fieldType == BigDecimal.class) {
            helper.writeBigDecimal((BigDecimal) fieldInstance);
        }
        else if (fieldType == byte[].class) {
            helper.writeByteArray((byte[]) fieldInstance);
        }
        else if (fieldType == String[].class) {
            helper.writeStringArray((String[]) fieldInstance);
        }
        else if (fieldType == Object[].class) {
            encodeObjectArray((Object[]) fieldInstance, helper);
        }
        else {
            if (List.class.isAssignableFrom(fieldType)) {
                encodeObjectList((List<?>) fieldInstance, helper);
            }
            else if (Set.class.isAssignableFrom(fieldType)) {
                encodeObjectSet((Set<?>) fieldInstance, helper);
            }
            else if (Map.class.isAssignableFrom(fieldType)) {
                encodeObjectMap((Map<?, ?>) fieldInstance, helper);
            }
            else if (Enum.class.isAssignableFrom(fieldType)) {
                encodeEnum((Enum<?>) fieldInstance, helper);
            }
            else {
                throw new RuntimeException(String.format("Can't directly encode {}", fieldType.getName()));
            }
        }

    }

    private void encodeEnum(Enum<?> fieldInstance, WriteBuffer helper) throws IllegalArgumentException, IllegalAccessException {
        helper.writeString(fieldInstance.getClass().getName());
        helper.writeString(fieldInstance.name());
    }

    private void encodeObjectArray(Object[] fieldInstance, WriteBuffer helper) throws IllegalArgumentException, IllegalAccessException {
        helper.writeInt(fieldInstance.length);
        for (Object object : fieldInstance) {
            encodeWithClass(object, helper);
        }
    }

    private void encodeObjectList(List<?> fieldInstance, WriteBuffer helper) throws IllegalArgumentException, IllegalAccessException {
        helper.writeInt(fieldInstance.size());
        for (Object object : fieldInstance) {
            encodeWithClass(object, helper);
        }
    }

    private void encodeObjectSet(Set<?> fieldInstance, WriteBuffer helper) throws IllegalArgumentException, IllegalAccessException {
        helper.writeInt(fieldInstance.size());
        for (Object object : fieldInstance) {
            encodeWithClass(object, helper);
        }
    }

    private <K, V> void encodeObjectMap(Map<K, V> fieldInstance, WriteBuffer helper) throws IllegalArgumentException, IllegalAccessException {
        helper.writeInt(fieldInstance.size());

        Set<Entry<K, V>> entrySet = fieldInstance.entrySet();
        for (Entry<K, V> entry : entrySet) {
            encodeWithClass(entry.getKey(), helper);
            encodeWithClass(entry.getValue(), helper);
        }
    }

    private Object[] decodeObjectArray(ReadBuffer helper) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException {
        int length = helper.readInt();
        Object[] array = new Object[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = decode(helper);
        }
        return array;
    }

    private Enum decodeEnum(ReadBuffer helper) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException {
        String type = helper.readString();
        String name = helper.readString();

        Class forName = (Class) Class.forName(type);
        @SuppressWarnings("unchecked") Enum valueOf = Enum.valueOf(forName, name);
        return valueOf;
    }

    private List<?> decodeObjectList(ReadBuffer helper) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException {
        int length = helper.readInt();
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < length; i++) {
            list.add(decode(helper));
        }
        return list;
    }

    private Set<?> decodeObjectSet(ReadBuffer helper) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException {
        int length = helper.readInt();
        Set<Object> set = new HashSet<Object>();
        for (int i = 0; i < length; i++) {
            set.add(decode(helper));
        }
        return set;
    }

    private Map<?, ?> decodeObjectMap(ReadBuffer helper) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException {
        int length = helper.readInt();

        Map<Object, Object> map = new HashMap<Object, Object>();
        for (int i = 0; i < length; i++) {
            Object key = decode(helper);
            Object value = decode(helper);
            map.put(key, value);
        }

        return map;
    }

    private Object directlyDecode(Class<?> fieldType, ReadBuffer helper) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException {
        Object decoded;

        if (fieldType == String.class) {
            decoded = helper.readString();
        }
        else if (fieldType == Date.class) {
            decoded = helper.readDate();
        }
        else if (fieldType == Integer.class || fieldType == Integer.TYPE) {
            decoded = helper.readInt();
        }
        else if (fieldType == Short.class || fieldType == Short.TYPE) {
            decoded = helper.readShort();
        }
        else if (fieldType == Boolean.class || fieldType == Boolean.TYPE) {
            decoded = helper.readBoolean();
        }
        else if (fieldType == Byte.class || fieldType == Byte.TYPE) {
            decoded = helper.readByte();
        }
        else if (fieldType == Long.class || fieldType == Long.TYPE) {
            decoded = helper.readLong();
        }
        else if (fieldType == Character.class || fieldType == Character.TYPE) {
            decoded = helper.readChar();
        }
        else if (fieldType == Float.class || fieldType == Float.TYPE) {
            decoded = helper.readFloat();
        }
        else if (fieldType == Double.class || fieldType == Double.TYPE) {
            decoded = helper.readDouble();
        }
        else if (fieldType == BigDecimal.class) {
            decoded = helper.readBigDecimal();
        }
        else if (fieldType == byte[].class) {
            decoded = helper.readByteArray();
        }
        else if (fieldType == String[].class) {
            decoded = helper.readStringArray();
        }
        else if (fieldType == Object[].class) {
            decoded = decodeObjectArray(helper);
        }
        else {
            if (List.class.isAssignableFrom(fieldType)) {
                decoded = decodeObjectList(helper);
            }
            else if (Set.class.isAssignableFrom(fieldType)) {
                decoded = decodeObjectSet(helper);
            }
            else if (Map.class.isAssignableFrom(fieldType)) {
                decoded = decodeObjectMap(helper);
            }
            else if (Enum.class.isAssignableFrom(fieldType)) {
                decoded = decodeEnum(helper);
            }
            else {
                throw new RuntimeException(String.format("Can't directly encode {}", fieldType.getName()));
            }
        }

        return decoded;
    }

    private boolean isSerialisable(Set<String> ignoredFieldNames, Field field) {
        boolean isPersistable;

        if (Modifier.isStatic(field.getModifiers())) {
            isPersistable = false;
        }
        else if (ignoredFieldNames.contains(field.getName())) {
            isPersistable = false;
        }
        else {
            isPersistable = true;
        }

        return isPersistable;

    }

    public void encodeWithClass(Object object, WriteBuffer helper) throws IllegalArgumentException, IllegalAccessException {
        String name;
        if (object == null) {
            // No real reason, it'll just work, but its crazy inefficient!
            name = String.class.getName();
        }
        else {
            name = object.getClass().getName();
        }
        helper.writeString(name);
        encode(object, helper);
    }

    @SuppressWarnings("unchecked") public <T> T decode(ReadBuffer helper) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException {
        String classname = helper.readString();
        Class<?> clazz = Class.forName(classname);
        logger.trace("Attempting to decode field of class '{}'", classname);
        return (T) decode(clazz, helper);
    }

}
