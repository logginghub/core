package com.logginghub.logging.frontend.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.logginghub.logging.frontend.images.Icons;
import com.logginghub.logging.frontend.images.Icons.IconIdentifier;
import com.logginghub.utils.CompareUtils;

public class QuickFilterHistoryTableModel extends AbstractTableModel {

    private String[] columns = new String[] { "Command", "Remove" };
    private List<QuickFilterHistoryEntryModel> commandHistory = new ArrayList<QuickFilterHistoryEntryModel>();

    @Override public String getColumnName(int column) {
        return columns[column];
    }

    @Override public int getRowCount() {
        return commandHistory.size();

    }

    @Override public int getColumnCount() {
        return 2;
    }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {

        QuickFilterHistoryEntryModel command = commandHistory.get(rowIndex);

        Object value = null;
        switch (columnIndex) {
            case 0:
                value = command.getCommand().get();
                break;
            case 1:
                value = Icons.get(IconIdentifier.Delete);
                break;
        }

        return value;

    }

    public void addEntry(QuickFilterHistoryEntryModel t) {
        commandHistory.add(t);
        fireTableRowsInserted(commandHistory.size(), commandHistory.size());
    }

    public void removeEntry(QuickFilterHistoryEntryModel t) {
        int indexOf = commandHistory.indexOf(t);
        commandHistory.remove(t);
        fireTableRowsDeleted(indexOf, indexOf);
    }

    public QuickFilterHistoryEntryModel getEntryAtRow(int selectedRow) {
        return commandHistory.get(selectedRow);

    }

    public void sortByUsage() {
        Collections.sort(commandHistory, new Comparator<QuickFilterHistoryEntryModel>() {
            @Override public int compare(QuickFilterHistoryEntryModel o1, QuickFilterHistoryEntryModel o2) {
                return CompareUtils.compareLongs(o2.getCount().get(), o1.getCount().get());
            }
        });
    }

    public void sortByTime() {
        Collections.sort(commandHistory, new Comparator<QuickFilterHistoryEntryModel>() {
            @Override public int compare(QuickFilterHistoryEntryModel o1, QuickFilterHistoryEntryModel o2) {
                return CompareUtils.compareLongs(o2.getLastUsed().get(), o1.getLastUsed().get());
            }
        });
    }

}
