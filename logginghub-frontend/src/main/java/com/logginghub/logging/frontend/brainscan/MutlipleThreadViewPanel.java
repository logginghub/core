package com.logginghub.logging.frontend.brainscan;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.messages.StackStrobeRequest;
import com.logginghub.swingutils.MigPanel;
import com.logginghub.utils.DelayedAction;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.WildcardOrRegexMatcher;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;

public class MutlipleThreadViewPanel extends JPanel {

    // private Map<String, Column> visibleColumnMap = new HashMap<String,
    // MutlipleThreadViewPanel.Column>();;
    private List<Column> visibleColumns = new ArrayList<Column>();

    private Map<String, Column> allColumnMap = new HashMap<String, MutlipleThreadViewPanel.Column>();;
    private List<Column> allColumns = new ArrayList<Column>();

    private DelayedAction filterDelay = new DelayedAction(50, TimeUnit.MILLISECONDS);
    private JTextField instanceFilter;
    private JCheckBox instanceFilterRegexCheckBox;
    private MigPanel scrollerPanel;
    private JTextField stackFilter;
    private JCheckBox stackFilterRegexCheckBox;

    private static final Font tahoma12Bold = new Font("Tahoma", Font.BOLD, 12);
    private JButton refreshButton;

    private BrainScanController controller;

    private JButton clearButton;

    private class Cell {
        String name;
        SingleThreadViewPanel panel = new SingleThreadViewPanel();
    }

    private class Column {
        List<Cell> cells = new ArrayList<Cell>();
        String name;
    }

    class PanelMap extends HashMap<SingleThreadViewModel, SingleThreadViewPanel> {}

