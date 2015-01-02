package com.logginghub.utils.swing;

import javax.swing.JComponent;

public interface ComponentFactory<T>
{
    public JComponent buildComponent(T t);
}
