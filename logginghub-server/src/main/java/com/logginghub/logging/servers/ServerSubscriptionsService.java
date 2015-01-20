package com.logginghub.logging.servers;


public interface ServerSubscriptionsService {
    void subscribe(String channel, ServerMessageHandler handler);
    void unsubscribe(String channel, ServerMessageHandler handler);
}
