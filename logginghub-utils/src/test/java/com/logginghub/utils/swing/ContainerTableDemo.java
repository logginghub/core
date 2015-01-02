package com.logginghub.utils.swing;

import java.util.Random;

import javax.swing.JScrollPane;

import com.logginghub.utils.container.ListContainer;
import com.logginghub.utils.swing.ContainerTable;
import com.logginghub.utils.swing.ContainerTableModel;
import com.logginghub.utils.swing.TestFrame;

public class ContainerTableDemo
{
    static class Item
    {
        String text;
        int number;

        public Item(String text, int number)
        {
            super();
            this.text = text;
            this.number = number;
        }
    }

    public static void main(String[] args)
    {
        ListContainer<Item> container = new ListContainer<Item>();
        int count = 200;
        Random random = new Random();
        for (int i = 0; i < count; i++)
        {
            container.add(new Item("Foo", random.nextInt(count)));
            container.add(new Item("Moo", random.nextInt(count)));
            container.add(new Item("Swing", random.nextInt(count)));
            container.add(new Item("Life", random.nextInt(count)));
        }

        ContainerTableModel<Item> model = new ContainerTableModel<Item>(container)
        {
            String[] columns = new String[] { "String", "Int" };

            @Override public int getColumnCount()
            {
                return 2;
            }

            @Override public String getColumnName(int columnIndex)
            {
                return columns[columnIndex];
            }

            @Override protected Object getColumnValue(Item item, int column)
            {
                if (column == 0) return item.text;
                else return item.number;
            }          
        };

        ContainerTable<Item> table = new ContainerTable<Item>(model);

        TestFrame.show(new JScrollPane(table));
    }
}
