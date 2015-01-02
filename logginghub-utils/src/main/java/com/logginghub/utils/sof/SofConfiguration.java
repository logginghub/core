package com.logginghub.utils.sof;

import java.util.HashMap;
import java.util.Map;

import com.logginghub.utils.logging.Logger;

public class SofConfiguration {

    private static final Logger logger = Logger.getLoggerFor(SofConfiguration.class);
    private Map<Class<? extends SerialisableObject>, Integer> typeIDByClass = new HashMap<Class<? extends SerialisableObject>, Integer>();
    private Map<Integer, Class<? extends SerialisableObject>> classByTypeID = new HashMap<Integer, Class<? extends SerialisableObject>>();

    private Object lock = new Object();
    private boolean compressed;
    private boolean allowUnknownNestedTypes = false;
    private boolean lazyDecodeOfNestedTypes = false;
    private boolean microFormat = false;

    public void registerType(Class<? extends SerialisableObject> clazz, int id) {

        synchronized (lock) {

            Integer oldID = typeIDByClass.put(clazz, id);
            Class<? extends SerialisableObject> oldClass = classByTypeID.put(id, clazz);

            if (oldID != null && !oldID.equals(id)) {
                logger.warning("Class '{}' has been re-registered with a different ID. New ID is '{}' and the old one was '{}'",
                               clazz.getName(),
                               id,
                               oldID);
            }

            if (oldClass != null && oldClass != clazz) {
                throw new SofRuntimeException("ID '{}' has been re-registered with a different class. New class is '{}' and the old one was '{}'",
                                       id,
                                       clazz.getName(),
                                       oldClass.getName());
            }
        }

    }

    public void unregisterType(int typeID) {
        synchronized (lock) {
            Class<? extends SerialisableObject> clazz = classByTypeID.remove(typeID);
            if (clazz != null) {
                typeIDByClass.remove(clazz);
            }
        }
    }

    public Class<? extends SerialisableObject> resolve(int typeID) {
        return classByTypeID.get(typeID);
    }

    public Integer resolve(Class<? extends SerialisableObject> clazz) {
        return typeIDByClass.get(clazz);
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setAllowUnknownNestedTypes(boolean allowUnknownNestedTypes) {
        this.allowUnknownNestedTypes = allowUnknownNestedTypes;
    }

    public boolean isAllowUnknownNestedTypes() {
        return allowUnknownNestedTypes;
    }

    public void setLazyDecodeOfNestedTypes(boolean lazyDecodeOfNestedTypes) {
        this.lazyDecodeOfNestedTypes = lazyDecodeOfNestedTypes;
    }

    public boolean isLazyDecodeOfNestedTypes() {
        return lazyDecodeOfNestedTypes;
    }

    public String resolveField(int fieldType) {

        String resolved;
        if (fieldType < 0) {
            resolved = DefaultSofWriter.resolveField(fieldType);
        }
        else {
            Class<? extends SerialisableObject> resolvedClass = resolve(fieldType);
            if (resolvedClass == null) {
                resolved = "<Unknown class>" + fieldType;
            }
            else {
                resolved = resolvedClass.getName();
            }
        }

        return resolved;

    }

    public void setMicroFormat(boolean microFormat) {
        this.microFormat = microFormat;
    }

    public boolean isMicroFormat() {
        return microFormat;
    }

}
