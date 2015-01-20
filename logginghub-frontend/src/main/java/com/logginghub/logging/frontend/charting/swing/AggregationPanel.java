package com.logginghub.logging.frontend.charting.swing;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.charting.model.AggregationConfiguration;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.utils.observable.Binder2;

public class AggregationPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private JComboBox aggregationTypeCombo;
    private JComboBox intervalComboBox;
    private Binder2 binder;
    
    public AggregationPanel() {
        setLayout(new MigLayout("", "[grow]", "[][][][]"));
        
        JRadioButton rdbtnNewRadioButton = new JRadioButton("Raw data");
        buttonGroup.add(rdbtnNewRadioButton);
        add(rdbtnNewRadioButton, "cell 0 0");
        
        JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("Aggregated data");
        buttonGroup.add(rdbtnNewRadioButton_1);
        add(rdbtnNewRadioButton_1, "cell 0 1");
        
        JLabel lblHowOftenDo = new JLabel("How often do you want it to update?");
        add(lblHowOftenDo, "flowx,cell 0 2");
        
        intervalComboBox = new JComboBox();
        intervalComboBox.setEditable(true);
        intervalComboBox.setModel(new DefaultComboBoxModel(new String[] {"1 second", "10 seconds", "30 seconds", "1 minute", "10 minutes"}));
        add(intervalComboBox, "cell 0 2,growx");
        
        JLabel lblWhatKindOf = new JLabel("What kind of result do you want to see?");
        add(lblWhatKindOf, "flowx,cell 0 3");
        
        aggregationTypeCombo = new JComboBox();
        aggregationTypeCombo.setEditable(false);
        DefaultComboBoxModel typeComboModel = new DefaultComboBoxModel(AggregationType.values());        
        aggregationTypeCombo.setModel(typeComboModel);
        add(aggregationTypeCombo, "cell 0 3,growx");
        
        
    }
    
    public void bind(AggregationConfiguration aggregationConfiguration) {
        binder = new Binder2();
        binder.bind(aggregationConfiguration.getType(), aggregationTypeCombo);
        binder.bind(aggregationConfiguration.getInterval(), intervalComboBox);
        
    }

    public void unbind(AggregationConfiguration aggregationConfiguration) {
        binder.unbind();
    }

}
