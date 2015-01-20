package com.logginghub.logging.frontend.charting.swing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.charting.model.BatchedArraryListTableModel;
import com.logginghub.logging.frontend.charting.model.StreamConfiguration;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

public class StreamDefinitionView extends JPanel {

    private static final long serialVersionUID = 1L;
    private ObservableProperty<StreamConfiguration> selectedStream = new ObservableProperty<StreamConfiguration>(null);
    private JTable table;
    
    private BatchedArraryListTableModel<StreamConfiguration> tableModel = new BatchedArraryListTableModel<StreamConfiguration>() {
        private static final long serialVersionUID = 1L;

        @Override public String[] getColumnNames() {
            return new String[] { "ID", "Pattern", "Field", "Event details" };
        }
        
        @Override public Object extractValue(StreamConfiguration item, int columnIndex) {
            switch(columnIndex) {
                case 0 : return item.getStreamID();
                case 1 : return item.getPatternID(); 
                case 2 : return item.getLabelIndex(); 
                case 3 : return Arrays.toString(item.getEventElements().toArray()); 
                default : return "?";
            }
            
        }
    };
    
    
    public StreamDefinitionView() {
        setLayout(new MigLayout("fill", "[fill,grow]", "[fill]"));
        table = new JTable(tableModel);
        add(new JScrollPane(table));
        
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();
                boolean isAdjusting = e.getValueIsAdjusting();

                List<LogEvent> selected = new ArrayList<LogEvent>();
                if (lsm.isSelectionEmpty()) {
                    // selection cleared
                }
                else {

                    // Find out which indexes are selected.
                    int minIndex = lsm.getMinSelectionIndex();
                    int maxIndex = lsm.getMaxSelectionIndex();
                    for (int i = minIndex; i <= maxIndex; i++) {
                        if (lsm.isSelectedIndex(i)) {
                            // We've set single selection, so we dont need all this other crap.
                            StreamConfiguration event = tableModel.getItemAtRow(i);
                            selectedStream.set(event);
                        }
                    }
                }
            }

        });
    }
    
    public ObservableProperty<StreamConfiguration> getSelectedStream() {
        return selectedStream;
    }
    
    public void bind(ObservableList<StreamConfiguration> streamDefinitionModels) {
        tableModel.bindTo(streamDefinitionModels);
    }

}
