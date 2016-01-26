package com.logginghub.logging.frontend;

import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.HubConnectionModel;
import com.logginghub.logging.frontend.model.HubConnectionModel.ConnectionState;
import com.logginghub.logging.frontend.views.logeventdetail.DetailedLogEventTablePanel;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservablePropertyListener;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;

public class TabColourManager {

    private final JTabbedPane tabbedPane;
    private final int tabIndex;
    private final DetailedLogEventTablePanel tablePanel;
    private final EnvironmentModel environmentModel;

    public TabColourManager(JTabbedPane tabbedPane, int tabIndex, DetailedLogEventTablePanel tablePanel, EnvironmentModel environmentModel) {
        this.tabbedPane = tabbedPane;
        this.tabIndex = tabIndex;
        this.tablePanel = tablePanel;
        this.environmentModel = environmentModel;
    }

    public static void bind(JTabbedPane tabbedPane, int tabIndex, DetailedLogEventTablePanel tablePanel, EnvironmentModel environmentModel) {

        final TabColourManager manager = new TabColourManager(tabbedPane, tabIndex, tablePanel, environmentModel);

        ObservableList<HubConnectionModel> hubs = environmentModel.getHubConnectionModels();

        for (HubConnectionModel hubModel : hubs) {
            hubModel.getConnectionState().addListener(new ObservablePropertyListener<ConnectionState>() {
                @Override
                public void onPropertyChanged(ConnectionState oldValue, ConnectionState newValue) {
                    invokeResolve(manager);
                }
            });
        }

        environmentModel.getHighestLevelSinceLastSelected().addListener(new ObservablePropertyListener<Integer>() {
            @Override
            public void onPropertyChanged(Integer oldValue, Integer newValue) {
                invokeResolve(manager);
            }
        });

        manager.resolveTabColour();
    }

    protected static void invokeResolve(final TabColourManager manager) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                manager.resolveTabColour();
            }
        });
    }

    public void resolveTabColour() {

        int connected = 0;
        int connecting = 0;
        int disconnected = 0;

        ObservableList<HubConnectionModel> hubs = environmentModel.getHubConnectionModels();
        for (HubConnectionModel hubModel : hubs) {
            ConnectionState connectionState = hubModel.getConnectionState().get();
            switch (connectionState) {
                case AttemptingConnection:
                    connecting++;
                    break;
                case Connected:
                    connected++;
                    break;
                case NotConnected:
                    disconnected++;
                    break;
            }
        }

        ConnectionState overall;

        if (connected == 0) {
            if (connecting == 0) {
                overall = ConnectionState.NotConnected;
            } else {
                overall = ConnectionState.AttemptingConnection;
            }
        } else {
            overall = ConnectionState.Connected;
        }

        if (hubs.size() == 0) {
            // No hubs have been provided, so it might be a local view of a binary file?
            overall = ConnectionState.Connected;
        }

        Color backgroundColor = null;
        Color foregroundColor = null;
        switch (overall) {
            case AttemptingConnection:
                backgroundColor = Color.orange;
                foregroundColor = Color.orange;
                break;
            case Connected:
                backgroundColor = null;
                break;
            case NotConnected:
                backgroundColor = Color.black;
                foregroundColor = Color.black;
                break;
        }

        if (backgroundColor == null) {
            // Need to decide based on the highest coloured event in the logeventdetail
            // display
            int highestStateSinceLastSelected = environmentModel.getHighestLevelSinceLastSelected().get();

            if (highestStateSinceLastSelected >= Level.WARNING.intValue()) {
                backgroundColor = Utils.getBackgroundColourForLevel(highestStateSinceLastSelected);
            }

        }

        if (tabbedPane.getSelectedIndex() == tabIndex) {
            tabbedPane.setForegroundAt(tabIndex, foregroundColor);
            tabbedPane.setBackgroundAt(tabIndex, Color.gray);
        } else {
            tabbedPane.setBackgroundAt(tabIndex, backgroundColor);
            tabbedPane.setForegroundAt(tabIndex, Color.DARK_GRAY);
        }

    }

}
