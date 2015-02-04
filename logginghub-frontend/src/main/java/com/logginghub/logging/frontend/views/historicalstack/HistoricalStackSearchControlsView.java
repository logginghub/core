package com.logginghub.logging.frontend.views.historicalstack;

import com.logginghub.logging.frontend.views.historicalevents.TimeSelectionView;
import com.logginghub.logging.frontend.views.stack.StackInstanceFilterModel;
import com.logginghub.logging.frontend.views.stack.StackInstanceFiltersModel;
import com.logginghub.logging.frontend.views.stack.StackInstanceFiltersView;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messages.StackTrace;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by james on 29/01/15.
 */
public class HistoricalStackSearchControlsView extends JPanel{

    private TimeSelectionView timeSelectionView;
    private HistoricalStackSearchController controller;
    private final DefaultComboBoxModel comboModel;
//    private final JComboBox levelsCombo;
//    private final JTextField keywordSearch;
    private final JButton startSearch;
    private final JButton stopSearch;

    private StackInstanceFiltersModel stackInstanceFiltersModel = new StackInstanceFiltersModel();
    private StackInstanceFiltersView instanceFiltersView = new StackInstanceFiltersView();

    public HistoricalStackSearchControlsView() {
        setLayout(new MigLayout("fill, gap 1", "[shrink][shrink][grow, fill]", "[grow, fill]"));

        timeSelectionView = new TimeSelectionView();

        add(timeSelectionView, "cell 0 0, grow, spanx 3");

//        keywordSearch = new JTextField("");
//        JLabel keywordSearchLabel = new JLabel("Keyword search:");

        comboModel = new DefaultComboBoxModel();
        comboModel.addElement("SEVERE");
        comboModel.addElement("WARNING");
        comboModel.addElement("INFO");
        comboModel.addElement("FINE");
        comboModel.addElement("FINER");
        comboModel.addElement("FINEST");
        comboModel.addElement("ALL");

//        levelsCombo = new JComboBox(comboModel);

        stackInstanceFiltersModel.getFilters().add(new StackInstanceFilterModel());
        instanceFiltersView.bind(stackInstanceFiltersModel);
        add(instanceFiltersView, "cell 0 1, grow, spanx 3");

//        add(levelsCombo, "cell 0 1");
//        add(keywordSearchLabel, "cell 1 1");
//        add(keywordSearch, "cell 2 1");

        startSearch = new JButton("Start");

        startSearch.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (controller != null) {
                    controller.startSearch();
                }
            }
        });

        stopSearch = new JButton("Stop");
        stopSearch.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (controller != null) {
                    controller.stopSearch();
                }
            }
        });

        add(startSearch, "cell 0 2");
        add(stopSearch, "cell 0 2");

    }

    public void bind(final HistoricalStackSearchController controller) {
        this.controller = controller;

        final HistoricalStackSearchModel historicalSearchModel = controller.getHistoricalSearchModel();
        this.timeSelectionView.bind(historicalSearchModel.getTimeSelectionModel());

//        Binder2 binder = new Binder2();
//        binder.bind(historicalSearchModel.getKeywordFilter(), keywordSearch);

        historicalSearchModel.getSearchInProgress().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                if(newValue) {
                    startSearch.setEnabled(false);
                    stopSearch.setEnabled(true);
                }else{
                    startSearch.setEnabled(true);
                    stopSearch.setEnabled(false);
                }
            }
        });


    }

    public boolean passesFilter(StackTrace trace, StackSnapshot stackSnapshot) {
        return instanceFiltersView.passesFilter(trace, stackSnapshot);
    }
}
