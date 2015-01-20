package com.logginghub.logging.frontend.modules;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.frontend.visualisations.TesterPanel;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Inject;

public class VisualisationTesterModule {
    private String layout;
    private LayoutService layoutService;
    private EnvironmentMessagingService messagingService;

    public VisualisationTesterModule() {

    }

    @Inject public void setLayoutService(LayoutService layoutService) {
        this.layoutService = layoutService;
    }

    @Inject public void setMessagingService(EnvironmentMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public void initialise() {

        JPanel panel = new JPanel(new MigLayout());
        add(panel, "Finest", Logger.finest);
        add(panel, "Finer/Trace", Logger.fine);
        add(panel, "Finer/Debug", Logger.finer);
        add(panel, "Info", Logger.info);
        add(panel, "Warning", Logger.warning);
        add(panel, "Severe", Logger.severe);

        layoutService.add(panel, layout);
    }

    private void add(JPanel panel, String levelDescription, int level) {
        TesterPanel testerPanel = new TesterPanel();
        testerPanel.initialise(levelDescription, level, messagingService);
        panel.add(testerPanel,"wrap");
    }

    public void start() {

    }
}
