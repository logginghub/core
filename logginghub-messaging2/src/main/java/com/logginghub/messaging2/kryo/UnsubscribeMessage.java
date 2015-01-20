package com.logginghub.messaging2.kryo;

public class UnsubscribeMessage {
    private String channelID;

    public UnsubscribeMessage(){
        
    }
    
    public UnsubscribeMessage(String channelID) {
        super();
        this.channelID = channelID;
    }
    
    public String getChannelID() {
        return channelID;
    }
}
