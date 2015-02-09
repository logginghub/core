package com.logginghub.logging.messaging;

import com.logginghub.utils.Destination;
import com.logginghub.utils.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

/**
 * Quite a complex undertaking with lots of generics that was intended to abstract channel subscriptions for both hub and client. Its become a little bit to hard to work with though, too complex!
 *
 * @param <T>
 * @param <K>
 * @author james
 */
public abstract class SubscriptionController<T extends Destination<K>, K> {

    private static final Logger logger = Logger.getLoggerFor(SubscriptionController.class);
    private Map<String, List<T>> subscriptions = new HashMap<String, List<T>>();
    private Map<String, Future<Boolean>> subscriptionFutures = new HashMap<String, Future<Boolean>>();

    public Future<Boolean> addSubscription(String channel, T counterpart) {
        channel = tidy(channel);
        List<T> list;
        synchronized (subscriptions) {
            list = subscriptions.get(channel);
            if (list == null) {
                list = new CopyOnWriteArrayList<T>();
                subscriptions.put(channel, list);
                subscriptionFutures.put(channel, handleFirstSubscription(channel));
            }
        }

        if (!list.contains(counterpart)) {
            list.add(counterpart);
        }

        Future<Boolean> future = subscriptionFutures.get(channel);
        return future;
    }

    private String tidy(String channel) {
        if (channel.endsWith("/")) {
            return channel.substring(0, channel.length() - 1);
        } else {
            return channel;
        }
    }

    public void removeAllSubscriptions(T counterpart) {
        synchronized (subscriptions) {
            List<String> emptySubscriptions = new ArrayList<String>();
            Set<Entry<String, List<T>>> entrySet = subscriptions.entrySet();
            for (Entry<String, List<T>> entry : entrySet) {
                List<T> value = entry.getValue();
                boolean removed = value.remove(counterpart);
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

    public void removeSubscription(String channel, T counterpart) {
        channel = tidy(channel);

        List<T> list;
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

    public void dispatch(String[] toChannel, K message) {
        dispatch(toChannel, message, null);
    }

    public void dispatch(String[] channels, K message, T source) {

        StringBuilder sb = new StringBuilder();
        String div = "";
        for (String channelPart : channels) {

            sb.append(div).append(channelPart);
            div = "/";

            String channel = sb.toString();
            List<T> list;
            synchronized (subscriptions) {
                list = subscriptions.get(channel);
            }

            dispatchToList(message, source, list);

        }

        // Remember to check the global listeners too
        List<T> list;
        synchronized (subscriptions) {
            list = subscriptions.get("");
        }

        dispatchToList(message, source, list);

    }

    private void dispatchToList(K message, T source, List<T> list) {
        if (list != null) {

            List<T> toRemove = null;

            for (Iterator<T> iterator = list.iterator(); iterator.hasNext(); ) {
                T t = iterator.next();
                if (t != source) {
                    try {
                        t.send(message);
                    } catch (RuntimeException e) {
                        logger.warning(e, "Failed to dispatch to destination");
                        if (toRemove == null) {
                            toRemove = new ArrayList<T>();
                        }
                        toRemove.add(t);
                    }
                }
            }

            if (toRemove != null) {
                for (T t : toRemove) {
                    removeAllSubscriptions(t);
                }
            }
        }
    }

    public List<T> getDestinations(String... channels) {

        List<T> destinations = new ArrayList<T>();

        StringBuilder sb = new StringBuilder();
        String div = "";
        for (String channelPart : channels) {
            sb.append(div).append(channelPart);
            div = "/";

            String channel = sb.toString();
            List<T> list;
            synchronized (subscriptions) {
                list = subscriptions.get(channel);
            }

            if (list != null) {
                destinations.addAll(list);
            }
        }

        return destinations;

    }

    protected abstract Future<Boolean> handleFirstSubscription(String channel);

    protected abstract Future<Boolean> handleLastSubscription(String channel);

    public Set<String> getChannels() {
        synchronized (subscriptions) {
            return subscriptions.keySet();
        }
    }

    public boolean hasSubscriptions(String channel) {
        List<T> list;
        synchronized (subscriptions) {
            list = subscriptions.get(channel);
        }

        boolean hasSubscriptions = list != null && !list.isEmpty();
        return hasSubscriptions;
    }

    public int getSubscriptionCount(String channel) {
        List<T> list;
        synchronized (subscriptions) {
            list = subscriptions.get(channel);
        }

        int count;
        if (list == null) {
            count = 0;
        } else {
            count = list.size();
        }

        return count;
    }

}
