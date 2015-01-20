package com.logginghub.messaging2.kryo;

public class SubscribeMessage {
    private String channelID;

    public SubscribeMessage(){
        
    }
    
    public SubscribeMessage(String channelID) {
        super();
        this.channelID = channelID;
    }
    
    public String getChannelID() {
        return channelID;
    }
}
