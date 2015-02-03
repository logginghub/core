package com.logginghub.logging.frontend.views.historicalstack.historicalevents;

import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messages.StackTrace;
import com.logginghub.logging.messages.StackTraceItem;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.DateFormatFactory;
import com.logginghub.utils.Out;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;

import javax.swing.table.AbstractTableModel;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by james on 03/02/15.
 */
public class HistoricalStackTableDataModel extends AbstractTableModel {

    public static final int fixedColumnCount = 5;
    private Map<Long, Column> timeMapping = new HashMap<Long, Column>();

    private Map<String, HistoricalStackTableRow> rowMapping = new HashMap<String, HistoricalStackTableRow>();

    private List<HistoricalStackTableRow> rows = new ArrayList<HistoricalStackTableRow>();
    private List<Column> columns = new ArrayList<Column>();

    private DateFormat dateFormat = DateFormatFactory.getTimeWithoutMillis(DateFormatFactory.local);

    private Set<String> threadsToHide = new HashSet<String>();

    public HistoricalStackTableDataModel() {
        threadsToHide.add("Finalizer");
        threadsToHide.add("Reference Handler");
        threadsToHide.add("Signal Dispatcher");
        threadsToHide.add("DestroyJavaVM");

    }

    public HistoricalStackTableRow getRow(int row) {
        return rows.get(row);
    }

    public void clear() {
        timeMapping.clear();
        rows.clear();
        columns.clear();
        rowMapping.clear();
    }


    class Column {
        String heading = "";
        List<String> rows = new ArrayList<String>();
        public long time;
        Map<String, String> cellValuesByRowKey = new HashMap<String, String>();
        Map<String, String> tracesByRowKey = new HashMap<String, String>();
    }

    @Override public int getRowCount() {
        return rows.size();
    }

    @Override public int getColumnCount() {
        return fixedColumnCount + columns.size();
    }

    @Override public Class<?> getColumnClass(int columnIndex) {
        return super.getColumnClass(columnIndex);
    }

    @Override public String getColumnName(int column) {
        String name;
        switch (column) {
            case 0:
                name = "Environment";
                break;
            case 1:
                name = "Host";
                break;
            case 2:
                name = "Type";
                break;
            case 3:
                name = "Instance";
                break;
            case 4:
                name = "Thread name";
                break;
            default: {
                int offset = column - fixedColumnCount;
                Column column1 = columns.get(offset);
                name = column1.heading;
            }
        }

        return name;
    }

    @Override public Object getValueAt(int rowIndex, int column) {

        HistoricalStackTableRow row = rows.get(rowIndex);

        Object value;

        switch (column) {
            case 0:
                value = row.getEnvironment();
                break;
            case 1:
                value = row.getHost();
                break;
            case 2:
                value = row.getType();
                break;
            case 3:
                value = row.getInstance();
                break;
            case 4:
                value = row.getThreadName();
                break;
            default: {
                int offset = column - fixedColumnCount;
                Column column1 = columns.get(offset);
                String s = column1.cellValuesByRowKey.get(row.getKey());
                value = s;
            }
        }
        return value;
    }

    public String getTrace(int rowIndex, int column) {
        HistoricalStackTableRow row = rows.get(rowIndex);
        int offset = column - fixedColumnCount;
        Column column1 = columns.get(offset);
        String s = column1.tracesByRowKey.get(row.getKey());
        return s;
    }


    public void add(StackSnapshot snapshot) {

        StackTrace[] traces = snapshot.getTraces();
        for (StackTrace trace : traces) {

            if (!threadsToHide.contains(trace.getThreadName())) {
                String key = StringUtils.format("{}.{}.{}.{}.{}", snapshot.getEnvironment(), snapshot.getHost(), snapshot.getInstanceType(), snapshot.getInstanceNumber(), trace.getThreadName());

                HistoricalStackTableRow historicalStackTableRow = rowMapping.get(key);
                if (historicalStackTableRow == null) {
                    Out.out("Creating new row for '{}'", key);
                    historicalStackTableRow = new HistoricalStackTableRow();
                    historicalStackTableRow.setKey(key);
                    historicalStackTableRow.setEnvironment(snapshot.getEnvironment());
                    historicalStackTableRow.setHost(snapshot.getHost());
                    historicalStackTableRow.setInstance(snapshot.getInstanceNumber());
                    historicalStackTableRow.setType(snapshot.getInstanceType());
                    historicalStackTableRow.setThreadName(trace.getThreadName());

                    rowMapping.put(key, historicalStackTableRow);
                    rows.add(historicalStackTableRow);

                    Collections.sort(rows, new Comparator<HistoricalStackTableRow>() {
                        @Override public int compare(HistoricalStackTableRow o1, HistoricalStackTableRow o2) {
                            return CompareUtils.start().add(o1.getEnvironment(), o2.getEnvironment()).add(o1.getHost(), o2.getHost()).add(o1.getType(), o2.getType()).add(o1.getInstance(),
                                    o2.getInstance()).add(o1.getThreadName(), o2.getThreadName()).compare();
                        }
                    });
                }

                long time = snapshot.getTime();

                long chunked = TimeUtils.chunk(time, TimeUtils.seconds);

                Column column = timeMapping.get(chunked);

                if (column == null) {
                    // New column
                    column = new Column();
                    column.time = chunked;
                    column.heading = dateFormat.format(new Date(chunked));
                    columns.add(column);
                    Collections.sort(columns, new Comparator<Column>() {
                        @Override public int compare(Column o1, Column o2) {
                            return CompareUtils.compare(o2.time, o1.time);
                        }
                    });

                    Out.out("Creating new column for '{}'", column.heading);

                    timeMapping.put(chunked, column);
                }

                column.cellValuesByRowKey.put(key, trace.getThreadState());

                StringUtils.StringUtilsBuilder builder = new StringUtils.StringUtilsBuilder();
                builder.append("<html>Thread '{}' ({}) - state {}<br/>", trace.getThreadName(), trace.getThreadID(), trace.getThreadState());
                for (StackTraceItem stackTraceItem : trace.getItems()) {
                    builder.append("<span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;").append(stackTraceItem.toString()).append("</span><br/>");
                }
                builder.append("</html>");

                column.tracesByRowKey.put(key, builder.toString());
            }
        }

    }
}
