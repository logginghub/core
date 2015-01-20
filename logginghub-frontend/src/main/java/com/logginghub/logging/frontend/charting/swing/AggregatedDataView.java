package com.logginghub.logging.frontend.charting.swing;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class AggregatedDataView extends JPanel {
    private static final long serialVersionUID = 1L;
    
    public AggregatedDataView() {
        setLayout(new MigLayout("fill", "[fill,grow]", "[fill]"));
        add(new JLabel("Aggregated data view"));
    }
    

}
