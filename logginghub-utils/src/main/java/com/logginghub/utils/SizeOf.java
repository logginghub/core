package com.logginghub.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.logginghub.utils.logging.Logger;

public class SizeOf {

    private static final Logger logger = Logger.getLoggerFor(SizeOf.class);

    public static final int instanceSize = 8;
    public static final int intSize = 4;
    public static final int charSize = 2;
    public static final int pointerSize = 4;
    public static final int longSize = 8;
    public static final long booleanSize = 1;
    public static final long doubleSize = 8;
    public static final long floatSize = 4;
    public static final long byteSize = 1;
    public static long shortSize = 2;

    public static long sizeOf(String string) {

        long size = 4;

        if (string != null) {

            // Object size
            size = instanceSize;

            // Fields
            size += 4 * intSize;

            long charLength = roundUp(8 + 4 + string.length() * 2);
            size += charLength;

            size = roundUp(size);
        }

        return size;

    }

    public static final long roundUp(long size) {
        if (size % instanceSize == 0) {
            return size;
        }
        return ((size / instanceSize) + 1) * instanceSize;
    }

    public static long sizeof(Object object) {
        return sizeof(object.getClass(), object);
    }

    public static long sizeof(Class objectType, Object object) {

        long size = 0;

        if (ReflectionUtils.isPrimativeType(objectType)) {
            logger.trace("Object {} is a primative type [{}]", object, objectType.getSimpleName());
            size += sizeofPrimative(objectType, object);
        }
        else {
            logger.trace("Object {} is not primative - {}", object, objectType.getSimpleName());
            if (object != null) {
                size += sizeofObject(object);
            }

            size = roundUp(size);
            logger.trace("[{}] rounded up to the nearest 8 bytes", size);
        }

        return size;
    }

    private static long sizeofObject(Object object) {

        long size = instanceSize;
        logger.trace("[{}] +{} bytes for the instance", size, instanceSize);

        Class<? extends Object> clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Class<?> fieldType = field.getType();
            if (!Modifier.isStatic(field.getModifiers())) {

                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(object);

                    if (ReflectionUtils.isPrimativeType(fieldType)) {
                        long primativeSize = sizeofPrimative(fieldType, fieldValue);
                        logger.trace("The field '{}' is an {} (+{} bytes)", field.getName(), fieldType.getSimpleName(), primativeSize);
                        size += primativeSize;
                    }
                    else {
                        logger.trace("Field {} is an object type [{}]", field.getName(), fieldType.getSimpleName());
                        size += pointerSize;
                        logger.trace("[{}] +{} bytes for object pointer", size, pointerSize);

                        logger.moreIndent();
                        size += sizeof(fieldType, fieldValue);
                        logger.lessOutdent();
                    }
                }

                catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return size;

    }

    private static long sizeofPrimative(Class<?> objectType, Object object) {
        long size = 0;
        if (objectType == char[].class) {
            logger.moreIndent();
            // logger.trace("The object is a char[]");
            size += sizeofCharArray((char[]) object);
            logger.lessOutdent();
        }
        else if (objectType == int[].class) {
            int[] intarray = (int[]) object;
            size += roundUp(pointerSize + instanceSize + (intarray.length * intSize));
            logger.trace("The object is an int[] (roundUp(8 + 4 + (4 * {})) = {}", intarray.length, size);
        }
        else if (objectType == Integer.TYPE) {
            size += intSize;
            // logger.trace("[{}] The object '{}' is an int (+{} bytes)", size,
            // object, intSize);
        }
        else if (objectType == Boolean.TYPE) {
            size += booleanSize;
            // logger.trace("[{}] The object '{}' is a boolean (+{} bytes)",
            // size, object, booleanSize);
        }
        else if (objectType == Short.TYPE) {
            size += shortSize;
            // logger.trace("[{}] The object '{}' is a short (+{} bytes)", size,
            // object, shortSize);
        }
        else if (objectType == Byte.TYPE) {
            size += byteSize;
            // logger.trace("[{}] The object '{}' is a byte (+{} bytes)", size,
            // object, byteSize);
        }
        else if (objectType == Long.TYPE) {
            size += longSize;
            // logger.trace("[{}] The object '{}' is a long (+{} bytes)", size,
            // object, longSize);
        }
        else if (objectType == Float.TYPE) {
            size += floatSize;
            // logger.trace("[{}] The object '{}' is a float (+{} bytes)", size,
            // object, floatSize);
        }
        else if (objectType == Double.TYPE) {
            size += doubleSize;
            // logger.trace("[{}] The object '{}' is a double (+{} bytes)",
            // size, object, doubleSize);
        }
        return size;
    }

    private static long sizeofCharArray(char[] charArray) {

        long size = instanceSize;
        logger.trace("[{}] +{} bytes for the instance", size, instanceSize);

        // The length
        size += intSize;
        logger.trace("[{}] +{} bytes for the length", size, intSize);

        // The multi-byte chars in the array
        size += charSize * charArray.length;
        logger.trace("[{}] +{} bytes * {} for the elements", size, charSize, charArray.length);

        size = roundUp(size);
        logger.trace("[{}] rounded up to the nearest 8 bytes", size);

        return size;

    }

    public static void main(String[] args) {
        System.out.println(sizeof("<xml></xml>"));
    }

}
