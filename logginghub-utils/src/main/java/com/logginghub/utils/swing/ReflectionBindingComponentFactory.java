package com.logginghub.utils.swing;

import java.lang.reflect.Field;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class ReflectionBindingComponentFactory<T> implements
                ComponentFactory<T>
{
    public ReflectionBindingComponentFactory(T t, String fieldName)
    {
        Class<? extends Object> c = t.getClass();

        try
        {
            Field field = c.getField(fieldName);
            
        }
        catch (Exception e)
        {

        }

        /*
         * Field[] fields = c.getFields();
         * 
         * for (Field field : fields) { if(field.getType() == String.class) {
         * 
         * } }
         */
    }

    public JComponent buildComponent(T t)
    {
        JPanel panel = new JPanel();

        return panel;
    }
}
