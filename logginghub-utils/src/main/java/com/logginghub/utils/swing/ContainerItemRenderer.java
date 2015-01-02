package com.logginghub.utils.swing;

import javax.swing.JComponent;

public interface ContainerItemRenderer<T> 
{
    public JComponent getRendererForItem(T item);
}
