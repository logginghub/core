package com.logginghub.messaging.directives;

public class SubscribeMessage {
    private String channel;

    public SubscribeMessage(String channel) {
        this.channel = channel;}

    public SubscribeMessage() {}
    
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @Override public String toString() {
        return "SubscribeMessage [channel=" + channel + "]";
    }

    
    
}
