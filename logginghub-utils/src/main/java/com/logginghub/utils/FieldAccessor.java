package com.logginghub.utils;

import java.lang.reflect.Field;

public class FieldAccessor {

    private Field field;
    private Object object;

    public FieldAccessor(Field field, Object object) {
        this.field = field;
        this.object = object;
    }
    
    public String getName() {
        return field.getName();
    }
    
    public Class<?> getType() {
        return field.getType();
    }

    public Field getField() {
        return field;
    }
    
    public Object getObject() {
        return object;
    }
    
    public void set(Object fieldValue) {
        boolean isAccessible = field.isAccessible();
        try {
            if (!isAccessible) {
                field.setAccessible(true);
            }
            field.set(object, fieldValue);
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException(String.format("Failed to set field %s", field), e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Failed to set field %s", field), e);
        }
        finally {
            if (!isAccessible) {
                field.setAccessible(false);
            }
        }
    }
    
    @SuppressWarnings("unchecked") public <T> T get() {
        boolean isAccessible = field.isAccessible();
        try {
            if (!isAccessible) {
                field.setAccessible(true);
            }
            return (T) field.get(object);
        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException(String.format("Failed to get from field %s", field), e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Failed to get from field %s", field), e);
        }
        finally {
            if (!isAccessible) {
                field.setAccessible(false);
            }
        }
    }

    public String toString() {
        return field.getName();
    }
    
}
