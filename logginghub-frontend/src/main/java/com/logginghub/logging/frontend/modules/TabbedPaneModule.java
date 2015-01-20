package com.logginghub.logging.frontend.modules;

import java.awt.Component;

import javax.swing.JTabbedPane;

import com.logginghub.logging.frontend.modules.configuration.TabbedPaneConfiguration;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.utils.module.Container;
import com.logginghub.utils.module.Inject;
import com.logginghub.utils.module.PeerServiceDiscovery;
import com.logginghub.utils.module.ServiceDiscovery;

public class TabbedPaneModule extends Container<TabbedPaneConfiguration> implements LayoutService {

    private JTabbedPane tabbedPane;
    private LayoutService layoutService;
    private String layout;

    public TabbedPaneModule() {
        tabbedPane = new JTabbedPane();
    }

    @Inject public void setLayoutService(LayoutService layoutService) {
        this.layoutService = layoutService;
    }

    public void initialise() {
        layoutService.add(tabbedPane, layout);
    }

    @Override public void configure(TabbedPaneConfiguration configuration, ServiceDiscovery serviceDiscovery) {
        LayoutService layoutService = serviceDiscovery.findService(LayoutService.class);
        layoutService.add(tabbedPane, configuration.getLayout());

        // TODO : this should be moved to a generic container/module base class for the frontend?
        PeerServiceDiscovery discovery = new PeerServiceDiscovery(this, true, serviceDiscovery);
        super.configure(configuration, discovery);
    }

    @Override public void add(Component component, String layout) {
        tabbedPane.add(component.getName(), component);
    }

}
