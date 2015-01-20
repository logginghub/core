package com.logginghub.logging.frontend.patterniseddataview;

import java.net.InetSocketAddress;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Destination;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.swing.TestFrame;

public class PatternisedDataViewPanel extends JPanel implements Asynchronous, Destination<PatternisedLogEvent> {

    private PatternisedDataViewTableModel model = new PatternisedDataViewTableModel();
    private PatternisedDataViewTable table = new PatternisedDataViewTable(model);

    private WorkerThread timer;

    public PatternisedDataViewPanel() {
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
    
    @Override public void send(PatternisedLogEvent t) {
        model.addToBatch(t);
    }

    public static void main(String[] args) throws ConnectorException {

        SocketClient client = new SocketClient();
        client.addConnectionPoint(new InetSocketAddress(VLPorts.getSocketHubDefaultPort()));

        client.connect();

        final PatternisedDataViewPanel panel = new PatternisedDataViewPanel();
        client.subscribe(Channels.patternisedEventUpdates, new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                panel.send((PatternisedLogEvent) t.getPayload());
            }
        });

        panel.start();

        TestFrame.show(panel, 640, 480);

    }

}
