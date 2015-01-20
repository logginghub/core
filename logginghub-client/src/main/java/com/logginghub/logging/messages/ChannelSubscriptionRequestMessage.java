package com.logginghub.logging.messages;

import java.util.Arrays;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class ChannelSubscriptionRequestMessage extends BaseRequestResponseMessage implements SerialisableObject {

    private String[] channels;
    private boolean subscribe = true;

    public ChannelSubscriptionRequestMessage(int requestID, boolean subscribe, String... channels) {
        super();
        this.channels = channels;
        this.subscribe = subscribe;
        setCorrelationID(requestID);
    }

    public ChannelSubscriptionRequestMessage() {}

    public void setSubscribe(boolean subscribe) {
        this.subscribe = subscribe;
    }

    public boolean isSubscribe() {
        return subscribe;
    }

    public String[] getChannels() {
        return channels;
    }

    public void setChannels(String... channel) {
        this.channels = channel;
    }

    public void read(SofReader reader) throws SofException {
        this.subscribe = reader.readBoolean(1);
        this.channels = reader.readStringArray(2);
        this.setCorrelationID(reader.readInt(3));
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, subscribe);
        writer.write(2, channels);
        writer.write(3, getCorrelationID());
    }

    @Override public String toString() {
        return "ChannelSubscriptionRequestMessage [channels=" + Arrays.toString(channels) + ", subscribe=" + subscribe + "]";
    }

    
}
