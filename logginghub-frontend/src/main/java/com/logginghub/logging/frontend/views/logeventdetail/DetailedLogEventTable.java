package com.logginghub.logging.frontend.views.logeventdetail;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.PathHelper;
import com.logginghub.logging.frontend.Utils;
import com.logginghub.logging.frontend.model.ColumnSettingsModel;
import com.logginghub.logging.frontend.model.ColumnSettingsModel.ColumnSettingModel;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.DelayedAction;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.VisualStopwatchController;
import com.logginghub.utils.logging.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class DetailedLogEventTable extends JTable {

    public final static String useDefaultColumnPropertiesKey = "detailedLogEventTable.useDefaultColumnProperties";
    private static final Logger logger = Logger.getLoggerFor(DetailedLogEventTable.class);
    private static final long serialVersionUID = 1L;
    private final ColumnSettingsModel columnSettingsModel;
    private DetailedLogEventTableModel tableModel;
    private List<RowHighlighter> highlighters = new CopyOnWriteArrayList<RowHighlighter>();
    private RowHighlighter defaultHighlighter;
    private File columnPropertiesFile;
    private boolean useDefaultColumnProperties = Boolean.getBoolean(useDefaultColumnPropertiesKey);
    private LogEvent[] bookmarks = new LogEvent[10];
    private Set<LogEvent> bookmarkSet = new HashSet<LogEvent>();
    private DelayedAction columnSaveDelayedAction = new DelayedAction(1, TimeUnit.SECONDS);

    private boolean controlKeyStatus = false;
    private boolean altKeyStatus = false;


    //    private IntegerStat paints;

    public DetailedLogEventTable(DetailedLogEventTableModel tableModel,
                                 RowHighlighter highlighter,
                                 String propertiesName,
                                 final ColumnSettingsModel columnSettingsModel) {
        this.tableModel = tableModel;
        this.defaultHighlighter = highlighter;
        this.columnSettingsModel = columnSettingsModel;

        //        StatBundle bundle = new StatBundle();
        //        paints = bundle.createIncremental("paints");
        //        bundle.startPerSecond(logger);

        columnPropertiesFile = PathHelper.getColumnsFile(propertiesName);

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setModel(tableModel);

        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                        applyColumnWidths(columnSettingsModel);
                    }
                }
            }
        });

        setupColumns(columnSettingsModel);

        // jshaw - its possible now to diable the entire dynamic columns system, so check to see if we need to skip all this
        if (!columnSettingsModel.isDisableColumnFile()) {
            loadColumnSettings();

            getColumnModel().addColumnModelListener(new TableColumnModelListener() {
                public void columnAdded(TableColumnModelEvent e) {
                }

                public void columnRemoved(TableColumnModelEvent e) {
                }

                public void columnMoved(TableColumnModelEvent e) {
                    columnSaveDelayedAction.execute(new Runnable() {
                        @Override
                        public void run() {
                            saveColumnSettings();
                        }
                    });
                }

                public void columnMarginChanged(ChangeEvent e) {
                    columnSaveDelayedAction.execute(new Runnable() {
                        @Override
                        public void run() {
                            saveColumnSettings();
                        }
                    });
                }

                public void columnSelectionChanged(ListSelectionEvent e) {
                }
            });
        }

        addMouseHandler();
    }

    private void applyColumnWidths(ColumnSettingsModel columnSettingsModel) {
        TableColumnModel tableColumnModel = getColumnModel();
        Map<String, ColumnSettingModel> columnSettings = columnSettingsModel.getColumnSettings();
        for (Entry<String, ColumnSettingModel> entry : columnSettings.entrySet()) {
            ColumnSettingModel value = entry.getValue();
            if (value.getWidth() > 0) {
                String columnName = entry.getKey();

                try {
                    int index = tableColumnModel.getColumnIndex(columnName);
                    TableColumn column = tableColumnModel.getColumn(index);
                    column.setPreferredWidth(value.getWidth());
                    logger.info("Setting column width for {} ({}) to {}", columnName, index, value.getWidth());

                    if (StringUtils.isNotNullOrEmpty(value.getAlignment())) {
                        if (value.getAlignment().equalsIgnoreCase("right")) {
                            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
                            rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
                            column.setCellRenderer(rightRenderer);
                        } else if (value.getAlignment().equalsIgnoreCase("left")) {
                            // Do nothing, this is the default
                        } else if (value.getAlignment().equalsIgnoreCase("centre") || value.getAlignment().equalsIgnoreCase("center")) {
                            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
                            rightRenderer.setHorizontalAlignment(SwingConstants.CENTER);
                            column.setCellRenderer(rightRenderer);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // jshaw - this is probably a column that we haven't got around to adding yet, we'll have to set the width on that later once
                    // it is in the column model
                }
            }
        }
    }

    private void setupColumns(ColumnSettingsModel columnSettingsModel) {

        // jshaw - due to the way column indicies change when removing columns, its important that we remove columns in one pass, and then change the order of the remaining items in a second pass
        Map<String, ColumnSettingModel> columnSettings = columnSettingsModel.getColumnSettings();
        Set<Entry<String, ColumnSettingModel>> entries = columnSettings.entrySet();

        TableColumnModel columnModel = getColumnModel();

        removeColumns(entries, columnModel);
        addMetadataColumns(entries);
        applyColumnWidths(columnSettingsModel);

        // We need to order the columns by their position to do this properly
        //        List<ColumnSettingModel> settings = sortColumnSettings(entries);

        //        applyColumnWidths(columnModel, settings);

    }

    private void loadColumnSettings() {
        Properties properties = new Properties();
        if (columnPropertiesFile.exists() && !useDefaultColumnProperties) {
            Reader reader = null;
            try {
                reader = new BufferedReader(new FileReader(columnPropertiesFile));
                properties.load(reader);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to load column settings from [%s]", columnPropertiesFile.getAbsolutePath()), e);
            } finally {
                FileUtils.closeQuietly(reader);
            }
        } else {
            InputStream openStream = ResourceUtils.openStream("defaultColumnWidths.properties");
            try {
                properties.load(openStream);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to load column settings from [%s]", columnPropertiesFile.getAbsolutePath()), e);
            } finally {
                FileUtils.closeQuietly(openStream);
            }
        }

        loadColumnSettings(properties, columnPropertiesFile);
    }

    protected void saveColumnSettings() {
        logger.fine("Saving column settings to '{}'", columnPropertiesFile.getAbsolutePath());
        Properties properties = new Properties();

        TableColumnModel columnModel = getColumnModel();
        int count = columnModel.getColumnCount();
        for (int i = 0; i < count; i++) {
            TableColumn column = columnModel.getColumn(i);

            int columnWidth = column.getWidth();
            String title = (String) column.getHeaderValue();

            properties.setProperty(title + "-width", "" + columnWidth);
            properties.setProperty(title + "-index", "" + i);
        }

        BufferedWriter writer = null;
        try {
            FileUtils.ensurePathExists(columnPropertiesFile);
            writer = new BufferedWriter(new FileWriter(columnPropertiesFile));
            properties.store(writer, "");
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to save column properties to [%s]", columnPropertiesFile.getAbsolutePath()), e);
        } finally {
            FileUtils.closeQuietly(writer);
        }
    }

    private final void addMouseHandler() {
        // The key listener detects if the control key is depressed
        addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_CONTROL) {
                    controlKeyStatus = true;
                } else if (event.getKeyCode() == KeyEvent.VK_ALT) {
                    altKeyStatus = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_CONTROL) {
                    controlKeyStatus = false;
                } else if (event.getKeyCode() == KeyEvent.VK_ALT) {
                    altKeyStatus = false;
                } else if (event.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteSelection();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                ListSelectionModel model = getSelectionModel();

                int selectionMode = model.getSelectionMode();
                if (selectionMode == ListSelectionModel.SINGLE_SELECTION) {
                    Point p = e.getPoint();
                    int rowNumber = rowAtPoint(p);
                    processOnRowClick(rowNumber, e);
                } else if (selectionMode == ListSelectionModel.SINGLE_INTERVAL_SELECTION) {
                    // model.setSelectionInterval(rowNumber, rowNumber);
                    int start = model.getMinSelectionIndex();
                    int end = model.getMaxSelectionIndex();
                    for (int i = start; i <= end; i++) {
                        processOnRowClick(i, e);
                    }
                } else if (selectionMode == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {

                    int[] selectedRows = getSelectedRows();
                    for (int i : selectedRows) {
                        processOnRowClick(i, e);
                    }

                }
            }
        });
    }

    private void removeColumns(Set<Entry<String, ColumnSettingModel>> entries, TableColumnModel columnModel) {
        for (Entry<String, ColumnSettingModel> entry : entries) {
            String columnName = entry.getKey();

            ColumnSettingModel value = entry.getValue();
            int width = value.getWidth();

            if (width == 0) {
                tableModel.removeColumn(columnName);
                logger.info("Removed column '{}' from the event view", columnName);
            }


        }
    }

    private void addMetadataColumns(Set<Entry<String, ColumnSettingModel>> entries) {
        List<ColumnSettingModel> columnSettingModels = sortColumnSettings(entries);

        for (ColumnSettingModel value : columnSettingModels) {
            if (StringUtils.isNotNullOrEmpty(value.getMetadataMapping())) {
                int index = value.getOrder();

                // jshaw - try and be a bit more accomodating to dodgy values
                if (index >= tableModel.getColumnCount()) {
                    index = tableModel.getColumnCount();
                }else if(index < 0) {
                    index = 0;
                }
                tableModel.addMetadataColumn(index, value.getMetadataMapping(), value.getName());
                logger.info("New column name '{}' mapped to '{}' added at index '{}'", value.getName(), value.getMetadataMapping(), index);
                TableColumnModel columnModel = getColumnModel();
            }
        }

    }

    //    private void applyColumnWidths(TableColumnModel columnModel, List<ColumnSettingModel> settings) {
    //        for (ColumnSettingModel entry : settings) {
    //
    //            String columnName = entry.getName();
    //
    //            try {
    //                int columnIndex = columnModel.getColumnIndex(columnName);
    //                int width = entry.getWidth();
    //                TableColumn column = columnModel.getColumn(columnIndex);
    //
    //                column.setPreferredWidth(width);
    //
    //                if (StringUtils.isNotNullOrEmpty(entry.getAlignment())) {
    //                    if (entry.getAlignment().equalsIgnoreCase("right")) {
    //                        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
    //                        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
    //                        column.setCellRenderer(rightRenderer);
    //                    } else if (entry.getAlignment().equalsIgnoreCase("left")) {
    //                        // Do nothing, this is the default
    //                    } else if (entry.getAlignment().equalsIgnoreCase("centre") || entry.getAlignment().equalsIgnoreCase("center")) {
    //                        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
    //                        rightRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    //                        column.setCellRenderer(rightRenderer);
    //                    }
    //                }
    //
    //                //                int order = entry.getOrder();
    //                //                order = Math.min(order, columnModel.getColumnCount() - 1);
    //                //
    //                //                logger.info("Moving column '{}' from original index {} to new index {}", columnName, columnIndex, order);
    //                //
    //                //                columnModel.moveColumn(columnIndex, order);
    //            } catch (IllegalArgumentException e) {
    //                logger.warn("Failed to find column '{}' in the column model - has it been removed?", columnName);
    //            }
    //        }
    //    }

    private void loadColumnSettings(Properties properties, File source) {
        Set<Object> keySet = properties.keySet();
        for (Object object : keySet) {
            String key = (String) object;
            String value = properties.getProperty(key);

            if (key.contains("-")) {
                String[] split = key.split("-");

                if (split.length == 2) {
                    String column = split[0];
                    String part = split[1];

                    try {
                        int valueInt = Integer.parseInt(value);
                        if (part.equals("width")) {
                            TableColumnModel columnModel = getColumnModel();
                            int columnIndex = columnModel.getColumnIndex(column);
                            columnModel.getColumn(columnIndex).setPreferredWidth(valueInt);
                        } else {
                            TableColumnModel columnModel = getColumnModel();
                            int columnIndex = columnModel.getColumnIndex(column);
                            columnModel.moveColumn(columnIndex, valueInt);
                        }

                        logger.info("Setting column properties from dynamic file {} {} {}", column, part, valueInt);
                    } catch (RuntimeException e) {
                        logger.warn(e,
                                    "Failed to process column setting for key '{}' and value '{}' from properties file '{}' - ignoring, but you might want to delete the columns file and re-layout your columns again to get rid of this warning",
                                    key,
                                    value,
                                    source.getAbsolutePath());

                    }
                }
            }
        }
    }

    protected void deleteSelection() {
        int[] selectedRows = getSelectedRows();
        int deleted = 0;

        for (int rowNumber : selectedRows) {
            int adjustedForEarlierDeletions = rowNumber - deleted;

            LogEvent logEventAtRow = tableModel.getLogEventAtRow(adjustedForEarlierDeletions);
            DefaultLogEvent defaultLogEvent = (DefaultLogEvent) logEventAtRow;

            if (!Utils.isEventLocked(logEventAtRow)) {
                tableModel.removeRow(adjustedForEarlierDeletions);
                deleted++;
            }
        }
    }

    private void processOnRowClick(int rowNumber, MouseEvent e) {
        LogEvent logEventAtRow = tableModel.getLogEventAtRow(rowNumber);
        if (e.getButton() == MouseEvent.BUTTON3) {
            if (logEventAtRow instanceof DefaultLogEvent) {
                toggleLocked(rowNumber, logEventAtRow);
            }
        }
    }

    private void toggleLocked(int rowNumber, LogEvent logEventAtRow) {
        DefaultLogEvent defaultLogEvent = (DefaultLogEvent) logEventAtRow;

        Map<String, String> metadata = defaultLogEvent.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<String, String>();
            defaultLogEvent.setMetadata(metadata);
        }

        if (metadata.containsKey("locked")) {
            boolean isLocked = Utils.isEventLocked(defaultLogEvent);
            if (isLocked) {
                metadata.put("locked", "false");
                tableModel.fireTableRowsUpdated(rowNumber, rowNumber);
            } else {
                metadata.put("locked", "true");
                tableModel.fireTableRowsUpdated(rowNumber, rowNumber);

            }
        } else {
            metadata.put("locked", "true");
            tableModel.fireTableRowsUpdated(rowNumber, rowNumber);
        }
    }

    public void addBookmark(int index) {
        LogEvent existing = bookmarks[index];
        if (existing != null) {
            bookmarkSet.remove(existing);
        }

        LogEvent selectedLogEvent = getSelectedLogEvent();
        if (selectedLogEvent != null) {
            bookmarks[index] = selectedLogEvent;
            bookmarkSet.add(selectedLogEvent);
        }

        repaint();
    }

    public LogEvent getSelectedLogEvent() {
        LogEvent selected = null;

        int selectedRow = getSelectedRow();
        if (selectedRow != -1) {
            selected = tableModel.getLogEventAtRow(selectedRow);
        }

        return selected;
    }

    public void addHighlighter(RowHighlighter highlighter) {
        highlighters.add(highlighter);
        tableModel.fireTableDataChanged();
    }

    @Override
    public Class<?> getColumnClass(int column) {
        String columnName = getColumnName(column);
        if (columnName != null && columnName.equals("")) {
            return ImageIcon.class;
        } else {
            return String.class;
        }
    }

    public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
        JComponent c = (JComponent) super.prepareRenderer(renderer, rowIndex, vColIndex);

        LogEvent event = tableModel.getLogEventAtRow(rowIndex);

        boolean isSelected = isRowSelected(rowIndex);
        boolean isBookmarked = bookmarkSet.contains(event);

        HighlightSettings settings = new HighlightSettings(null, null);

        for (RowHighlighter rowHighlighter : highlighters) {
            if (rowHighlighter.isInterested(rowIndex, vColIndex, isSelected, isBookmarked, event)) {
                rowHighlighter.updateSettings(settings, rowIndex, vColIndex, isSelected, isBookmarked, event);
                break;
            }
        }

        defaultHighlighter.updateSettings(settings, rowIndex, vColIndex, isSelected, isBookmarked, event);

        if (settings.getFont() != null) {
            c.setFont(settings.getFont());
        }

        if (isSelected) {
            int thickness = settings.getBorderThickness();
            MatteBorder outside = new MatteBorder(thickness, 0, thickness, 0, settings.getBorderColour());
            EmptyBorder inside = new EmptyBorder(0, thickness, 0, thickness);
            Border highlight = new CompoundBorder(outside, inside);

            c.setBorder(highlight);
        }

        if (settings != null) {
            c.setBackground(settings.getBackground());
            c.setForeground(settings.getForeground());
        }

        return c;
    }

    public void gotoBookmark(int index) {
        LogEvent event = bookmarks[index];
        if (event != null) {
            int rowIndex = tableModel.getVisibleIndexForEvent(event);
            setSelectedRow(rowIndex);

            int height = rowIndex * getRowHeight();

            Rectangle cellRect = getCellRect(rowIndex, 0, true);
            cellRect.y = height;
            scrollRectToVisible(cellRect);
        }
    }

    public void setSelectedRow(int newPositionOfSelectedEvent) {
        getSelectionModel().setSelectionInterval(newPositionOfSelectedEvent, newPositionOfSelectedEvent);
    }

    @Override
    protected void paintComponent(Graphics g) {
        JTable table = this;

        Rectangle clip = g.getClipBounds();

        Point upperLeft = clip.getLocation();
        int rMin = table.rowAtPoint(upperLeft);
        if (rMin == -1) {
            // Going to try and fudge it - if the clip is after our last view, change it to
            // something else or else we'll end up rendering the entire table.
            logger.fine("Fudging scrolling");
            g.setClip(0, 0, 10, 10);
        }

        Stopwatch stopwatch = Stopwatch.start("Table paintComponent");
        super.paintComponent(g);
        stopwatch.stop();
        VisualStopwatchController.getInstance().add(stopwatch);
        //        paints.increment();
    }

    public void removeHighlighter(RowHighlighter highlighter) {
        highlighters.remove(highlighter);
        tableModel.fireTableDataChanged();
    }

    public void setDefaultHighlighter(LevelHighlighter defaultHighlighter) {
        this.defaultHighlighter = defaultHighlighter;
    }

    public void setPreferredColumnWidths(double[] percentages) {
        Dimension tableDim = this.getPreferredSize();

        double total = 0;
        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            total += percentages[i];
        }

        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            TableColumn column = getColumnModel().getColumn(i);
            column.setPreferredWidth((int) (tableDim.width * (percentages[i] / total)));
        }
    }

    private List<ColumnSettingModel> sortColumnSettings(Set<Entry<String, ColumnSettingModel>> entries) {
        List<ColumnSettingModel> settings = new ArrayList<ColumnSettingModel>();
        for (Entry<String, ColumnSettingModel> entry : entries) {
            if (entry.getValue().getWidth() > 0) {
                settings.add(entry.getValue());
            }
        }

        Collections.sort(settings, new Comparator<ColumnSettingModel>() {
            @Override
            public int compare(ColumnSettingModel o1, ColumnSettingModel o2) {
                return CompareUtils.compare(o1.getOrder(), o2.getOrder());
            }
        });
        return settings;
    }
}
