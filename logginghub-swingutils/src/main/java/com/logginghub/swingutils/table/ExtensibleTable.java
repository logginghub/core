package com.logginghub.swingutils.table;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class ExtensibleTable<T> extends JTable {
    private static final long serialVersionUID = 1L;

    private ExtensibleTableModel<T> model;

//    private JComboBox backlogComboBox;

    public ExtensibleTable(final ExtensibleTableModel<T> model) {
        super(model);
        this.model = model;

//        setRowHeight(22);

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);

//        sorter.setComparator(TicketTableModel.column_id, new Comparator<Integer>() {
//            @Override public int compare(Integer o1, Integer o2) {
//                return CompareUtils.compare(o1, o2);
//            }
//        });
//
//        sorter.setComparator(TicketTableModel.column_score, new Comparator<Integer>() {
//            @Override public int compare(Integer o1, Integer o2) {
//                return CompareUtils.compare(o1, o2);
//            }
//        });

        setRowSorter(sorter);

//        setAutoscrolls(false);

//        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        getColumnModel().getColumn(TicketTableModel.column_id).setPreferredWidth(50);
//        getColumnModel().getColumn(TicketTableModel.column_extid).setPreferredWidth(50);
//        getColumnModel().getColumn(TicketTableModel.column_score).setPreferredWidth(50);
//        getColumnModel().getColumn(TicketTableModel.column_stateIcon).setPreferredWidth(30);
//        getColumnModel().getColumn(TicketTableModel.column_state).setPreferredWidth(100);
//        // getColumnModel().getColumn(TicketTableModel.column_value).setPreferredWidth(130);
//        getColumnModel().getColumn(TicketTableModel.column_severity).setPreferredWidth(130);
//        getColumnModel().getColumn(TicketTableModel.column_priority).setPreferredWidth(130);
//        getColumnModel().getColumn(TicketTableModel.column_effort).setPreferredWidth(100);
//        getColumnModel().getColumn(TicketTableModel.column_backlog).setPreferredWidth(100);
//        getColumnModel().getColumn(TicketTableModel.column_headline).setPreferredWidth(1000);
//
//        TableColumn stateColumn = getColumnModel().getColumn(TicketTableModel.column_state);
//        TableColumn backlogColumn = getColumnModel().getColumn(TicketTableModel.column_backlog);
//        TableColumn effortColumn = getColumnModel().getColumn(TicketTableModel.column_effort);
//        // TableColumn valueColumn = getColumnModel().getColumn(TicketTableModel.column_value);
//        TableColumn priorityColumn = getColumnModel().getColumn(TicketTableModel.column_priority);
//        TableColumn severityColumn = getColumnModel().getColumn(TicketTableModel.column_severity);
//
//        setupEnumCombo(State.values(), stateColumn);
//        setupEnumCombo(Priority.values(), priorityColumn);
//        setupEnumCombo(Severity.values(), severityColumn);
//        // setupEnumCombo(Value.values(), valueColumn);


        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    JTable table = (JTable) me.getSource();
                    Point p = me.getPoint();
                    int row = table.rowAtPoint(p);

                    int realRow = table.getRowSorter().convertRowIndexToModel(row);

//                    Ticket ticketAtRow = model.getTicketAtRow(realRow);
//                    selectedTickets.send(ticketAtRow);
                }
            }
        });
    }

//    private <T> void setupEnumCombo(T[] values, TableColumn stateColumn) {
//        JComboBox combo = new JComboBox();
//        for (T state : values) {
//            combo.addItem(state);
//        }
//        setupListSize(combo);
//        stateColumn.setCellEditor(new DefaultCellEditor(combo));
//    }
//
//    private void setupListSize(JComboBox effortComboBox) {
//        Object popup = effortComboBox.getUI().getAccessibleChild(effortComboBox, 0);
//        if (popup instanceof ComboPopup) {
//            JList jlist = ((ComboPopup) popup).getList();
//            jlist.setFixedCellHeight(14);
//        }
//    }

    @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {

        Component component = super.prepareRenderer(renderer, row, column);

        int realRow = getRowSorter().convertRowIndexToModel(row);
        T ticketAtRow = model.getItemAtRow(realRow);

//        if (ticketAtRow.getState() == State.Done) {
//            component.setBackground(ColourUtils.parseColor("C7FFCD"));
//        }
//        else if (ticketAtRow.getState() == State.OnHold) {
//            component.setBackground(ColourUtils.parseColor("F2BFCE"));
//        }
//        else if (ticketAtRow.getState() == State.ReadyForDeployment) {
//            component.setBackground(ColourUtils.parseColor("64FA74"));
//        }
//        else {
//            int score = 0;
//
//            if (!StringUtils.isNotNullOrEmpty(ticketAtRow.getBacklog())) {
//                score++;
//            }
//
//            if (ticketAtRow.getEffort() == Effort.Unknown) {
//                score++;
//            }
//
//            if (ticketAtRow.getPriority() == Priority.Unknown) {
//                score++;
//            }
//
//            if (ticketAtRow.getSeverity() == Severity.Unknown) {
//                score++;
//            }
//
//            if (score > 0) {
//                String[] colours = new String[] { "FFFFFF", "FFF1DE", "FFECD1", "FCD195", "FFAE3D" };
//                component.setBackground(ColourUtils.parseColor(colours[score]));
//            }
//            else if (ticketAtRow.getSeverity() == Severity.BlocksRelease) {
//                if (ticketAtRow.getTriageScore() >= 80) {
//                    component.setBackground(ColourUtils.parseColor("FEFFF5"));
//                }
//                else {
//                    component.setBackground(ColourUtils.parseColor("A6A6A6"));
//                }
//            }
//            else {
//                component.setBackground(Color.white);
//            }
//        }
//
//        component.setForeground(Color.black);
        
        return component;

    }


//    public Stream<Ticket> getSelectedTickets() {
//        return selectedTickets;
//    }

//    public void changeSelection(int row, int column, boolean toggle, boolean extend) {
//        super.changeSelection(row, column, toggle, extend);
//        if (editCellAt(row, column)) {
//            Component editor = getEditorComponent();
//            editor.requestFocusInWindow();
//        }
//    }

//    @Override public Class<?> getColumnClass(int column) {
//        switch (column) {
//            case TicketTableModel.column_stateIcon:
//                return ImageIcon.class;
//            case TicketTableModel.column_extid:
//            case TicketTableModel.column_headline:
//            case TicketTableModel.column_backlog:
//            case TicketTableModel.column_state:
//                return String.class;
//            case TicketTableModel.column_id:
//            case TicketTableModel.column_score:
//                return Integer.class;
//            default:
//                return String.class;
//        }
//
//    }

    @Override public TableModel getModel() {
        return super.getModel();
    }

//    public JComboBox getBacklogComboBox() {
//        return backlogComboBox;
//    }
}
