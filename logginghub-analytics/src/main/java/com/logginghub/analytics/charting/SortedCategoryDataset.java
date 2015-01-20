package com.logginghub.analytics.charting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;

public class SortedCategoryDataset implements CategoryDataset {

    private Map<Comparable, Map<Comparable, Double>> values = new HashMap<Comparable, Map<Comparable, Double>>();
    private List<Comparable> columnKeys = new ArrayList<Comparable>();
    private List<Comparable> rowKeys = new ArrayList<Comparable>();
    private List<DatasetChangeListener> listeners = new CopyOnWriteArrayList<DatasetChangeListener>();

    private DatasetGroup datasetGroup;
    private boolean sortRows;
    private boolean sortColumns;

    @Override
    public int getColumnIndex(Comparable column) {
        return columnKeys.indexOf(column);
    }

    @Override
    public Comparable getColumnKey(int columnKey) {
        return columnKeys.get(columnKey);
    }

    @Override
    public List getColumnKeys() {
        return columnKeys;
    }

    @Override
    public int getRowIndex(Comparable arg0) {
        return rowKeys.indexOf(arg0);
    }

    @Override
    public Comparable getRowKey(int arg0) {
        return rowKeys.get(arg0);
    }

    @Override
    public List getRowKeys() {
        return rowKeys;
    }

    @Override
    public Number getValue(Comparable row, Comparable column) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getColumnCount() {
        return columnKeys.size();
    }

    @Override
    public int getRowCount() {
        return rowKeys.size();
    }

    @Override
    public Number getValue(int arg0, int arg1) {

        Comparable row = rowKeys.get(arg0);
        Comparable column = columnKeys.get(arg1);

        Map<Comparable, Double> map = values.get(column);

        Number result;

        if (map != null) {
            Double integer = map.get(row);
            if (integer != null) {
                result = integer;
            } else {
                result = null;
            }
        } else {
            result = null;
        }

        return result;
    }

    @Override
    public void addChangeListener(DatasetChangeListener changeListener) {
        listeners.add(changeListener);
    }

    @Override
    public DatasetGroup getGroup() {
        return datasetGroup;
    }

    @Override
    public void removeChangeListener(DatasetChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    @Override
    public void setGroup(DatasetGroup datasetGroup) {
        this.datasetGroup = datasetGroup;
    }

    public void addValue(int value, Comparable subBit, Comparable seriesName) {
        setValue(value, subBit, seriesName);
    }

    public void setValue(double value, Comparable subBit, Comparable seriesName) {

        Map<Comparable, Double> map = values.get(seriesName);

        if (map == null) {
            map = new HashMap<Comparable, Double>();
            values.put(seriesName, map);
            columnKeys.add(seriesName);
            if (sortColumns) {
                Collections.sort(columnKeys);
            }
        }

        Double removed = map.put(subBit, value);
        if (removed == null) {
            if (!rowKeys.contains(subBit)) {
                rowKeys.add(subBit);
                if (sortRows) {
                    Collections.sort(rowKeys);
                }
            }
        }

        notifyListeners();
    }

    private void notifyListeners() {
        List<DatasetChangeListener> listeners2 = listeners;
        for (DatasetChangeListener datasetChangeListener : listeners2) {
            DatasetChangeEvent event = new DatasetChangeEvent(this, this);
            datasetChangeListener.datasetChanged(event);
        }
    }

    public void setSortRows(boolean sortRows) {
        this.sortRows = sortRows;
    }

    public void setSortColumns(boolean sortColumns) {
        this.sortColumns = sortColumns;
    }

}
