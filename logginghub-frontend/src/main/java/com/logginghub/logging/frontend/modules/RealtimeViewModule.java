package com.logginghub.logging.frontend.modules;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.EventTableModel;
import com.logginghub.logging.frontend.model.LogEventContainerController;
import com.logginghub.logging.frontend.modules.configuration.RealtimeViewConfiguration;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.frontend.views.logeventdetail.DetailedLogEventTablePanel;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.module.Inject;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

import javax.swing.*;

public class RealtimeViewModule implements Module<RealtimeViewConfiguration> {

    private DetailedLogEventTablePanel detailedLogEventTablePanel;
    private LayoutService layoutService;
    private String name;
    private String layout;
    private EnvironmentMessagingService messagingService;
    private LogEventContainerController eventController;
    private TimeProvider timeProvider = new SystemTimeProvider();
    
    public RealtimeViewModule() {
        JMenuBar menuBar = new JMenuBar();
        String propertiesName = "realtimeView";
        eventController = new LogEventContainerController();

        EventTableModel eventTableModel = new EventTableModel();

        detailedLogEventTablePanel = new DetailedLogEventTablePanel(menuBar, propertiesName, eventTableModel, eventController, timeProvider, false);
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setLayout(String layout) {
        this.layout = layout;
    }
    
    @Inject
    public void setLayoutService(LayoutService layoutService) {
        this.layoutService = layoutService;
    }
    
    @Inject
    public void setMessagingService(EnvironmentMessagingService messagingService) {
        this.messagingService = messagingService;
    }
    
    @Override public void configure(RealtimeViewConfiguration configuration, ServiceDiscovery discovery) {
        layoutService = discovery.findService(LayoutService.class);
        messagingService = discovery.findService(EnvironmentMessagingService.class);
    }
    
    public void initialise() {
        detailedLogEventTablePanel.setName(name);
        detailedLogEventTablePanel.bind(new EnvironmentModel());
        
        layoutService.add(detailedLogEventTablePanel, layout);
        
        messagingService.addLogEventListener(new LogEventListener() {
            @Override public void onNewLogEvent(LogEvent event) {
                detailedLogEventTablePanel.onNewLogEvent(event);
            }
        });
    }

    @Override public void start() {}

    @Override public void stop() {}

    @Override public String toString() {
        return "RealtimeViewModule [name=" +
               name +
               ", layout=" +
               layout +
               ", layoutService=" +
               layoutService +
               ", messagingService=" +
               messagingService +
               "]";
    }
    
    
    
    

}
