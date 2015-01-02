package com.logginghub.utils.enumerator;

import java.util.ArrayList;
import java.util.List;

public class Package
{
    private List<Package> m_children = new ArrayList<Package>();
    private List<Class<?>> m_classes = new ArrayList<Class<?>>();
    private String m_name;

    public Package(String name, Package parent)
    {
        m_name = name;
    }

    public Package()
    {
        // root package
    }

    public String getName()
    {
        return m_name;
    }

    public Package addChild(String name)
    {
        Package child = new Package(name, this);
        m_children.add(child);
        return child;
    }

    public void addClass(Class<?> forName)
    {
        m_classes.add(forName);
    }

    public List<Class<?>> getClasses()
    {
        return m_classes;
    }
    
    public List<Package> getChildren()
    {
        return m_children;
    }
}
