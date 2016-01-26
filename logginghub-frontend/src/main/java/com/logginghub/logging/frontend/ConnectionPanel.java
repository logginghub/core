package com.logginghub.logging.frontend;

import com.logginghub.logging.frontend.model.HubConnectionModel;
import com.logginghub.logging.frontend.model.HubConnectionModel.ConnectionState;
import com.logginghub.utils.observable.ObservableItemContainer;
import com.logginghub.utils.observable.ObservableListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.net.InetSocketAddress;
import java.util.List;

public class ConnectionPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    public static ImageIcon green = new ImageIcon(ConnectionPanel.class.getResource("/icons/16-circle-green.png"));
    public static ImageIcon blue = new ImageIcon(ConnectionPanel.class.getResource("/icons/16-circle-blue.png"));
    public static ImageIcon red = new ImageIcon(ConnectionPanel.class.getResource("/icons/16-circle-red.png"));
    private JLabel nameLabel;
    private JLabel connectionPointlabel;

    public ConnectionPanel() {
        setLayout(new MigLayout("gap 10", "[]", "[]"));

        nameLabel = new JLabel("<name>");
        connectionPointlabel = new JLabel("<host name:port>");
        nameLabel.setIcon(green);

        connectionPointlabel.setForeground(Color.gray);

        add(nameLabel, "cell 0 0");
        add(connectionPointlabel, "cell 1 0");
    }

    public void setModel(final HubConnectionModel socketSourceModel) {

        socketSourceModel.addListener(new ObservableListener() {
            @Override
            public void onChanged(ObservableItemContainer observable, Object childPropertyThatChanged) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        update(socketSourceModel);
                    }
                });
            }
        });

        update(socketSourceModel);
    }

    protected void update(HubConnectionModel socketSourceModel) {

        nameLabel.setText(socketSourceModel.getName().get());

        String text;
        List<InetSocketAddress> clusteredConnectionPoints = socketSourceModel.getClusteredConnectionPoints();
        if (clusteredConnectionPoints.isEmpty()) {
            text = String.format("%s:%d", socketSourceModel.getHost().get(), socketSourceModel.getPort().get());
        } else {
            StringBuilder builder = new StringBuilder();
            String div = "";
            for (InetSocketAddress clusteredConnectionPoint : clusteredConnectionPoints) {
                builder.append(div)
                       .append(clusteredConnectionPoint.getHostName())
                       .append(":")
                       .append(clusteredConnectionPoint.getPort());
                div = ",";
            }

            text = builder.toString();
        }

        // jshaw - have to set the name on the label with the icon to make the tests pass
        nameLabel.setName(text);

        connectionPointlabel.setText(text);

        ImageIcon icon = null;
        ConnectionState connectionState = socketSourceModel.getConnectionState().get();
        switch (connectionState) {
            case AttemptingConnection: {
                icon = blue;
                break;
            }
            case Connected: {
                icon = green;
                break;
            }
            case NotConnected: {
                icon = red;
                break;
            }
        }

        nameLabel.setIcon(icon);
    }
}
