package com.logginghub.logging.servers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.utils.logging.Logger;

public class ServerHandlerSubscriptions {

    private static final Logger logger = Logger.getLoggerFor(ServerHandlerSubscriptions.class);
    private Map<String, List<ServerMessageHandler>> subscriptions = new HashMap<String, List<ServerMessageHandler>>();
    
    private Map<String, Future<Boolean>> subscriptionFutures = new HashMap<String, Future<Boolean>>();

    public Future<Boolean> addSubscription(String channel, ServerMessageHandler counterpart) {
        channel = tidy(channel);
        List<ServerMessageHandler> list;
        synchronized (subscriptions) {
            list = subscriptions.get(channel);
            if (list == null) {
                list = new CopyOnWriteArrayList<ServerMessageHandler>();
                subscriptions.put(channel, list);
                subscriptionFutures.put(channel, handleFirstSubscription(channel));
            }
        }

        list.add(counterpart);

        Future<Boolean> future = subscriptionFutures.get(channel);
        return future;
    }

    private String tidy(String channel) {
        if (channel.endsWith("/")) {
            return channel.substring(0, channel.length() - 1);
        }
        else {
            return channel;
        }
    }

    public void removeAllSubscriptions(ServerMessageHandler handler) {
        synchronized (subscriptions) {
            List<String> emptySubscriptions = new ArrayList<String>();
            Set<Entry<String, List<ServerMessageHandler>>> entrySet = subscriptions.entrySet();
            for (Entry<String, List<ServerMessageHandler>> entry : entrySet) {
                List<ServerMessageHandler> value = entry.getValue();
                boolean removed = value.remove(handler);
                if (removed && value.size() == 0) {
                    emptySubscriptions.add(entry.getKey());
                }
            }

            for (String channel : emptySubscriptions) {
                subscriptions.remove(channel);
                handleLastSubscription(channel);
            }
        }
    }

    public void removeSubscription(String channel, ServerMessageHandler counterpart) {
        channel = tidy(channel);

        List<ServerMessageHandler> list;
        synchronized (subscriptions) {
            list = subscriptions.get(channel);

            if (list != null) {
                list.remove(counterpart);
                if (list.size() == 0) {
                    subscriptions.remove(channel);
                    handleLastSubscription(channel);
                }
            }
        }
    }

    public void dispatch(String[] toChannel, LoggingMessage message) {
        dispatch(toChannel, message, null);
    }

    public void dispatch(String[] channels, LoggingMessage message, LoggingMessageSender source) {

        StringBuilder sb = new StringBuilder();
        String div = "";
        for (String channelPart : channels) {

            sb.append(div).append(channelPart);
            div = "/";

            String channel = sb.toString();
            List<ServerMessageHandler> list;
            synchronized (subscriptions) {
                list = subscriptions.get(channel);
            }

            dispatchToList(message, source, list);

        }

        // Remember to check the global listeners too
        List<ServerMessageHandler> list;
        synchronized (subscriptions) {
            list = subscriptions.get("");
        }

        dispatchToList(message, source, list);

    }

    private void dispatchToList(LoggingMessage message, LoggingMessageSender source, List<ServerMessageHandler> list) {
        if (list != null) {

            List<ServerMessageHandler> toRemove = null;

            for (Iterator<ServerMessageHandler> iterator = list.iterator(); iterator.hasNext();) {
                ServerMessageHandler t = iterator.next();
                if (t != source) {
                    try {
                        t.onMessage(message, source);
                    }
                    catch (RuntimeException e) {
                        if (toRemove == null) {
                            toRemove = new ArrayList<ServerMessageHandler>();
                        }
                        toRemove.add(t);
                        logger.warn(e, "Failed to dispatch message '{}' to server channel handler '{}' - this handler will be removed", message, t);
                    }
                }
            }

            if (toRemove != null) {
                for (ServerMessageHandler t : toRemove) {
                    removeAllSubscriptions(t);
                }
            }
        }
    }

    public List<ServerMessageHandler> getDestinations(String... channels) {

        List<ServerMessageHandler> destinations = new ArrayList<ServerMessageHandler>();

        StringBuilder sb = new StringBuilder();
        String div = "";
        for (String channelPart : channels) {
            sb.append(div).append(channelPart);
            div = "/";

            String channel = sb.toString();
            List<ServerMessageHandler> list;
            synchronized (subscriptions) {
                list = subscriptions.get(channel);
            }

            if (list != null) {
                destinations.addAll(list);
            }
        }

        return destinations;

    }

    protected Future<Boolean> handleFirstSubscription(String channel) {
        return null;
    }

    protected Future<Boolean> handleLastSubscription(String channel) {
        return null;
    }

    public Set<String> getChannels() {
        synchronized (subscriptions) {
            return subscriptions.keySet();
        }
    }

    public boolean hasSubscriptions(String channel) {
        List<ServerMessageHandler> list;
        synchronized (subscriptions) {
            list = subscriptions.get(channel);
        }

        boolean hasSubscriptions = list != null && !list.isEmpty();
        return hasSubscriptions;
    }

    public int getSubscriptionCount(String channel) {
        List<ServerMessageHandler> list;
        synchronized (subscriptions) {
            list = subscriptions.get(channel);
        }

        int count;
        if (list == null) {
            count = 0;
        }
        else {
            count = list.size();
        }

        return count;
    }


}
