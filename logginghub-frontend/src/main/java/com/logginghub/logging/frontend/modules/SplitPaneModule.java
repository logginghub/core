package com.logginghub.logging.frontend.modules;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import com.logginghub.logging.frontend.modules.configuration.TabbedPaneConfiguration;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.utils.module.Container;
import com.logginghub.utils.module.Inject;
import com.logginghub.utils.module.PeerServiceDiscovery;
import com.logginghub.utils.module.ServiceDiscovery;

public class SplitPaneModule extends Container<TabbedPaneConfiguration> implements LayoutService {

    private JSplitPane splitPane;
    private LayoutService layoutService;
    private String layout;
    private int count = 0;
    private boolean horizontal = false;
    private int dividerSize = 10;

    public SplitPaneModule() {
        splitPane = new JSplitPane();
    }

    public void setDividerSize(int dividerSize) {
        this.dividerSize = dividerSize;
    }

    public void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
    }

    @Inject public void setLayoutService(LayoutService layoutService) {
        this.layoutService = layoutService;
    }

    public void initialise() {
        // jshaw - I know, its counterintuitive...
        if (horizontal) {
            splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        }
        else {
            splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        }

        splitPane.setDividerSize(dividerSize);
        splitPane.setOneTouchExpandable(true);

        layoutService.add(splitPane, layout);

        splitPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        BasicSplitPaneUI flatDividerSplitPaneUI = new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void setBorder(Border b) {
                    }
                };
            }
        };
        splitPane.setUI(flatDividerSplitPaneUI);
    }

    @Override public void configure(TabbedPaneConfiguration configuration, ServiceDiscovery serviceDiscovery) {
        LayoutService layoutService = serviceDiscovery.findService(LayoutService.class);
        layoutService.add(splitPane, configuration.getLayout());

        // TODO : this should be moved to a generic container/module base class for the frontend?
        PeerServiceDiscovery discovery = new PeerServiceDiscovery(this, true, serviceDiscovery);
        super.configure(configuration, discovery);
    }

    @Override public void add(final Component component, String layout) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {

                if (count == 0) {
                    splitPane.setLeftComponent(component);
                    count++;
                }
                else {
                    splitPane.setRightComponent(component);
                }

                splitPane.setResizeWeight(0.5d);
                splitPane.setDividerLocation(0.5d);
            }
        });
    }

}
