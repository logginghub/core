package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

/**
 * I needed a version of ChannelMessage with a plain string channel in order to json encode it
 * properly. I'm not convinced the ChannelMessage should have an array, its a tuning thing to make
 * the dispatch thread quicker inside the hub. It shouldn't really be in the modle classes.
 * 
 * @author james
 *
 */
public class NonArrayChannelMessage implements SerialisableObject, LoggingMessage {

    private String channel;
    private SerialisableObject value;

    public NonArrayChannelMessage() {}

    public NonArrayChannelMessage(String channel, SerialisableObject payload) {
        super();
        this.channel = channel;
        this.value = payload;
    }

    public static NonArrayChannelMessage fromChannelMessage(ChannelMessage cm) {
        NonArrayChannelMessage message = new NonArrayChannelMessage(Channels.join(cm.getChannel()), cm.getPayload());
        return message;
    }

    public void read(SofReader reader) throws SofException {
        this.channel = reader.readString(1);
        this.value = (SerialisableObject) reader.readObject(2);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, channel);
        writer.write(2, value);
    }

    public String getChannel() {
        return channel;
    }

    public SerialisableObject getValue() {
        return value;
    }

    @Override public String toString() {
        return "ChannelMessage [channel=" + channel + ", value=" + value + "]";
    }

}
