package com.logginghub.logging.frontend.aggregateddataview;

import java.net.InetSocketAddress;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Destination;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.swing.TestFrame;

public class AggregatedDataViewPanel extends JPanel implements Asynchronous, Destination<AggregatedLogEvent> {

    private AggregatedDataViewTableModel model = new AggregatedDataViewTableModel();
    private AggregatedDataViewTable table = new AggregatedDataViewTable(model);

    private WorkerThread timer;

    public AggregatedDataViewPanel() {
        setLayout(new MigLayout("", "[grow]", "[][grow]"));

        model.setAsync(false);

        add(new JScrollPane(table), "cell 0 1,grow");
    }
    
    @Override public void start() {
        model.start();
        stop();
    }
    
    @Override public void stop() {
        model.stop();
    }
    
    @Override public void send(AggregatedLogEvent t) {
        model.addToBatch(t);
    }

    public static void main(String[] args) throws ConnectorException {

        SocketClient client = new SocketClient();
        client.addConnectionPoint(new InetSocketAddress(VLPorts.getSocketHubDefaultPort()));

        client.connect();

        final AggregatedDataViewPanel panel = new AggregatedDataViewPanel();
        client.subscribe(Channels.aggregatedEventUpdates, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                panel.send((AggregatedLogEvent) t.getPayload());
            }
        });

        panel.start();

        TestFrame.show(panel, 640, 480);

    }

}
