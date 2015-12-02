package com.logginghub.logging.modules;

import com.logginghub.logging.VLLogEvent;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.InternalConnection;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.modules.configuration.InternalLoggingConfiguration;
import com.logginghub.logging.servers.SocketHubInterface;
import com.logginghub.utils.ProcessUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.logging.LoggerPerformanceInterface.EventContext;
import com.logginghub.utils.logging.LoggerStream;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InternalLoggingModule implements Module<InternalLoggingConfiguration> {

    private boolean once = false;
    private InternalConnection internal = new InternalConnection("InternalLoggingModule", SocketConnection.CONNECTION_TYPE_NORMAL);

    @Override public void start() {}

    @Override public void stop() {}

    @Override public void configure(InternalLoggingConfiguration configuration, ServiceDiscovery discovery) {

        final SocketHubInterface socketHubInterface = discovery.findService(SocketHubInterface.class);

        if (!once) {
            once = true;
            final String sourceApplication = System.getProperty("vllogging.sourceApplication", "LoggingHub");

            InetAddress localHost = null;
            try {
                localHost = InetAddress.getLocalHost();
            }
            catch (UnknownHostException e) {
                e.printStackTrace();
            }

            final int finalPid = ProcessUtils.getPid();
            final InetAddress finalHost = localHost;

            Logger root = com.logginghub.utils.logging.Logger.root();

            root.addStream(new LoggerStream() {
                @Override public void onNewLogEvent(com.logginghub.utils.logging.LogEvent event) {
                    VLLogEvent vlevent = new VLLogEvent(event, finalPid, sourceApplication, finalHost.getHostAddress(), finalHost.getHostName());
                    vlevent.setChannel("private/hubinternal");
                    socketHubInterface.processLogEvent(new LogEventMessage(vlevent), internal);
                }

                @Override
                public void onNewLogEvent(EventContext eventContext) {

                }
            });
        }

    }

}