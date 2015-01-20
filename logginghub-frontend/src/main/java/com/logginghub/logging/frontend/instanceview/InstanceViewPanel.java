package com.logginghub.logging.frontend.instanceview;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.api.patterns.InstanceManagementAPI;
import com.logginghub.logging.api.patterns.PingResponse;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.DelayedAction;
import com.logginghub.utils.Destination;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.swing.TestFrame;

public class InstanceViewPanel extends JPanel implements Asynchronous {

    private InstanceViewTableModel model = new InstanceViewTableModel();
    private InstanceViewTable table = new InstanceViewTable(model);

    private Map<InstanceKey, InstanceInfo> instanceInfoByKey = new HashMap<InstanceKey, InstanceInfo>();
    private InstanceManagementAPI instanceManagementAPI;
    private WorkerThread timer;

    
    public InstanceViewPanel() {
        setLayout(new MigLayout("", "[grow]", "[][grow]"));

        model.setAsync(false);

        JPanel filters = new JPanel(new MigLayout("", "[grow][grow][grow][grow][grow]", "[][]"));
        filters.setBorder(BorderFactory.createTitledBorder("Filters"));
        
        JLabel lblNewLabel = new JLabel("Name");
        filters.add(lblNewLabel, "cell 0 0");

        JLabel lblNewLabel_2 = new JLabel("Host");
        filters.add(lblNewLabel_2, "cell 1 0");

        JLabel lblNewLabel_3 = new JLabel("IP");
        filters.add(lblNewLabel_3, "cell 2 0");

        JLabel lblNewLabel_1 = new JLabel("Pid");
        filters.add(lblNewLabel_1, "cell 3 0");

        JLabel lblNewLabel_4 = new JLabel("Port");
        filters.add(lblNewLabel_4, "cell 4 0");

        final JTextField nameFilter = new JTextField();
        filters.add(nameFilter, "cell 0 1,growx");
        nameFilter.setColumns(10);

        final JTextField hostFilter = new JTextField();
        filters.add(hostFilter, "cell 1 1,growx");
        hostFilter.setColumns(10);

        final JTextField pidFilter = new JTextField();
        filters. add(pidFilter, "cell 3 1,growx");
        pidFilter.setColumns(10);

        final JTextField portFilter = new JTextField();
        filters.add(portFilter, "cell 4 1,growx");
        portFilter.setColumns(10);

        add(new JScrollPane(table), "cell 0 1,grow");

        final JTextField ipFilter = new JTextField();
        filters.add(ipFilter, "flowx,cell 2 1,growx");
        ipFilter.setColumns(10);

        add(filters, "cell 0 0,growx,aligny top");
        
        final InstanceInfoContainsFilter filter = new InstanceInfoContainsFilter();
        
        final Runnable runnable = new Runnable() {
            @Override public void run() {
                filter.setPortFilter(portFilter.getText());
                filter.setHostFilter(hostFilter.getText());
                filter.setPidFilter(pidFilter.getText());
                filter.setNameFilter(nameFilter.getText());
                filter.setIPFilter(ipFilter.getText());
                
                model.setFilter(filter);
            }
        };
        
        final DelayedAction delayedAction = new DelayedAction(50, TimeUnit.MILLISECONDS);
        KeyListener listener = new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                delayedAction.execute(runnable);
            }
        };

        portFilter.addKeyListener(listener);
        hostFilter.addKeyListener(listener);
        nameFilter.addKeyListener(listener);
        pidFilter.addKeyListener(listener);
        ipFilter.addKeyListener(listener);

    }

    public void setInstanceManagementAPI(InstanceManagementAPI instanceManagementAPI) {
        this.instanceManagementAPI = instanceManagementAPI;
    }

    private void handleResponse(PingResponse t) {
        InstanceKey key = new InstanceKey();
        key.setAddress(t.getInstanceDetails().getHostIP());
        key.setLocalPort(t.getInstanceDetails().getLocalPort());
        key.setInstanceName(t.getInstanceDetails().getInstanceName());

        InstanceInfo instanceInfo = instanceInfoByKey.get(key);

        boolean newItem;

        if (instanceInfo == null) {
            instanceInfo = new InstanceInfo();
            instanceInfoByKey.put(key, instanceInfo);
            newItem = true;
        }
        else {
            newItem = false;
        }

        instanceInfo.setDelay(0);
        instanceInfo.setHost(t.getInstanceDetails().getHostname());
        instanceInfo.setIp(t.getInstanceDetails().getHostIP());
        instanceInfo.setLocalPort(t.getInstanceDetails().getLocalPort());
        instanceInfo.setName(t.getInstanceDetails().getInstanceName());
        instanceInfo.setPid(t.getInstanceDetails().getPid());
        instanceInfo.setDelay(System.currentTimeMillis() - t.getTimestamp());
        instanceInfo.setLastResponse(System.currentTimeMillis());

        if (newItem) {
            model.addToBatch(instanceInfo);
        }
        else {
            // panel.model.notifyUpdated(instanceInfo);
        }
    }

    public static void main(String[] args) throws ConnectorException {

        SocketClient client = new SocketClient();
        client.addConnectionPoint(new InetSocketAddress(VLPorts.getSocketHubDefaultPort()));

        client.connect();

        final InstanceManagementAPI instanceManagementAPI = client.getInstanceManagementAPI();
        final InstanceViewPanel panel = new InstanceViewPanel();
        panel.setInstanceManagementAPI(instanceManagementAPI);
        panel.start();

        TestFrame.show(panel, 640, 480);

    }

    @Override public void start() {
        stop();

        instanceManagementAPI.addPingListener(new Destination<PingResponse>() {
            @Override public void send(PingResponse t) {
                handleResponse(t);
            }
        });

        timer = WorkerThread.everySecond("Pinger", new Runnable() {
            @Override public void run() {
                instanceManagementAPI.sendPing();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        if (model.getRowCount() > 0) {
                            model.fireTableRowsUpdated(0, model.getRowCount() - 1);
                        }
                    }
                });
            }
        });
    }

    @Override public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }
}
