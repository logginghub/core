package com.logginghub.messaging2.encoding;

import java.util.Arrays;

public class NestedObject {

    private String topLevelString = "topLevelString";
    private AllTypesDummyObject nestedObject = new AllTypesDummyObject();
    private Object[] nestedObjectArray = new Object[] { new String("hello"), 234, new AllTypesDummyObject()};
    
    public Object[] getNestedObjectArray() {
        return nestedObjectArray;
    }
    
    public void setNestedObjectArray(Object[] nestedObjectArray) {
        this.nestedObjectArray = nestedObjectArray;
    }
    
    public AllTypesDummyObject getNestedObject() {
        return nestedObject;
    }
    
    public String getTopLevelString() {
        return topLevelString;
    }
    
    public void setNestedObject(AllTypesDummyObject nestedObject) {
        this.nestedObject = nestedObject;
    }
    
    public void setTopLevelString(String topLevelString) {
        this.topLevelString = topLevelString;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nestedObject == null) ? 0 : nestedObject.hashCode());
        result = prime * result + Arrays.hashCode(nestedObjectArray);
        result = prime * result + ((topLevelString == null) ? 0 : topLevelString.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        NestedObject other = (NestedObject) obj;
        if (nestedObject == null) {
            if (other.nestedObject != null) return false;
        }
        else if (!nestedObject.equals(other.nestedObject)) return false;
        if (!Arrays.equals(nestedObjectArray, other.nestedObjectArray)) return false;
        if (topLevelString == null) {
            if (other.topLevelString != null) return false;
        }
        else if (!topLevelString.equals(other.topLevelString)) return false;
        return true;
    }

    @Override public String toString() {
        return "NestedObject [topLevelString=" + topLevelString + ", nestedObject=" + nestedObject + ", nestedObjectArray=" + Arrays.toString(nestedObjectArray) + "]";
    }
    

    
    
}
