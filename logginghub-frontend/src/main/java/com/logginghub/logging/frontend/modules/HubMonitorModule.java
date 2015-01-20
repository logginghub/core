package com.logginghub.logging.frontend.modules;

import java.awt.Color;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.HistoricalDataRequest;
import com.logginghub.logging.messages.HistoricalDataResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketConnectorListener;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.MutableLong;
import com.logginghub.utils.Out;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Container2.Hint;

public class HubMonitorModule implements Asynchronous {
    private static final Logger logger = Logger.getLoggerFor(HubMonitorModule.class);

    private String host;
    private String name;
    private int port = VLPorts.getSocketHubDefaultPort();

    private JLabel label;
    private WorkerThread thread;

    private String layout;

    private SocketClient client;

    private int nextRequestID;

    public HubMonitorModule(LayoutService layoutService, @Hint(attribute = "layout") String layout) {
        this.layout = layout;
        label = new JLabel("", JLabel.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.orange);
        layoutService.add(label, layout);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setName(String name) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                label.setText(StringUtils.format("{} {} {} ms", host, "?", "?"));
            }
        });

        this.name = name;
    }

    @Override public void start() {
        stop();

        client = new SocketClient();
        client.addConnectionPoint(new InetSocketAddress(host, port));
        client.setAutoGlobalSubscription(false);
        client.setAutoSubscribe(false);

        client.getConnector().addSocketConnectorListener(new SocketConnectorListener() {
            @Override public void onConnectionLost(final String reason) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        label.setText(StringUtils.format("Not connected {}:{} : {}", host, port, reason));
                        label.setBackground(Color.red);
                    }
                });
            }

            @Override public void onConnectionEstablished() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        label.setText(StringUtils.format("Connected {}:{}", host, port));
                        label.setBackground(Color.green);
                    }
                });
            }
        });

        thread = WorkerThread.everySecond("HubCheck-" + name, new Runnable() {
            public void run() {
                try {
                    if (!client.isConnected()) {
                        logger.info("Connecting to hub '{}:{}'...", host, port);
                        client.connect();
                        logger.info("Connected");
                    }
                    else {

                        makeHistoricalRquest(client);

                    }
                }
                catch (ConnectorException e) {
                    label.setText(StringUtils.format("Not connected {}:{} : {}", host, port, e.getMessage()));
                    label.setBackground(Color.red);
                }
            }
        });

    }

    protected void makeHistoricalRquest(SocketClient client) {
        // Construct the history data request message to send to the hub
        HistoricalDataRequest request = new HistoricalDataRequest();
        final int requestID = nextRequestID++;
        request.setCorrelationID(requestID);

        // We'll request the last 5 minutes of data
        long now = System.currentTimeMillis();
        request.setStart(TimeUtils.before(now, "10 minutes"));
        request.setEnd(now);

        // The request will execute asynchronously in one or more batch updates, so if we want to
        // wait for the response to complete we'll need to coordinate between the threads
        final CountDownLatch latch = new CountDownLatch(1);

        // We can use some counters to track the process of the request
        final MutableInt batches = new MutableInt(0);
        final MutableInt count = new MutableInt(0);

        final MutableLong earliest = new MutableLong(Long.MAX_VALUE);
        final MutableLong latest = new MutableLong(Long.MIN_VALUE);

        // We need to bind to the message receiver to pick out the HistoricalDataResponse messages
        LoggingMessageListener listener = new LoggingMessageListener() {

            @Override public void onNewLoggingMessage(LoggingMessage message) {

                if (message instanceof HistoricalDataResponse) {
                    HistoricalDataResponse response = (HistoricalDataResponse) message;
                    if (response.getCorrelationID() == requestID) {

                        DefaultLogEvent[] events = response.getEvents();

                        for (DefaultLogEvent defaultLogEvent : events) {
                            // This is where you add your code to consume the historical events
                            // System.out.println(defaultLogEvent);
                            earliest.min(defaultLogEvent.getTime());
                            latest.max(defaultLogEvent.getTime());
                        }

                        count.value += events.length;
                        batches.value++;

                        // The isLastBatch field indidicates when all of the data has been received
                        if (!response.isLastBatch()) {
                            // System.out.println("=== more to follow ===");
                        }
                        else {
                            // System.out.println("======================");

                            // This is the final batch, so notify the main thread we are done
                            latch.countDown();
                        }
                    }
                }

            }

        };
        client.addLoggingMessageListener(listener);

        Stopwatch timer = Stopwatch.start("History request");

        // Send the request - note that we always register the listener _before_ sending the request
        // to avoid race conditions.
        try {
            client.send(request);
        }
        catch (LoggingMessageSenderException e) {
            e.printStackTrace();
        }

        // Block the main thread and wait for the response to arrive
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        timer.stop();
        client.removeLoggingMessageListener(listener);

        Out.out("Received {} events in {} batches in {} ms : times between {} and {}",
                count,
                batches,
                timer.getFormattedDurationMillis(),
                Logger.toDateString(earliest.value),
                Logger.toDateString(latest.value));

    }

    @Override public void stop() {

        if (thread != null) {
            thread.dontRunAgain();
        }

        if (client != null) {
            client.disconnect();
        }

        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }
}
