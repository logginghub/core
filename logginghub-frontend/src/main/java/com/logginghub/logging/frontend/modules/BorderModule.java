package com.logginghub.logging.frontend.modules;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

import com.logginghub.logging.frontend.modules.configuration.TabbedPaneConfiguration;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.utils.module.Container;
import com.logginghub.utils.module.Inject;

public class BorderModule extends Container<TabbedPaneConfiguration> implements LayoutService {

    private LayoutService layoutService;
    private String title;

    @Inject(direction = Inject.Direction.Parent) public void setLayoutService(LayoutService layoutService) {
        this.layoutService = layoutService;
    }

    @Override public void add(Component component, String layout) {

        if (component instanceof JComponent) {
            JComponent jComponent = (JComponent) component;
            jComponent.setBorder(BorderFactory.createTitledBorder(title));
        }

        layoutService.add(component, layout);

    }

    @Override public String toString() {
        return "BorderModule [layoutService=" + layoutService + ", title=" + title + "]";
    }

}
