package matt;

import net.miginfocom.swing.MigLayout;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

public class JTreeFiltering {

    private DefaultMutableTreeNode visibleTreeRoot;
    private JTextField searchText;
    private DefaultMutableTreeNode invisibleTreeRoot;
    private JTree visibleTree;
    private DefaultTreeModel visibleTreeModel;

    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        JTreeFiltering jTreeFiltering = new JTreeFiltering();
        jTreeFiltering.show();
    }

    private void addOptions(final DefaultMutableTreeNode invisibleTreeRoot, JCheckBox options1, JCheckBox options2, JCheckBox options3) {
        options1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (nodeExists(invisibleTreeRoot, "FTSE 100")) {
                    removeNode(invisibleTreeRoot, "FTSE 100");
                } else {
                    DefaultMutableTreeNode options = new DefaultMutableTreeNode("FTSE 100");

                    options.add(new DefaultMutableTreeNode("BT"));
                    options.add(new DefaultMutableTreeNode("MKS"));
                    options.add(new DefaultMutableTreeNode("LLOY"));

                    invisibleTreeRoot.add(options);
                }
                updateFilters();
            }
        });

        options2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (nodeExists(invisibleTreeRoot, "DAX 30")) {
                    removeNode(invisibleTreeRoot, "DAX 30");
                } else {
                    DefaultMutableTreeNode options = new DefaultMutableTreeNode("DAX 30");

                    options.add(new DefaultMutableTreeNode("BASF"));
                    options.add(new DefaultMutableTreeNode("BMW"));
                    options.add(new DefaultMutableTreeNode("SAP"));

                    invisibleTreeRoot.add(options);
                }
                updateFilters();
            }
        });

        options3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (nodeExists(invisibleTreeRoot, "DOW 100")) {
                    removeNode(invisibleTreeRoot, "DOW 100");
                } else {
                    DefaultMutableTreeNode options = new DefaultMutableTreeNode("DOW 100");

                    options.add(new DefaultMutableTreeNode("GE"));
                    options.add(new DefaultMutableTreeNode("CS"));
                    options.add(new DefaultMutableTreeNode("XOM"));

                    invisibleTreeRoot.add(options);
                }
                updateFilters();
            }
        });
    }

    private DefaultMutableTreeNode createAllParentNodes(DefaultMutableTreeNode invisibleNode, DefaultMutableTreeNode visibleNodeRoot) {

        // This has the complete path of parent nodes back to the root node
        TreeNode[] path = invisibleNode.getPath();

        // We don't want to create the root node again, so trim that from the array
        TreeNode[] pathWithoutRoot = new TreeNode[path.length - 1];
        System.arraycopy(path, 1, pathWithoutRoot, 0, pathWithoutRoot.length);

        // We don't want to create the leaf node either, so trim that as well
        TreeNode[] pathWithoutRootOrLeaf = new TreeNode[pathWithoutRoot.length - 1];
        System.arraycopy(pathWithoutRoot, 0, pathWithoutRootOrLeaf, 0, pathWithoutRootOrLeaf.length);

        // Create a pointer than will walk through the visible nodes as we go
        DefaultMutableTreeNode visibleNodePointer = visibleNodeRoot;

        for (TreeNode treeNode : pathWithoutRootOrLeaf) {

            // Convert to your custom node etc etc
            String nodeText = treeNode.toString();

            int index = indexOfNode(visibleNodePointer, nodeText);
            if (index == -1) {
                DefaultMutableTreeNode missingNode = new DefaultMutableTreeNode(nodeText);
                visibleNodePointer.add(missingNode);

                // Move the pointer forward to this new node
                visibleNodePointer = missingNode;
            } else {
                // This node aleady exists, move the pointer forward to it
                visibleNodePointer = (DefaultMutableTreeNode) visibleNodePointer.getChildAt(index);
            }

        }

        // By the end of that loop, the pointer will be pointing to the correct node in which to add our new item
        return visibleNodePointer;

    }

    private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }

        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    private int indexOfNode(DefaultMutableTreeNode node, String s) {
        int index = -1;
        for (int i = 0; i < node.getChildCount() && index == -1; i++) {
            // Replace this with a cast to your custom node type and have a look at the actual values
            if (node.getChildAt(i).toString().equals(s)) {
                index = i;
            }
        }
        return index;
    }

    private boolean nodeExists(DefaultMutableTreeNode node, String s) {
        return indexOfNode(node, s) != -1;
    }

    private void recursivelySearchNodes(String filter, DefaultMutableTreeNode invisibleNodeParent, DefaultMutableTreeNode visibleNodeRoot) {

        DefaultMutableTreeNode visibleNode = null;

        for (int i = 0; i < invisibleNodeParent.getChildCount(); i++) {

            DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) invisibleNodeParent.getChildAt(i);

            // Replace this with a cast to your custom node type to access specific fields
            String childText = childAt.toString();

            if (filter.isEmpty() || childText.contains(filter)) {

                // This is a match
                if (visibleNode == null) {
                    // We need to create a visible node if at least one of the child nodes match. Note that in a complex tree this might require us to go and create a whole load of parent nodes
                    visibleNode = createAllParentNodes(childAt, visibleNodeRoot);
                }

                // Replace the DefaultMutableTreeNode with your custom node, copy over the appropriate fields from the childAt node
                visibleNode.add(new DefaultMutableTreeNode(childText));

                // Quick shortcut to add all the children for this node too - this is optional
                recursivelySearchNodes("", childAt, visibleNodeRoot);

            } else {
                recursivelySearchNodes(filter, childAt, visibleNodeRoot);
            }

        }
    }

    private void removeNode(DefaultMutableTreeNode node, String s) {
        node.remove(indexOfNode(node, s));
    }

    private void show() throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {

                visibleTreeRoot = new DefaultMutableTreeNode("Visible root");
                invisibleTreeRoot = new DefaultMutableTreeNode("Invisible root");

                visibleTreeModel = new DefaultTreeModel(visibleTreeRoot);

                visibleTree = new JTree(visibleTreeModel);

                searchText = new JTextField();
                searchText.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        updateFilters();
                    }
                });

                JLabel searchTextLabel = new JLabel("Search");

                JCheckBox options1 = new JCheckBox("Options 1");
                JCheckBox options2 = new JCheckBox("Options 2");
                JCheckBox options3 = new JCheckBox("Options 3");

                addOptions(invisibleTreeRoot, options1, options2, options3);

                MigLayout layout = new MigLayout("fill", "[fill, grow]", "[fill][fill][fill, grow]");
                JPanel panel = new JPanel(layout);

                panel.add(options1, "cell 0 0");
                panel.add(options2, "cell 0 0");
                panel.add(options3, "cell 0 0");

                panel.add(searchTextLabel, "cell 0 1");
                panel.add(searchText, "cell 0 1, grow");

                panel.add(new JScrollPane(visibleTree), "cell 0 2");

                JFrame frame = new JFrame("JTreeFiltering");
                frame.setSize(800, 800);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(panel);
                frame.setVisible(true);
            }


        });

    }

    private void updateFilters() {
        String filter = searchText.getText();
        System.out.println(filter);

        visibleTreeRoot.removeAllChildren();

        recursivelySearchNodes(filter, invisibleTreeRoot, visibleTreeRoot);

        // This will notify the view to reload the data based on the model changes
        visibleTreeModel.reload();
        expandAllNodes(visibleTree, 0, visibleTree.getRowCount());


    }
}
