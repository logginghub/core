package com.logginghub.logging.frontend.brainscan;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.miginfocom.swing.MigLayout;

import com.logginghub.swingutils.treetable.JTreeTable;
import com.logginghub.utils.observable.ObservableItemContainer;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservableListener;

public class MutlipleThreadTreeViewPanel extends JPanel {

    private Map<SingleThreadViewModel, SingleThreadViewPanel> panels = new HashMap<SingleThreadViewModel, SingleThreadViewPanel>();

    // private ThreadTreeModel2 threadTableModel;
    // private DefaultMutableTreeNode root;
    // private DefaultTreeModel treeModel;

    private ThreadTreeModel2 treeTableModel;

    private JTreeTable treeTable;

    private DefaultMutableTreeNode treeRoot;

    private DefaultTreeModel treeModel;

    private DefaultMutableTreeNode tableTreeRoot;

    private JTree tree;

    public MutlipleThreadTreeViewPanel() {
        setLayout(new MigLayout("", "[fill, grow]", "[fill, grow]"));

        treeRoot = new DefaultMutableTreeNode("root", true);
        treeRoot.add(new DefaultMutableTreeNode(""));

        tableTreeRoot = new DefaultMutableTreeNode("root", true);
        tableTreeRoot.add(new DefaultMutableTreeNode(""));

        treeModel = new DefaultTreeModel(treeRoot);
        treeTableModel = new ThreadTreeModel2(tableTreeRoot);

        tree = new JTree(treeModel);
        treeTable = new JTreeTable(treeTableModel);

        tree.setShowsRootHandles(true);
        treeTable.getTree().setShowsRootHandles(true);

        add(new JScrollPane(treeTable));
        // add(new JScrollPane(tree));

        // treeTable.getTree().setRootVisible(true);
        // treeTable.getTree().setShowsRootHandles(true);

        // threadTableModel = new ThreadTreeModel2();
        // threadTableModel.add(new ThreadViewModel("Asshat"));
        // threadTableModel.add(new ThreadViewModel("Fucktard"));
        // JTreeTable treeTable = new JTreeTable(threadTableModel);
        // treeTable.getTree().setShowsRootHandles(true);
        // add(new JScrollPane(treeTable));

    }

    public void bind(MutlipleThreadViewModel model) {

        model.getThreads().addListenerAndNotifyExisting(new ObservableListListener<SingleThreadViewModel>() {
            @Override public void onRemoved(SingleThreadViewModel t) {}

            @Override public void onCleared() {}

            @Override public void onAdded(final SingleThreadViewModel t) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        // SingleThreadViewPanel panel = new SingleThreadViewPanel();
                        // panel.bind(t);
                        // panels.put(t, panel);
                        // add(panel);
                        // validate();
                        final DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(t);

                        t.addListener(new ObservableListener() {
                            @Override public void onChanged(ObservableItemContainer observable, Object childPropertyThatChanged) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override public void run() {
                                        treeTableModel.nodeChanged(newChild);
                                        repaint();
                                    }
                                });
                            }
                        });

                        treeTableModel.insertNodeInto(newChild, tableTreeRoot, 0);
                        treeModel.insertNodeInto(newChild, treeRoot, 0);
                        repaint();
                        // threadTableModel.add(t);
                    }
                });

            }
        });
    }
}
