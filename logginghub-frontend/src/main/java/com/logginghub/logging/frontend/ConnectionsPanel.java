package com.logginghub.logging.frontend;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.HubConnectionModel;
import com.logginghub.logging.frontend.model.LoggingFrontendModel;
import com.logginghub.logging.frontend.model.ObservableList;

public class ConnectionsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private MigLayout migLayout;

    public ConnectionsPanel() {
        setBorder(new TitledBorder(null, "Connections", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        migLayout = new MigLayout("gap 0", "[grow,fill]", "[grow][grow]");
        setLayout(migLayout);

        ConnectionPanel connectionPanel1 = new ConnectionPanel();
        add(connectionPanel1, "cell 0 0,growx,aligny top");

        ConnectionPanel connectionPanel = new ConnectionPanel();
        add(connectionPanel, "cell 0 1,growx,aligny top");
    }

    public void setModel(LoggingFrontendModel model) {
        // TODO : attach listeners to pick up new environments and hubs
        update(model);
    }

    protected void update(LoggingFrontendModel model) {
        removeAll();

        // TODO : support multiple environments
        if (model.getEnvironments().size() > 0) {
            EnvironmentModel environmentModel = model.getEnvironments().get(0);
            ObservableList<HubConnectionModel> hubs = environmentModel.getHubConnectionModels();
            createPanels(hubs);
        }
    }

    private void createPanels(List<HubConnectionModel> socketSourceModels) {
        for (HubConnectionModel socketSourceModel : socketSourceModels) {
            ConnectionPanel connectionPanel = new ConnectionPanel();
            connectionPanel.setModel(socketSourceModel);
            add(connectionPanel, "gap 0, wrap");
        }
    }
}
