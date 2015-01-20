package com.logginghub.logging.frontend.charting.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.logginghub.utils.Is;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;

/**
 * Provides a re-usable table model based on ArrayList. It also adds a batch update feature -
 * because you can only add items to a table model in the swing thread, and if you add tens of
 * thousands of events per second you end up flooding the swing queue - meaning nothing gets drawn.
 * This table model updates 10 times per second adding a batch each time on the swing thread,
 * cutting down on object in the swing queue and improving redraw efficiency.
 * 
 * @author James
 * 
 * @param <T>
 */
public abstract class BatchedArraryListTableModel<T> extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private List<T> itemsx = new ArrayList<T>();
    private List<T> filteredOutItems = new ArrayList<T>();

    private boolean somethingToAdd = false;
    private boolean somethingToRemove = false;

    private volatile List<T> incommingItemsToAdd = new ArrayList<T>();
    private volatile List<T> incommingItemsToRemove = new ArrayList<T>();

    private Timer timer;

    private int maximumRows = Integer.MAX_VALUE;

    private volatile Filter<T> filter = null;
    private Comparator<T> sorter = null;

    private volatile boolean needsRefilter = false;
    private volatile boolean needsSorting = false;

    // If this is set to false we process the batch as soon as something is added, without needing
    // the separate timer thread
    private boolean asyncMode = true;

    public BatchedArraryListTableModel() {
        startTimer();
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public abstract String[] getColumnNames();

    @Override public int getRowCount() {
        return itemsx.size();
    }

    @Override public int getColumnCount() {
        if (getColumnNames() == null) {
            return 0;
        }
        else {
            return getColumnNames().length;
        }
    }

    @Override public String getColumnName(int column) {
        String[] columnNames = getColumnNames();
        if (column >= 0 && column < columnNames.length) {
            return columnNames[column];
        }
        else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public T getItemAtRow(int row) {
        return itemsx.get(row);
    }

    public abstract Object extractValue(T item, int columnIndex);

    @Override public Object getValueAt(int rowIndex, int columnIndex) {
        T itemAtRow = getItemAtRow(rowIndex);
        Object value = extractValue(itemAtRow, columnIndex);
        return value;
    }

    public void addToBatch(final T item) {
        synchronized (incommingItemsToAdd) {
            somethingToAdd = true;
            incommingItemsToAdd.add(item);
        }

        if (!asyncMode) {
            processBatch();
        }
    }

    public void remove(T item) {
        synchronized (incommingItemsToRemove) {
            somethingToRemove = true;
            incommingItemsToRemove.add(item);
        }

        if (!asyncMode) {
            processBatch();
        }
    }

    private void startTimer() {
        if (timer == null) {
            timer = TimerUtils.every("ArrayListTableModel-batchUpdater", 100, TimeUnit.MILLISECONDS, new Runnable() {
                @Override public void run() {
                    processBatch();
                }
            });
        }
    }

    public void setFilter(Filter<T> filter) {
        this.filter = filter;
        refilter();
    }

    public void refilter() {
        needsRefilter = true;
    }

    public void sort() {
        needsSorting = true;
    }

    public Filter<T> getFilter() {
        return filter;
    }

    public void rowUpdated(T t) {
        int indexOf = itemsx.indexOf(t);
        if (indexOf >= 0) {
            fireTableRowsUpdated(indexOf, indexOf);
        }
    }

    private void batchUpdate() {

        // These booleans aren't really thread safe - but the order which they will be called means
        // we'll end up doing add/remove operations unneccesarily, rather than missing an update,
        // which is acceptable

        if (somethingToAdd) {
            somethingToAdd = false;
            batchAdd();
        }

        if (somethingToRemove) {
            somethingToRemove = false;
            batchRemove();
        }

        if (needsRefilter) {
            batchRefilter();
            needsRefilter = false;
        }

        if (needsSorting) {
            batchSort();
            needsSorting = false;
        }
    }

    private void batchRefilter() {

        if (filter == null) {
            itemsx.addAll(filteredOutItems);
            filteredOutItems.clear();
        }
        else {
            // Move items from filtered out to current
            Iterator<T> iterator = filteredOutItems.iterator();
            while (iterator.hasNext()) {
                T t = (T) iterator.next();
                if (filter.passes(t)) {
                    itemsx.add(t);
                    iterator.remove();
                }
            }

            // Move items from current to filtered out
            iterator = itemsx.iterator();
            while (iterator.hasNext()) {
                T t = (T) iterator.next();
                if (!filter.passes(t)) {
                    filteredOutItems.add(t);
                    iterator.remove();
                }
            }
        }

        batchSort();

        fireTableDataChanged();

    }

    private void batchSort() {
        if (sorter != null) {
            Collections.sort(itemsx, sorter);
        }
    }

    public void setSorter(Comparator<T> sorter) {
        this.sorter = sorter;
        needsSorting = true;
    }

    private void batchAdd() {
        // Reference switch the lists
        List<T> thisBatch = incommingItemsToAdd;
        incommingItemsToAdd = new ArrayList<T>();

        Is.trueStatement(SwingUtilities.isEventDispatchThread(), "update outside of swing");

        // Remove enough items so we dont overfil
        int newSize = itemsx.size() + incommingItemsToAdd.size();
        if (newSize > maximumRows) {
            int rowsToRemove = newSize - maximumRows;
            for (int i = 0; i < rowsToRemove; i++) {
                itemsx.remove(0);
            }
            fireTableRowsDeleted(0, rowsToRemove);
        }

        // Add the new batch
        int start = itemsx.size();
        int size = thisBatch.size();
        for (int i = 0; i < size; i++) {
            T t = thisBatch.get(i);
            if (filter == null || filter.passes(t)) {
                itemsx.add(t);
            }
        }
        int end = itemsx.size();

        // Help the GC
        thisBatch = null;

        // Notify the table
        fireTableRowsInserted(start, end);
    }

    private void batchRemove() {
        Is.trueStatement(SwingUtilities.isEventDispatchThread(), "update outside of swing");

        // Reference switch the lists
        List<T> thisBatch = incommingItemsToRemove;
        incommingItemsToRemove = new ArrayList<T>();

        // We are notifying the table for each element, as they might be all over the place
        for (T t : thisBatch) {
            int index = itemsx.indexOf(t);
            if (index >= 0) {
                itemsx.remove(index);
                fireTableRowsDeleted(index, index);
            }
        }

        // Help the GC
        thisBatch = null;

    }

    public void clear() {
        // This is thoroughly not atomic or predicatable :/

        synchronized (incommingItemsToAdd) {
            incommingItemsToAdd.clear();
        }

        synchronized (incommingItemsToRemove) {
            incommingItemsToAdd.clear();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                itemsx.clear();
            }
        });

        if (!asyncMode) {
            processBatch();
        }
    }

    public void bindTo(ObservableList<T> list) {
        list.addListenerAndNotifyExisting(new ObservableListListener<T>() {

            @Override public void onRemoved(T t) {
                remove(t);
            }

            @Override public void onCleared() {
                clear();
            }

            @Override public void onAdded(T t) {
                addToBatch(t);
            }
        });

    }

    // public void addNow(final T item) {
    // SwingUtilities.invokeLater(new Runnable() {
    // @Override public void run() {
    // addInternal(item);
    // }
    // });
    // }
    //
    // protected void addInternal(T item) {
    // items.add(item);
    // int rowCount = getRowCount();
    // fireTableRowsInserted(rowCount, rowCount);
    // }

    public void setMaximumRows(int maximumRows) {
        this.maximumRows = maximumRows;
    }

    public void processBatch() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                batchUpdate();
            }
        });
    }

    public void setAsync(boolean asyncMode) {
        this.asyncMode = asyncMode;
    }

}
