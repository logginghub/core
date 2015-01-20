package com.logginghub.logging.frontend.modules;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.services.EnvironmentNotificationService;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.frontend.telemetry.IOStatTelemetryPanel;
import com.logginghub.logging.frontend.telemetry.MachineTelemetryPanel;
import com.logginghub.logging.frontend.telemetry.ProcessTelemetryPanel;
import com.logginghub.logging.frontend.telemetry.VMStatTelemetryPanel;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.utils.Destination;
import com.logginghub.utils.IntegerStat;
import com.logginghub.utils.StatBundle;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Inject;

public class TelemetryViewModule {

    private static final Logger logger = Logger.getLoggerFor(TelemetryViewModule.class);

    final MachineTelemetryPanel machineTelemetryPanel = new MachineTelemetryPanel();
    final ProcessTelemetryPanel processTelemetryPanel = new ProcessTelemetryPanel();
    final VMStatTelemetryPanel vmstatTelemetryPanel = new VMStatTelemetryPanel();
    final IOStatTelemetryPanel iostatTelemetryPanel = new IOStatTelemetryPanel();

    private JPanel panel;
    private String connectionPoints = "localhost";
    private String layout;
    private LayoutService layoutService;

    public enum Transport {
        Messaging3,
        Hub
    }

    private Transport transport = Transport.Messaging3;

    private IntegerStat updates;

    private EnvironmentNotificationService environmentNotificationService;

    private EnvironmentMessagingService messagingService;

    public TelemetryViewModule() {

        JTabbedPane machinePane = new JTabbedPane();
        machinePane.addTab("Sigar", machineTelemetryPanel);
        machinePane.addTab("Vmstat", vmstatTelemetryPanel);
        machinePane.addTab("Iostat", iostatTelemetryPanel);
        machinePane.addTab("Process", processTelemetryPanel);

        panel = new JPanel(new MigLayout("", "", ""));
        panel.add(machinePane);
    }

    @Inject public void setLayoutService(LayoutService layoutService) {
        this.layoutService = layoutService;
    }

    @Inject public void setMessagingService(EnvironmentMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @Inject public void setEnvironmentNotificationService(EnvironmentNotificationService environmentNotificationService) {
        this.environmentNotificationService = environmentNotificationService;
    }

    public void initialise() {

        layoutService.add(panel, layout);
        
        messagingService.subscribe("updates/telemetry", new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
                DataStructure data = (DataStructure) t.getPayload();
                vmstatTelemetryPanel.update(data);
                iostatTelemetryPanel.update(data);
                machineTelemetryPanel.update(data);
                processTelemetryPanel.update(data);
            }
        });

    }

    public void start() {
        StatBundle bundle = new StatBundle();
        updates = bundle.createStat("Updates");
        updates.setIncremental(true);
        bundle.startPerSecond(logger);

        WorkerThread.execute("LoggingHub-telemetryViewConnector", new Runnable() {
            @Override public void run() {
                connect();
            }
        });
    }

    private void connect() {

        // if (transport == Transport.Messaging3) {
        // List<InetSocketAddress> inetSocketAddressList =
        // NetUtils.toInetSocketAddressList(connectionPoints,
        // VLPorts.getTelemetryMessaging3HubDefaultPort());
        //
        // Messaging3TelemetryClient tc = new Messaging3TelemetryClient();
        // // TODO : do we actually want to add any generators to this?!
        // tc.start(inetSocketAddressList);
        //
        // for (InetSocketAddress inetSocketAddress : inetSocketAddressList) {
        // Messaging3TelemetryListener listener = new Messaging3TelemetryListener();
        // listener.start(inetSocketAddress, new TelemetryInterface() {
        // public void publishTelemetry(TelemetryData data) {
        // logger.debug("host [{}] process [{}]", data.getKey(TelemetryKeyElement.host),
        // data.getKey(TelemetryKeyElement.processName));
        // updates.increment();
        // // TODO : refactor to use the new stats approach
        // machineTelemetryPanel.update(data);
        // processTelemetryPanel.update(data);
        // }
        //
        // @Override public void publishTelemetry(DataStructure dataStructure) {
        // if (dataStructure.containsValue(Values.VMSTAT_CPU_Idle)) {
        // vmstatTelemetryPanel.update(dataStructure);
        // }
        //
        // if (dataStructure.containsValue(Values.IOSTAT_Average_Request_Queue_Length)) {
        // iostatTelemetryPanel.update(dataStructure);
        // }
        // }
        // });
        // }
        // }
    }

    private static void startSwingChecker() {
        TimerUtils.everySecond("SwingCheck", new Runnable() {
            long time;

            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        long now = System.currentTimeMillis();
                        long delta = now - time;
                        logger.info("Swing is still processing {}", delta);
                        time = now;
                    }
                });
            }
        });
    }
}
