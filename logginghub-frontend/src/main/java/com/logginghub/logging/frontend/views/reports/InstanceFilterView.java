package com.logginghub.logging.frontend.views.reports;

import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Color;

public class InstanceFilterView extends JPanel {

    private JTextField instanceFilter;
    private JCheckBox instanceFilterRegexCheckBox;

    private JTextField pidFilter;
    private JCheckBox pidFilterRegexCheckBox;

    public InstanceFilterView() {
        setLayout(new MigLayout("", "[][grow,fill][]", "[][][fill]"));

        instanceFilter = new JTextField("*.*.*.*");
        add(new JLabel("Instance Filter"), "cell 0 0");
        add(instanceFilter, "cell 1 0, grow");
        instanceFilterRegexCheckBox = new JCheckBox("Regex", false);
        add(instanceFilterRegexCheckBox, "cell 2 0");

        pidFilter = new JTextField("*");
        add(new JLabel("PID Filter"), "cell 0 1");
        add(pidFilter, "cell 1 1, grow");
        pidFilterRegexCheckBox = new JCheckBox("Regex", false);
        add(pidFilterRegexCheckBox, "cell 2 1");

        instanceFilter.setOpaque(true);
        pidFilter.setOpaque(true);

        instanceFilter.setBackground(Color.cyan);
    }

    public void bind(InstanceFilterModel model) {

        Binder2 binder = new Binder2();

        binder.bind(model.getInstanceFilter(), instanceFilter);
        binder.bind(model.getPidFilter(), pidFilter);

        binder.bind(model.getInstanceFilterIsRegex(), instanceFilterRegexCheckBox);
        binder.bind(model.getPidFilterIsRegex(), pidFilterRegexCheckBox);

        binder.bind(model.getInstanceFilterIsOK(), new ObservablePropertyListener<Boolean>() {
            @Override public void onPropertyChanged(Boolean oldValue, final Boolean newValue) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        if(newValue) {
                            instanceFilter.setBackground(Color.white);
                        }else{
                            instanceFilter.setBackground(Color.red);
                        }
                    }
                });
            }
        });

        binder.bind(model.getPidFilterIsOK(), new ObservablePropertyListener<Boolean>() {
            @Override public void onPropertyChanged(Boolean oldValue, final Boolean newValue) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        if(newValue) {
                            pidFilter.setBackground(Color.white);
                        }else{
                            pidFilter.setBackground(Color.red);
                        }
                    }
                });
            }
        });

    }


}
