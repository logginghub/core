package com.logginghub.logging.frontend;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.model.LoggingFrontendModel;

public class ConfigurationPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private ConnectionsPanel connectionsPanel;

    public ConfigurationPanel() {
        setLayout(new MigLayout("", "[grow,fill]", "[fill]"));

        connectionsPanel = new ConnectionsPanel();
        add(connectionsPanel, "cell 0 0,alignx left,aligny top");
    }

    public void setModel(LoggingFrontendModel model) {
        connectionsPanel.setModel(model);
    }

}
