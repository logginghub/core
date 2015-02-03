package com.logginghub.logging.frontend.charting.swing;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.charting.model.BatchedArraryListTableModel;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableItemContainer;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservableListener;
import com.logginghub.utils.observable.ObservableProperty;

public class PatternModelListView extends JPanel {

    private static final Logger logger = Logger.getLoggerFor(PatternModelListView.class);
    private JTable table;

    private ObservableProperty<PatternModel> selectedPattern = new ObservableProperty<PatternModel>(null);

    private BatchedArraryListTableModel<PatternModel> tableModel = new BatchedArraryListTableModel<PatternModel>() {
        private static final long serialVersionUID = 1L;

        @Override public Object extractValue(PatternModel item, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return item.getName().get();
                case 1:
                    return item.getPattern().get();
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override public String[] getColumnNames() {
            return new String[] { "Name", "Pattern" };
        }
    };

    private static final long serialVersionUID = 1L;

    private int previousSelection = -1;

    public PatternModelListView() {
        setLayout(new MigLayout("fill", "[fill, grow]"));
        table = new JTable(tableModel) {
            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                // Always toggle on single selection
                super.changeSelection(rowIndex, columnIndex, !extend, extend);
            }
        };
        add(new JScrollPane(table));

        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();
                boolean isAdjusting = e.getValueIsAdjusting();

                logger.trace("Pattern list selection has changed : first {} last {} isAdjusting {}", firstIndex, lastIndex, isAdjusting);
                if (!isAdjusting) {

                    List<LogEvent> selected = new ArrayList<LogEvent>();
                    if (lsm.isSelectionEmpty()) {
                        selectedPattern.set(null);
                    }
                    else {

                        // Find out which indexes are selected.
                        int minIndex = lsm.getMinSelectionIndex();
                        int maxIndex = lsm.getMaxSelectionIndex();
                        for (int i = minIndex; i <= maxIndex; i++) {
                            if (lsm.isSelectedIndex(i)) {
                                // We've set single selection, so we dont need all this other
                                // crap.
                                PatternModel event = tableModel.getItemAtRow(i);
                                selectedPattern.set(event);
                                previousSelection = i;
                            }
                        }
                    }
                }
            }

        });
    }

    public ObservableProperty<PatternModel> getSelectedPattern() {
        return selectedPattern;
    }

    public void bind(ObservableList<PatternModel> patterns) {

        final Counterparts<PatternModel, ObservableListener> listeners = new Counterparts<PatternModel, ObservableListener>();

        patterns.addListenerAndNotifyExisting(new ObservableListListener<PatternModel>() {
            @Override public void onAdded(final PatternModel t) {
                tableModel.addToBatch(t);

                ObservableListener listener = new ObservableListener() {
                    @Override public void onChanged(ObservableItemContainer observable, Object childPropertyThatChanged) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                tableModel.rowUpdated(t);
                            }
                        });
                    }
                };

                t.addListener(listener);
                listeners.put(t, listener);
            }

            @Override public void onCleared() {}

            @Override public void onRemoved(PatternModel t, int index) {
                tableModel.remove(t);
                t.removeListener(listeners.remove(t));
            }
        });

    }

    public void clearSelection() {
        table.clearSelection();
    }

}
