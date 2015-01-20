package com.logginghub.logging.frontend.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.logginghub.logging.frontend.modules.configuration.MenuBarConfiguration;
import com.logginghub.logging.frontend.services.FrameService;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.frontend.services.MenuService;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.module.Inject;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class MenuBarModule implements Module<MenuBarConfiguration>, MenuService {

    private JMenuBar menuBar;
    private JMenu menuFile = new JMenu("File");
    private LayoutService layoutService;
    private FrameService frameService;

    private boolean quitOnExit = false;
    private boolean fileMenu = true;
    
    private Map<String, JMenu> topLevelMenusByName = new FactoryMap<String, JMenu>() {
        @Override protected JMenu createEmptyValue(String key) {
            return new JMenu(key);
        }};
    
    public MenuBarModule() {
        menuBar = new JMenuBar();
    }

    @Inject public void setLayoutService(LayoutService layoutService) {
        this.layoutService = layoutService;
    }

    @Inject public void setFrameService(FrameService frameService) {
        this.frameService = frameService;
    }

    @Override public void configure(MenuBarConfiguration configuration, ServiceDiscovery discovery) {
        layoutService = discovery.findService(LayoutService.class);
        frameService = discovery.findService(FrameService.class);
        initialise();
    }

    public void initialise() {

        layoutService.add(menuBar, null);

        if (this.fileMenu) {
            menuFile.setMnemonic('f');

            JMenuItem exitMenuItem = new JMenuItem("Exit");
            exitMenuItem.setMnemonic('x');
            exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
            exitMenuItem.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    frameService.dispose();
                    if(quitOnExit) {
                        System.exit(-1);
                    }
                        
                }
            });
            menuFile.add(exitMenuItem);
            menuBar.add(menuFile);
        }

    }

    @Override public void start() {}

    @Override public void stop() {}

    @Override public void addMenuItem(String topLevel, String secondLevel, ActionListener action) {
        JMenu menu = getTopLevel(topLevel);
        JMenuItem item = new JMenuItem(secondLevel);

        item.addActionListener(action);
        menu.add(item);
    }

    private JMenu getTopLevel(String topLevel) {
        return topLevelMenusByName.get(topLevel);
    }

}
