package com.logginghub.logging.frontend.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JOptionPane;

import com.logginghub.logging.frontend.aggregateddataview.AggregatedDataViewPanel;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.frontend.services.MenuService;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Destination;

public class AggregatedDataViewModule implements Asynchronous {

    private LayoutService layoutService;
    private SocketClientDirectAccessService directAccessService;

    public AggregatedDataViewModule(MenuService menuService, SocketClientDirectAccessService directAccessService, LayoutService layoutService) {
        this.directAccessService = directAccessService;
        this.layoutService = layoutService;

        menuService.addMenuItem("View", "Add Aggregated Data View Tab", new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                addTab();
            }
        });
    }

    protected void addTab() {

        List<SocketClient> directAccess = directAccessService.getDirectAccess();
        if (directAccess.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                                          "Please have a log view selected before you create a Aggregated view,\r\nso we know which environment to connect to",
                                          "Sorry",
                                          JOptionPane.ERROR_MESSAGE);
        }
        else {
            
            final AggregatedDataViewPanel panel = new AggregatedDataViewPanel();
            layoutService.add(panel, "Aggregated Data Viewer");
            panel.start();
            
            SocketClient client = directAccess.get(0);

            client.subscribe(Channels.aggregatedEventUpdates, new Destination<ChannelMessage>() {
                @Override public void send(ChannelMessage t) {
                    panel.send((AggregatedLogEvent) t.getPayload());
                }
            });
        }

    }

    @Override public void start() {}

    @Override public void stop() {}

}
