package com.logginghub.logging.frontend.views.historicalstack.historicalevents;

import com.logginghub.logging.frontend.views.historicalevents.TimeSelectionView;
import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
    private final JTextField keywordSearch;
    private final JButton startSearch;
    private final JButton stopSearch;

    public HistoricalStackSearchControlsView() {
        setLayout(new MigLayout("fill", "[shrink][shrink][grow, fill]", "[grow, fill]"));

        timeSelectionView = new TimeSelectionView();

        add(timeSelectionView, "cell 0 0, grow, spanx 3");

        keywordSearch = new JTextField("");
        JLabel keywordSearchLabel = new JLabel("Keyword search:");

        comboModel = new DefaultComboBoxModel();
        comboModel.addElement("SEVERE");
        comboModel.addElement("WARNING");
        comboModel.addElement("INFO");
        comboModel.addElement("FINE");
        comboModel.addElement("FINER");
        comboModel.addElement("FINEST");
        comboModel.addElement("ALL");

//        levelsCombo = new JComboBox(comboModel);

//        add(levelsCombo, "cell 0 1");
        add(keywordSearchLabel, "cell 1 1");
        add(keywordSearch, "cell 2 1");

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

        Binder2 binder = new Binder2();
        binder.bind(historicalSearchModel.getKeywordFilter(), keywordSearch);

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
}
