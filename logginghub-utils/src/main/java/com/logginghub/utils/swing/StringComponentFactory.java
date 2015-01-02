package com.logginghub.utils.swing;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class StringComponentFactory implements ComponentFactory<String>
{
    public JComponent buildComponent(String t)
    {
        return new JLabel(t);
    }
}
