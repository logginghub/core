package com.logginghub.logging.servers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.logging.messaging.SocketConnectionInterface;
import com.logginghub.utils.ConcurrentFactoryMap;
import com.logginghub.utils.Destination;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;

public class ChannelMultiplexer implements Destination<LogEvent> {

    private static final Logger logger = Logger.getLoggerFor(ChannelMultiplexer.class);

    private ConcurrentFactoryMap<String, List<Destination<LogEvent>>> channelSubscriptions = new ConcurrentFactoryMap<String, List<Destination<LogEvent>>>() {
        @Override protected List<Destination<LogEvent>> createEmptyValue(String key) {
            return new CopyOnWriteArrayList<Destination<LogEvent>>();
        }
    };

    public List<Destination<LogEvent>> getChannelSubscribedConnections(String channel) {
        return channelSubscriptions.get(channel);
    }

    public boolean send(LogEvent logEvent, SocketConnectionInterface source) {
        boolean sent = false;
        String channel = logEvent.getChannel();        
        if (StringUtils.isNotNullOrEmpty(channel)) {

            StringBuilder fullChannel = new StringBuilder();

            String[] split = channel.split("/");
            for (String subChannel : split) {
                fullChannel.append(subChannel);
                String subChannelName = fullChannel.toString();

                List<Destination<LogEvent>> onlyIfExists = channelSubscriptions.getOnlyIfExists(subChannelName);
                if (onlyIfExists != null) {
                    for (Destination<LogEvent> connection : onlyIfExists) {
                        if (connection != source) {
                            connection.send(logEvent);
                            sent = true;
                        }                   
                    }
                }

                fullChannel.append("/");
            }
        }
        
        return sent;
    }

    public void send(LogEvent logEvent) {
        send(logEvent, null);
    }

    public void subscribe(String channel, Destination<LogEvent> source) {

        List<Destination<LogEvent>> list = channelSubscriptions.get(channel);
        if (list.contains(source)) {
            logger.fine("Not subscribing '{}' to channel '{}' - it is already subscribed", source, channel);
        }
        else {
            logger.fine("Subscribing '{}' to channel '{}'", source, channel);
            list.add(source);
        }

    }

    public void unsubcribe(String channel, Destination<LogEvent> source) {

        List<Destination<LogEvent>> list = channelSubscriptions.get(channel);
        if (list.contains(source)) {
            logger.fine("Unsubscribing '{}' from channel '{}'", source, channel);
            // TODO : channel string memory leak?
            list.remove(source);
        }
        else {
            logger.fine("Not unsubscribing '{}' from channel '{}' - this client was never subscribed", source, channel);
        }

    }

}
