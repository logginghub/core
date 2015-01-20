package com.logginghub.logging.frontend.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.logginghub.logging.frontend.instanceview.InstanceViewPanel;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.frontend.services.MenuService;

public class InstanceViewModule {

    private LayoutService layoutService;
    private SocketClientDirectAccessService directAccessService;

    public InstanceViewModule(MenuService menuService, SocketClientDirectAccessService directAccessService, LayoutService layoutService) {
        this.directAccessService = directAccessService;
        this.layoutService = layoutService;

        menuService.addMenuItem("View", "Add Instance View Tab", new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                addTab();
            }
        });
    }

    protected void addTab() {

        InstanceViewPanel panel = new InstanceViewPanel();
        // TODO : flakey much?
        panel.setInstanceManagementAPI(directAccessService.getDirectAccess().get(0).getInstanceManagementAPI());
        
        layoutService.add(panel, "Instance Viewer");
        
        
        panel.start();
        
    }

}
