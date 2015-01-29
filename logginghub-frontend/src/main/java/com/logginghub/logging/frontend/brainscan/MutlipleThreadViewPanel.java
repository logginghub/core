package com.logginghub.logging.frontend.brainscan;

import com.logginghub.logging.messages.StackStrobeRequest;
import com.logginghub.swingutils.MigPanel;
import com.logginghub.utils.DelayedAction;
import com.logginghub.utils.Destination;
import com.logginghub.utils.HTMLBuilder2;
import com.logginghub.utils.Out;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MutlipleThreadViewPanel extends JPanel {

    private static final Logger logger = Logger.getLoggerFor(MutlipleThreadViewPanel.class);

    private final JScrollPane scrollPane;
    private final JButton addFilterButton;
    private final JButton andOrButton;
    private final JButton copyCSVButton;
    private final JButton copyHTMLButton;

    private List<MutlipleThreadViewFilterPanel> filterPanels = new ArrayList<MutlipleThreadViewFilterPanel>();

    //    private final JTextField threadFilter;
    //    private final JCheckBox threadFilterRegexCheckBox;
    private List<Column> visibleColumns = new ArrayList<Column>();

    private Map<String, Column> allColumnMap = new HashMap<String, MutlipleThreadViewPanel.Column>();
    private List<Column> allColumns = new ArrayList<Column>();

    private DelayedAction filterDelay = new DelayedAction(50, TimeUnit.MILLISECONDS);
    //    private JTextField instanceFilter;
    //    private JCheckBox instanceFilterRegexCheckBox;
    private MigPanel scrollerContentPanel;
    //    private JTextField stackFilter;
    //    private JCheckBox stackFilterRegexCheckBox;

    private static final Font tahoma12Bold = new Font("Tahoma", Font.BOLD, 12);
    private JButton refreshButton;

    private BrainScanController controller;

    private JButton clearButton;
    private final JPanel filterPane;
    private boolean andMatch = true;

    public static class Cell {
        String name;
        SingleThreadViewPanel panel = new SingleThreadViewPanel();
    }

    public static class Column {
        List<Cell> cells = new ArrayList<Cell>();
        String name;
    }

    class PanelMap extends HashMap<SingleThreadViewModel, SingleThreadViewPanel> {}

    public MutlipleThreadViewPanel() {
        setLayout(new MigLayout("insets 1", "[grow,fill]", "[top][grow,fill]"));

        filterPane = new JPanel();
        filterPane.setLayout(new MigLayout("", "[grow,fill]", "[grow, fill]"));

        addFilter();
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
                clearData();
            }
        });
        buttonPane.add(clearButton, "cell 1 0");

        copyCSVButton = new JButton("Copy CSV to clipboard");
        copyCSVButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copyCSVToClipboard();
            }
        });
        buttonPane.add(copyCSVButton, "cell 2 0");

        copyHTMLButton = new JButton("Copy HTML to clipboard");
        copyHTMLButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copyHTMLToClipboard();
            }
        });
        buttonPane.add(copyHTMLButton, "cell 3 0");

        addFilterButton = new JButton("Add another filter");
        addFilterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addFilter();
                filterPane.invalidate();
                filterPane.revalidate();
                filterPane.doLayout();
                andOrButton.setVisible(true);
            }
        });
        buttonPane.add(addFilterButton, "cell 4 0");

        andOrButton = new JButton("AND");
        andOrButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toggleAndOr();
                runFilter();
            }
        });
        buttonPane.add(andOrButton, "cell 5 0");
        andOrButton.setVisible(false);

        filterPane.add(buttonPane, "cell 0 1");
        filterPane.setBorder(BorderFactory.createTitledBorder("Filtering"));

        scrollerContentPanel = new MigPanel();
        scrollerContentPanel.setBackground(Color.WHITE);
        scrollPane = new JScrollPane(scrollerContentPanel);

        scrollPane.getVerticalScrollBar().setUnitIncrement(32);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(32);

        add(scrollPane, "cell 0 1");
    }

    private void copyCSVToClipboard() {

        StringUtils.StringUtilsBuilder builder = new StringUtils.StringUtilsBuilder();

        // Write the header rows
        builder.append("Thread,");
        for (Column visibleColumn : visibleColumns) {
            builder.append(visibleColumn.name).append(",,");
        }
        builder.append("\r\n");

        builder.append(",");
        for (Column visibleColumn : visibleColumns) {
            builder.append("State,Trace,");
        }
        builder.append("\r\n");

        // Get a sorted list of all the threads
        Set<String> threadNames = new HashSet<String>();
        for (Column visibleColumn : visibleColumns) {
            List<Cell> cells = visibleColumn.cells;
            for (Cell cell : cells) {
                threadNames.add(cell.name);
            }
        }
        List<String> sortedThreadNames = new ArrayList<String>(threadNames);
        Collections.sort(sortedThreadNames);

        // Now render each row based on the thread names
        for (String sortedThreadName : sortedThreadNames) {
            builder.append(sortedThreadName).append(",");
            for (Column visibleColumn : visibleColumns) {
                List<Cell> cells = visibleColumn.cells;

                String state = "";
                String stack = "";

                for (Cell cell : cells) {
                    if (cell.name.equals(sortedThreadName)) {
                        state = cell.panel.getModel().getState().get();
                        stack = cell.panel.getModel().getStack().get();
                        break;
                    }
                }

                builder.append(state).append(",\"").append(stack).append("\",");

            }

            builder.append("\r\n");
        }

        Out.out(builder);

        StringSelection stringSelection = new StringSelection(builder.toString());
        Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard();
        clpbrd.setContents (stringSelection, null);

    }

    private void copyHTMLToClipboard() {

        HTMLBuilder2 builder = new HTMLBuilder2();

        HTMLBuilder2.TableElement table = builder.getBody().table();

        HTMLBuilder2.RowElement header1 = table.row();
        HTMLBuilder2.RowElement header2 = table.row();

        // Write the header rows
        header1.cell("Thread");
        for (Column visibleColumn : visibleColumns) {
            header1.cell(visibleColumn.name).setAttribute("colspan", "2");
        }

        header2.cell();
        for (Column visibleColumn : visibleColumns) {
            header2.cell("State");
            header2.cell("Trace");
        }

        // Get a sorted list of all the threads
        Set<String> threadNames = new HashSet<String>();
        for (Column visibleColumn : visibleColumns) {
            List<Cell> cells = visibleColumn.cells;
            for (Cell cell : cells) {
                threadNames.add(cell.name);
            }
        }
        List<String> sortedThreadNames = new ArrayList<String>(threadNames);
        Collections.sort(sortedThreadNames);

        // Now render each row based on the thread names
        for (String sortedThreadName : sortedThreadNames) {

            HTMLBuilder2.RowElement dataRow = table.row();

            dataRow.cell(sortedThreadName);
            for (Column visibleColumn : visibleColumns) {
                List<Cell> cells = visibleColumn.cells;

                String state = "";
                String stack = "";

                for (Cell cell : cells) {
                    if (cell.name.equals(sortedThreadName)) {
                        state = cell.panel.getModel().getState().get();
                        stack = cell.panel.getModel().getStack().get();
                        break;
                    }
                }

                dataRow.cell(state);
                dataRow.cell(stack);

            }

        }

        Out.out(builder);

        StringSelection stringSelection = new StringSelection(builder.toString());
        Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard();
        clpbrd.setContents (stringSelection, null);

    }

    private void toggleAndOr() {
        andMatch = !andMatch;
        if (andMatch) {
            andOrButton.setText("AND");
        } else {
            andOrButton.setText("OR");
        }
    }

    private void addFilter() {
        MutlipleThreadViewFilterPanel filterPanel = new MutlipleThreadViewFilterPanel();

        filterPanel.getFilterChangedStream().addDestination(new Destination<MutlipleThreadViewFilterPanel>() {
            @Override public void send(MutlipleThreadViewFilterPanel mutlipleThreadViewFilterPanel) {
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

        filterPanels.add(filterPanel);
        filterPane.add(filterPanel, "cell 0 0");
    }

    public void bind(BrainScanController controller, ObservableList<ThreadsInProcessViewModel> viewList) {

        this.controller = controller;

        viewList.addListenerAndNotifyExisting(new ObservableListListener<ThreadsInProcessViewModel>() {

            @Override public void onAdded(ThreadsInProcessViewModel t) {

                final ObservableList<SingleThreadViewModel> threads = t.getThreads();

                // Be on the look out for new data
                threads.addListener(new ObservableListListener<SingleThreadViewModel>() {
                    @Override public void onAdded(final SingleThreadViewModel t) {
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

            @Override public void onRemoved(ThreadsInProcessViewModel t) {}
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

        logger.debug("Running filter");
        visibleColumns.clear();

        for (Column column : allColumns) {
            boolean includeColumn = andMatch;

            for (MutlipleThreadViewFilterPanel filter : filterPanels) {
                if (andMatch) {
                    includeColumn &= filter.includeColumn(column);
                } else {
                    includeColumn |= filter.includeColumn(column);
                }
            }

            if (includeColumn) {

                Column visibleColumn = new Column();
                visibleColumn.name = column.name;

                List<Cell> cells = column.cells;
                for (Cell cell : cells) {

                    boolean includeCell = andMatch;

                    for (MutlipleThreadViewFilterPanel filter : filterPanels) {
                        if (andMatch) {
                            includeCell &= filter.includeCell(cell);
                        } else {
                            includeCell |= filter.includeCell(cell);
                        }
                    }

                    if (includeCell) {
                        visibleColumn.cells.add(cell);
                    }
                }

                if (visibleColumn.cells.size() > 0) {
                    visibleColumns.add(visibleColumn);
                }
            }
        }

        sortColumns();
        updateLayout();
    }

    private void sortRow(Column row) {
        Collections.sort(row.cells, new Comparator<Cell>() {
            @Override public int compare(Cell o1, Cell o2) {
                return o1.name.compareTo(o2.name);
            }
        });
    }

    private void sortColumns() {
        Collections.sort(visibleColumns, new Comparator<Column>() {
            @Override public int compare(Column o1, Column o2) {
                return o1.name.compareTo(o2.name);
            }
        });
    }

    private void updateLayout() {
        logger.debug("Updating layout");
        scrollerContentPanel.removeAll();

        int rowIndex = 0;
        int columnIndex = 0;

        for (Column column : visibleColumns) {
            JLabel columnHeader = new JLabel(column.name);
            columnHeader.setFont(tahoma12Bold);
            scrollerContentPanel.add(columnHeader, StringUtils.format("cell {} {}, grow", columnIndex, 0));
            rowIndex++;

            List<Cell> cells = column.cells;
            for (Cell cell : cells) {
                scrollerContentPanel.add(cell.panel, StringUtils.format("cell {} {}, grow", columnIndex, rowIndex++));
                rowIndex++;
            }

            columnIndex++;
            rowIndex = 0;
        }

        revalidate();

    }

    protected void updateModel(SingleThreadViewModel t) {

        String columnKey = StringUtils.format("{}.{}.{}.{}",
                t.getEnvironment(),
                t.getHost(),
                t.getInstanceType(),
                t.getInstanceNumber());

        Column column = allColumnMap.get(columnKey);
        if (column == null) {
            column = new Column();
            column.name = columnKey;
            allColumns.add(column);
            sortColumns();
            allColumnMap.put(columnKey, column);
        }


        Cell cell = new Cell();
        cell.panel.bind(t);
        cell.name = t.getName().get();
        column.cells.add(cell);
        sortRow(column);
        runFilter();
    }
}