    public MutlipleThreadViewPanel() {
        setLayout(new MigLayout("insets 1", "[grow,fill]", "[top][grow,fill]"));

        JPanel filterPane = new JPanel();
        filterPane.setLayout(new MigLayout("", "[][grow,fill][]", "[][][fill]"));

        instanceFilter = new JTextField("*.*.*.*");
        filterPane.add(new JLabel("Instance Filter"), "cell 0 0");
        filterPane.add(instanceFilter, "cell 1 0, grow");
        instanceFilterRegexCheckBox = new JCheckBox("Regex", false);
        filterPane.add(instanceFilterRegexCheckBox, "cell 2 0");

        stackFilter = new JTextField("*");
        filterPane.add(new JLabel("Stack Filter"), "cell 0 1");
        filterPane.add(stackFilter, "cell 1 1, grow");
        stackFilterRegexCheckBox = new JCheckBox("Regex", false);
        filterPane.add(stackFilterRegexCheckBox, "cell 2 1");

        stackFilterRegexCheckBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                runFilter();
            }
        });

        instanceFilterRegexCheckBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                runFilter();
            }
        });

        instanceFilter.addKeyListener(new KeyListener() {
            @Override public void keyPressed(KeyEvent e) {}

            @Override public void keyReleased(KeyEvent e) {}

            @Override public void keyTyped(KeyEvent e) {
                filterDelay.execute(new Runnable() {
                    @Override public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                runFilter();
                            }
                        });
                    }
                });
            }
        });

        stackFilter.addKeyListener(new KeyListener() {
            @Override public void keyPressed(KeyEvent e) {}

            @Override public void keyReleased(KeyEvent e) {}

            @Override public void keyTyped(KeyEvent e) {
                filterDelay.execute(new Runnable() {
                    @Override public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                runFilter();
                            }
                        });
                    }
                });
            }
        });

        add(filterPane, "top");

        JPanel buttonPane = new JPanel(new MigLayout());
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.sendStrobeRequest(new StackStrobeRequest("*", 1, 0));
            }
        });
        buttonPane.add(refreshButton, "cell 0 0");

        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        clearData();
                    }

                    
                });
            }
        });
        buttonPane.add(clearButton, "cell 1 0");
        filterPane.add(buttonPane, "cell 0 3");
        filterPane.setBorder(BorderFactory.createTitledBorder("Filtering"));

        scrollerPanel = new MigPanel();
        scrollerPanel.setBackground(Color.WHITE);
        add(new JScrollPane(scrollerPanel), "cell 0 1");
    }

    public void bind(BrainScanController controller, ObservableList<MutlipleThreadViewModel> viewList) {

        this.controller = controller;

        viewList.addListenerAndNotifyExisting(new ObservableListListener<MutlipleThreadViewModel>() {

            @Override public void onAdded(MutlipleThreadViewModel t) {
                t.getThreads().addListenerAndNotifyExisting(new ObservableListListener<SingleThreadViewModel>() {
                    @Override public void onAdded(final SingleThreadViewModel t) {

                        String key = StringUtils.format("{}.{}.{}.{}", t.getEnvironment(), t.getHost(), t.getInstanceType(), t.getInstanceNumber());

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                updateModel(t);
                                updateLayout();
                                repaint();
                            }

                        });
                    }

                    @Override public void onCleared() {}

                    @Override public void onRemoved(SingleThreadViewModel t) {}

                });
            }

            @Override public void onCleared() {}

            @Override public void onRemoved(MutlipleThreadViewModel t) {}
        });

    }
    
    private void clearData() {
        controller.clearData();
        allColumnMap.clear();
        allColumns.clear();
        visibleColumns.clear();
        updateLayout();
    }

    private void runFilter() {

        visibleColumns.clear();

        WildcardOrRegexMatcher stackMatcher = null;
        WildcardOrRegexMatcher instanceMatcher = null;

        boolean badFilters = false;

        try {
            instanceMatcher = new WildcardOrRegexMatcher(instanceFilter.getText(), instanceFilterRegexCheckBox.isSelected());
            instanceFilter.setBackground(Color.white);
        }
        catch (PatternSyntaxException pse) {
            instanceFilter.setBackground(Color.red);
            badFilters = true;
        }

        try {
            stackMatcher = new WildcardOrRegexMatcher(stackFilter.getText(), stackFilterRegexCheckBox.isSelected());
            stackFilter.setBackground(Color.white);
        }
        catch (PatternSyntaxException pse) {
            stackFilter.setBackground(Color.red);
            badFilters = true;
        }

        if (!badFilters) {
            for (Column column : allColumns) {

                if (instanceMatcher.matches(column.name)) {

                    Column visibleColumn = new Column();
                    visibleColumn.name = column.name;

                    List<Cell> cells = column.cells;
                    for (Cell cell : cells) {
                        if (stackMatcher.matches(cell.name) ||
                            stackMatcher.matches(cell.panel.getModel().getState().get()) ||
                            stackMatcher.matches(cell.panel.getModel().getStack().get())) {
                            visibleColumn.cells.add(cell);
                        }
                    }

                    if (visibleColumn.cells.size() > 0) {
                        visibleColumns.add(visibleColumn);
                    }
                }
            }

        }
    }

    private void sortRow(Column row) {
        Collections.sort(row.cells, new Comparator<Cell>() {
            @Override public int compare(Cell o1, Cell o2) {
                return o1.name.compareTo(o2.name);
            }
        });
    }

    private void sortRows() {
        Collections.sort(visibleColumns, new Comparator<Column>() {
            @Override public int compare(Column o1, Column o2) {
                return o1.name.compareTo(o2.name);
            }
        });
    }

    private void updateLayout() {
        scrollerPanel.removeAll();

        int rowIndex = 0;
        int columnIndex = 0;

        for (Column column : visibleColumns) {
            JLabel columnHeader = new JLabel(column.name);
            columnHeader.setFont(tahoma12Bold);
            scrollerPanel.add(columnHeader, StringUtils.format("cell {} {}, grow", columnIndex, 0));
            rowIndex++;

            List<Cell> cells = column.cells;
            for (Cell cell : cells) {
                scrollerPanel.add(cell.panel, StringUtils.format("cell {} {}, grow", columnIndex, rowIndex++));
                rowIndex++;
            }

            columnIndex++;
            rowIndex = 0;
        }

        revalidate();

    }

    protected void updateModel(SingleThreadViewModel t) {

        String key = StringUtils.format("{}.{}.{}.{}", t.getEnvironment(), t.getHost(), t.getInstanceType(), t.getInstanceNumber());
        
        Column column = allColumnMap.get(key);
        if (column == null) {
            column = new Column();
            column.name = key;
            allColumns.add(column);
            sortRows();
            allColumnMap.put(key, column);
        }

        Cell cell = new Cell();
        cell.panel.bind(t);
        cell.name = t.getName().get();
        column.cells.add(cell);
        sortRow(column);
        runFilter();
    }
}
