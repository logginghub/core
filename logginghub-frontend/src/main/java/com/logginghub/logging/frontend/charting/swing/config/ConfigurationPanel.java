package com.logginghub.logging.frontend.charting.swing.config;

import com.logginghub.logging.frontend.charting.model.ChartingModel;
import com.logginghub.swingutils.MigPanel;
import com.logginghub.utils.swing.TestFrame;

public class ConfigurationPanel extends MigPanel {

    public ConfigurationPanel() {
        super("fill", "", "");
    }
    
    public void bind(ChartingModel model) {
        add(DynamicEditor.createEditor(model));
    }
    
    public static void main(String[] args) {
        ConfigurationPanel panel = new ConfigurationPanel();
        ChartingModel model = new ChartingModel();
        panel.bind(model);
        TestFrame.show(panel);
    }
    
    
}
