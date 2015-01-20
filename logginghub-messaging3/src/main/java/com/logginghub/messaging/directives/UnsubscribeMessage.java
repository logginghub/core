package com.logginghub.messaging.directives;

public class UnsubscribeMessage {
    private String channel;

    public UnsubscribeMessage(String channel) {
        this.channel = channel;}

    public UnsubscribeMessage() {}
    
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
