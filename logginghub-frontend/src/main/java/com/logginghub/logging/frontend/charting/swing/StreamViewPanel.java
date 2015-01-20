package com.logginghub.logging.frontend.charting.swing;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.charting.model.BatchedArraryListTableModel;
import com.logginghub.logging.frontend.charting.model.Stream;
import com.logginghub.logging.frontend.charting.model.StreamListener;
import com.logginghub.logging.frontend.charting.model.StreamResultItem;

public class StreamViewPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private BatchedArraryListTableModel<StreamResultItem> tableModel = new BatchedArraryListTableModel<StreamResultItem>() {
        @Override public String[] getColumnNames() {
            return new String[] { "Path", "Result" };
        }

        @Override public Object extractValue(StreamResultItem item, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return item.getPath();
                case 1:
                    return item.getResult();
                default:
                    return "?";
            }
        }
    };

    private JTable table;

    public StreamViewPanel() {
        setLayout(new MigLayout("fill", "[grow, fill]", "[fill]"));
        table = new JTable(tableModel);
        add(new JScrollPane(table));
    }

    public void bind(Stream<StreamResultItem> itemStream) {

        // We only want to display the latest result from each item
        final Map<String, StreamResultItem> pathMap = new HashMap<String, StreamResultItem>();

        itemStream.addListener(new StreamListener<StreamResultItem>() {
            @Override public void onNewItem(StreamResultItem t) {

                StreamResultItem existing = pathMap.get(t.getPath());
                if (existing == null) {
                    existing = new StreamResultItem(t.getTime(), t.getPath(), t.getResult(), t.isNumeric());
                    pathMap.put(t.getPath(), existing);
                    tableModel.addToBatch(existing);
                }
                else {
                    existing.setResult(t.getResult());
                    tableModel.fireTableDataChanged();
                }
            }
        });

    }

}
