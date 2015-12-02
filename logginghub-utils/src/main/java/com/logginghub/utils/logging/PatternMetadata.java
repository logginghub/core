package com.logginghub.utils.logging;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 07/10/15.
 */
public class PatternMetadata {

    private String pattern;
    private List<Type> types;

    public String getPattern() {
        return pattern;
    }

    public List<Type> getTypes() {
        return types;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setTypes(Type... types) {
        this.types = new ArrayList<Type>();
        for (Type type : types) {
            this.types.add(type);
        }
    }

    public void setTypes(List<Type> types) {
        this.types = types;
    }
}
