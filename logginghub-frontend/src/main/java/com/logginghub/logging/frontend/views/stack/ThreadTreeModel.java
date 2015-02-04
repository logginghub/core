package com.logginghub.logging.frontend.views.stack;

/*
 * %W% %E%
 *
 * Copyright 1997, 1998 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer. 
 *   
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution. 
 *   
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.  
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,   
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS 
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.logginghub.swingutils.treetable.AbstractTreeTableModel;
import com.logginghub.swingutils.treetable.TreeTableModel;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.observable.ObservableItemContainer;
import com.logginghub.utils.observable.ObservableListener;

/**
 * FileSystemModel is a TreeTableModel representing a hierarchical file system. Nodes in the
 * FileSystemModel are FileNodes which, when they are directory nodes, cache their children to avoid
 * repeatedly querying the real file system.
 * 
 * @version %I% %G%
 * 
 * @author Philip Milne
 * @author Scott Violet
 */

public class ThreadTreeModel extends AbstractTreeTableModel implements TreeTableModel {

    static protected String[] cNames = { "Name", "State", "Top stack" };
    static protected Class[] cTypes = { TreeTableModel.class, String.class, String.class, String.class };

    public final static int column_name = 0;
    public final static int column_state = 1;
    public final static int column_top = 2;

    private ThreadTreeNode rootNode;

    public ThreadTreeModel() {
        super(new ThreadTreeNode(null));
        rootNode = (ThreadTreeNode) getRoot();
    }

    public void add(SingleThreadViewModel t) {
        final ThreadTreeNode node = rootNode.createChild(t);
        final int index = rootNode.getChildren().indexOf(node);
        
        t.addListener(new ObservableListener() {
            @Override public void onChanged(ObservableItemContainer observable, Object childPropertyThatChanged) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        fireTreeNodesChanged(this, new Object[] { root }, new int[] { index }, new Object[] { node });
                    }
                });
            }
        });

        fireTreeNodesInserted(this, new Object[] { rootNode }, new int[] { index }, new Object[] { node });        
    }

    protected Object[] getChildren(Object node) {
        ThreadTreeNode fileNode = ((ThreadTreeNode) node);
        return fileNode.getChildren().toArray(new Object[fileNode.getChildren().size()]);
    }

    public int getChildCount(Object node) {
        Object[] children = getChildren(node);
        return (children == null) ? 0 : children.length;
    }

    public Object getChild(Object node, int i) {
        return getChildren(node)[i];
    }

    // The superclass's implementation would work, but this is more efficient.
    public boolean isLeaf(Object node) {
        ThreadTreeNode treeNode = (ThreadTreeNode) node;
        boolean isLeaf = treeNode.getChildren().size() == 0;
        return isLeaf;
    }

    //
    // The TreeTableNode interface.
    //

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
        ThreadTreeNode threadNode = (ThreadTreeNode) node;
        SingleThreadViewModel model = threadNode.getModel();
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

        return null;
    }

}

class ThreadTreeNode {

    private SingleThreadViewModel model;
    private List<ThreadTreeNode> children = new ArrayList<ThreadTreeNode>();

    public ThreadTreeNode(SingleThreadViewModel model) {
        this.model = model;
    }

    public ThreadTreeNode createChild(SingleThreadViewModel t) {
        ThreadTreeNode childNode = new ThreadTreeNode(t);
        children.add(childNode);
        return childNode;
    }

    /**
     * Returns the the string to be used to display this leaf in the JTree.
     */
    public String toString() {
        if (model == null) {
            return "root";
        }
        else {
            return model.getName().get();
        }
    }

    public SingleThreadViewModel getModel() {
        return model;
    }

    public List<ThreadTreeNode> getChildren() {
        return children;
    }
}
