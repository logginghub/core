package com.logginghub.logging.frontend.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JOptionPane;

import com.logginghub.logging.frontend.patterniseddataview.PatternisedDataViewPanel;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.frontend.services.MenuService;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Destination;

public class PatternisedDataViewModule implements Asynchronous {

    private LayoutService layoutService;
    private SocketClientDirectAccessService directAccessService;

    public PatternisedDataViewModule(MenuService menuService, SocketClientDirectAccessService directAccessService, LayoutService layoutService) {
        this.directAccessService = directAccessService;
        this.layoutService = layoutService;

        menuService.addMenuItem("View", "Add Patternised Data View Tab", new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                addTab();
            }
        });
    }

    protected void addTab() {

        List<SocketClient> directAccess = directAccessService.getDirectAccess();
        if (directAccess.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                                          "Please have a log view selected before you create a patternised view,\r\nso we know which environment to connect to",
                                          "Sorry",
                                          JOptionPane.ERROR_MESSAGE);
        }
        else {
            
            final PatternisedDataViewPanel panel = new PatternisedDataViewPanel();
            layoutService.add(panel, "Patternised Data Viewer");
            panel.start();
            
            SocketClient client = directAccess.get(0);

            client.subscribe(Channels.patternisedEventUpdates, new Destination<ChannelMessage>() {
                @Override public void send(ChannelMessage t) {
                    panel.send((PatternisedLogEvent) t.getPayload());
                }
            });
        }

    }

    @Override public void start() {}

    @Override public void stop() {}

}
