package com.logginghub.utils.xml;

import java.util.List;

public class SampleObject
{
    public String text;
    public Child child;
    public Child childField;
    public List<Child> children;

    @Override public boolean equals(Object object)
    {
        SampleObject other = (SampleObject) object;
        return (text == null || text.equals(other.text)) &&
               (child == null || child.equals(other.child)) &&
               (childField == null || childField.equals(other.childField)) &&
               (children == null || children.equals(other.children));
    }
}
