package com.logginghub.utils.data;

import java.util.HashMap;
import java.util.Map;

public class SerialisedObject {

    private Map<Object, SerialisedField> fields = new HashMap<Object, SerialisedField>();

    public void addField(int field, int type, Object value) {
        fields.put(field, new SerialisedField(type, value));
    }

    public SerialisedField getField(Object field) {
        SerialisedField serialisedField = fields.get(field);
        return serialisedField;
    }

}
