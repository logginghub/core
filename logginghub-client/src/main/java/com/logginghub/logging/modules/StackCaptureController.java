package com.logginghub.logging.modules;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.InstanceKey;
import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messages.StackStrobeRequest;
import com.logginghub.logging.utils.StackCapture;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

import java.util.concurrent.TimeUnit;

public class StackCaptureController {

    private static final Logger logger = Logger.getLoggerFor(StackCaptureController.class);
    private WorkerThread timer;
    private StackCapture capture = new StackCapture();
    private LoggingMessageSender loggingMessageSender;
    private boolean respondToRequests;

    private long snapshotBroadcastInterval;
    private long snapshotRequestInterval;
    private InstanceKey instanceKey;

    public StackCaptureController() {
    }

    public void configure(LoggingMessageSender loggingMessageSender,
                          long snapshotBroadcastInterval,
                          long snapshotRequestInterval,
                          boolean respondToRequests,
                          InstanceKey instanceKey) {
        this.loggingMessageSender = loggingMessageSender;
        this.snapshotBroadcastInterval = snapshotBroadcastInterval;
        this.snapshotRequestInterval = snapshotRequestInterval;
        this.respondToRequests = respondToRequests;
        this.instanceKey = instanceKey;
    }

    public synchronized void start() {
        stop();

        if (snapshotBroadcastInterval > 0) {
            timer = WorkerThread.every("LoggingHub-stackCaptureThread",
                    snapshotBroadcastInterval,
                    TimeUnit.MILLISECONDS,
                    new Runnable() {
                        public void run() {
                            takeSnapshot();
                        }
                    });
        }

        if (snapshotRequestInterval > 0) {
            timer = WorkerThread.every("LoggingHub-stackRequestThread",
                    snapshotRequestInterval,
                    TimeUnit.MILLISECONDS,
                    new Runnable() {
                        public void run() {
                            sendSnapshotRequest();
                        }
                    });
        }

    }

    public synchronized void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        } else {
        }
    }

    protected void takeSnapshot() {

        StackSnapshot snapshot = capture.capture(instanceKey);
        ChannelMessage channelMessage = new ChannelMessage(Channels.stackSnapshots, snapshot);

        logger.debug("Sending snapshot response...");

        try {
            loggingMessageSender.send(channelMessage);
        } catch (LoggingMessageSenderException e) {

        }

    }

    protected void sendSnapshotRequest() {

        StackStrobeRequest request = new StackStrobeRequest();
        request.setInstanceSelector("*");
        request.setSnapshotCount(1);

        ChannelMessage channelMessage = new ChannelMessage(Channels.stackStrobeRequests, request);

        logger.debug("Sending snapshot request...");

        try {
            loggingMessageSender.send(channelMessage);
        } catch (LoggingMessageSenderException e) {
        }

    }

    public void executeStrobe(final StackStrobeRequest request) {
        if (respondToRequests) {
            WorkerThread.executeDaemon("LoggingHub-strobeExecutor", new Runnable() {
                public void run() {
                    int snapshotCount = request.getSnapshotCount();
                    if (snapshotCount > 0) {
                        long intervalLength = request.getIntervalLength();
                        long sleepDuration = intervalLength / snapshotCount;
                        for (int i = 0; i < snapshotCount; i++) {
                            takeSnapshot();
                            ThreadUtils.sleep(sleepDuration);
                        }
                    } else {
                        takeSnapshot();
                    }

                    logger.debug("Strobe '{}' complete", request);
                }
            });
        }
    }

}
