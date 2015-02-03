package com.logginghub.logging.frontend.charting.swing;

import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.utils.InsertSortedArrayList;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservableProperty;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PatternSelectionCombo extends JComboBox {
    private Model model;
    private static final long serialVersionUID = 1L;

    private ObservableProperty<PatternModel> selectedPattern = new ObservableProperty<PatternModel>(null);

    public PatternSelectionCombo() {

        setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                PatternModel model = (PatternModel) value;
                if (model != null) {
                    label.setText(model.getName().asString());
                }
                return label;
            }
        });

        addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    PatternModel selected = (PatternModel) getSelectedItem();
                    selectedPattern.set(selected);
                }
            }
        });
    }

    public ObservableProperty<PatternModel> getSelectedPattern() {
        return selectedPattern;
    }

    class Model implements ComboBoxModel {

        private InsertSortedArrayList<PatternModel> items = new InsertSortedArrayList<PatternModel>(new Comparator<PatternModel>() {
            @Override public int compare(PatternModel o1, PatternModel o2) {
                return o1.getName().asString().compareTo(o2.getName().asString());
            }
        });

        private Object selectedItem;
        private List<ListDataListener> listeners = new CopyOnWriteArrayList<ListDataListener>();

        @Override public int getSize() {
            return items.size();
        }

        @Override public Object getElementAt(int index) {
            return items.get(index);

        }

        @Override public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        @Override public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }

        @Override public void setSelectedItem(Object anItem) {
            this.selectedItem = anItem;
        }

        @Override public Object getSelectedItem() {
            return selectedItem;
        }

        public void removeItem(PatternModel t) {

            int removedIndex = -1;
            int index = 0;

            Iterator<PatternModel> iterator = items.iterator();
            while (iterator.hasNext()) {
                PatternModel wrapper = iterator.next();
                if (wrapper == t) {
                    removedIndex = index;
                    iterator.remove();
                    break;
                }
                index++;
            }

            if (removedIndex != -1) {
                ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, removedIndex, removedIndex);
                for (ListDataListener listDataListener : listeners) {
                    listDataListener.intervalRemoved(e);
                }
            }
        }

        public void clear() {
            int size = items.size();
            items.clear();
            ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, size);
            for (ListDataListener listDataListener : listeners) {
                listDataListener.intervalRemoved(e);
            }
        }

        public void add(PatternModel t) {
            int index = items.addAndReturnIndex(t);
            ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index);
            for (ListDataListener listDataListener : listeners) {
                listDataListener.intervalAdded(e);
            }
        }
    }

    public void bind(ObservableList<PatternModel> patternModels) {
        model = new Model();

        patternModels.addListenerAndNotifyExisting(new ObservableListListener<PatternModel>() {

            @Override public void onRemoved(final PatternModel t, int index) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        model.removeItem(t);
                        repaint();
                    }
                });
            }

            @Override public void onCleared() {
                model.clear();
            }

            @Override public void onAdded(final PatternModel t) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        model.add(t);
                        repaint();
                    }
                });

            }
        });

        setModel(model);
    }

}
