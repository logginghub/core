package com.logginghub.utils.swing;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.logginghub.utils.container.ListContainer;
import com.logginghub.utils.swing.ComponentFactory;
import com.logginghub.utils.swing.ContainerItemRenderer;
import com.logginghub.utils.swing.ContainerList;
import com.logginghub.utils.swing.TestFrame;

public class ContainerListDemo
{
    public static void main(String[] args)
    {
        ComponentFactory<String> factory = new ComponentFactory<String>()
        {
            public JComponent buildComponent(String t)
            {
                return new JLabel(t);
            }
        };

        ListContainer<String> container = new ListContainer<String>();
        int count = 200;
        for (int i = 0; i < count; i++)
        {
            container.add("Foo");
            container.add("Moo");
            container.add("Swing");
            container.add("Life");
        }

        ContainerItemRenderer<String> renderer = new ContainerItemRenderer<String>()
        {
            JLabel label = new JLabel();

            public JComponent getRendererForItem(String item)
            {
                label.setText(item);
                return label;
            }
        };

        ContainerList<String> list = new ContainerList<String>(container,
                                                               renderer);
        TestFrame.show(new JScrollPane(list));
    }
}
