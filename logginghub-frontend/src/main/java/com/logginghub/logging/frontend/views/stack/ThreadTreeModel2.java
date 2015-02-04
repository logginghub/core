package com.logginghub.logging.frontend.views.stack;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.logginghub.swingutils.treetable.TreeTableModel;
import com.logginghub.utils.StringUtils;

public class ThreadTreeModel2 extends DefaultTreeModel implements TreeTableModel {

    static protected String[] cNames = { "Name", "State", "Top stack" };
    static protected Class[] cTypes = { TreeTableModel.class, String.class, String.class, String.class };

    public final static int column_name = 0;
    public final static int column_state = 1;
    public final static int column_top = 2;

    public ThreadTreeModel2(DefaultMutableTreeNode root) {
        super(root);
    }

    public int getColumnCount() {
        return cNames.length;
    }

    public String getColumnName(int column) {
        return cNames[column];
    }

    public Class getColumnClass(int column) {
        return cTypes[column];
    }

    public Object getValueAt(Object node, int column) {
        DefaultMutableTreeNode threadNode = (DefaultMutableTreeNode) node;
        Object userObject = threadNode.getUserObject();
        if (userObject instanceof SingleThreadViewModel) {
            SingleThreadViewModel model = (SingleThreadViewModel) userObject;
            if (model != null) {
                switch (column) {
                    case column_name:
                        return model.getName().get();
                    case column_state:
                        return model.getState().get();
                    case column_top:
                        return StringUtils.firstLine(model.getStack().get());
                }
            }
        }

        return null;
    }

    @Override public boolean isCellEditable(Object node, int column) {
        return false;

    }

    @Override public void setValueAt(Object aValue, Object node, int column) {}

}
