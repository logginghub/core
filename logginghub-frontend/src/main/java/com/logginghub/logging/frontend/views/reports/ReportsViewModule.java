package com.logginghub.logging.frontend.views.reports;

import com.logginghub.logging.frontend.modules.EnvironmentMessagingService;
import com.logginghub.logging.frontend.modules.EnvironmentNotificationListener;
import com.logginghub.logging.frontend.modules.configuration.ReportsViewConfiguration;
import com.logginghub.logging.frontend.services.EnvironmentNotificationService;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.telemetry.configuration.HubConfiguration;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Inject;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

import javax.swing.JPanel;

public class ReportsViewModule implements Module<ReportsViewConfiguration> {

    private static final Logger logger = Logger.getLoggerFor(ReportsViewModule.class);
    private ReportsViewConfiguration configuration;
    private EnvironmentMessagingService messagingService;
    private EnvironmentNotificationService environmentNotificationService;

    private ReportsView view = new ReportsView();
    private ReportsModel model = new ReportsModel();
    private ReportsController controller;

    private JPanel outputPanel = new JPanel();

    private String name;
    private String layout;
    private LayoutService layoutService;

    @Override public void configure(ReportsViewConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
        this.layout = configuration.getLayout();

        messagingService = discovery.findService(EnvironmentMessagingService.class, configuration.getEnvironmentRef());
        environmentNotificationService = discovery.findService(EnvironmentNotificationService.class, configuration.getEnvironmentRef());

        layoutService = discovery.findService(LayoutService.class);
        initialise();
    }

    public void initialise() {
        layoutService.add(view, layout);
        controller = new ReportsController(model, messagingService);
        view.bind(controller);
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    @Inject public void setLayoutService(LayoutService layoutService) {
        this.layoutService = layoutService;
    }

    @Inject public void setEnvironmentNotificationService(EnvironmentNotificationService environmentNotificationService) {
        this.environmentNotificationService = environmentNotificationService;
    }

    @Inject public void setMessagingService(EnvironmentMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @Override public void start() {
        environmentNotificationService.addListener(new EnvironmentNotificationListener() {
            @Override public void onHubConnectionEstablished(HubConfiguration hubConfiguration) {
            }

            @Override public void onHubConnectionLost(HubConfiguration hubConfiguration) {
            }

            @Override public void onEnvironmentConnectionLost() {
            }

            @Override public void onEnvironmentConnectionEstablished() {
            }

            @Override public void onTotalEnvironmentConnectionEstablished() {
            }

            @Override public void onTotalEnvironmentConnectionLost() {
            }
        });

        controller.requestReportList();
    }

    @Override public void stop() {
        controller.unbind();
    }

}

