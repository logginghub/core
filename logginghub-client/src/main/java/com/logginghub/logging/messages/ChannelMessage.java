package com.logginghub.logging.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.logginghub.utils.Is;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class ChannelMessage implements SerialisableObject, LoggingMessage {

    private String[] channel;
    private SerialisableObject payload;
    private int correlationID;
    private String[] replyToChannel;

    public ChannelMessage() {}

    public ChannelMessage(String channel, SerialisableObject payload) {
        super();
        setChannel(channel);
        this.payload = payload;
    }

    public void setPayload(SerialisableObject payload) {
        this.payload = payload;
    }
    
    public void setChannel(String channel) {
        this.channel = parseChannel(channel);
    }

    public static String[] parseChannelSplit(String channel) {
        String trimmed = channel.trim();
        return trimmed.split("\\\\");
    }

    public static String[] parseChannel(String channel) {
        Is.notNull(channel, "Channel cannot be null");

        String trimmed = channel.trim();

        List<String> channels = new ArrayList<String>();

        int start = 0;
        int end = 0;

        int size = trimmed.length();
        for (int i = 0; i < size; i++) {
            char c = trimmed.charAt(i);
            if (c == '/' || c == '\\') {
                channels.add(trimmed.subSequence(start, end).toString().trim());
                start = end + 1;
                end++;
            }
            else {
                end++;
            }
        }

        if (start < size) {
            channels.add(trimmed.subSequence(start, size).toString().trim());
        }

        return channels.toArray(new String[channels.size()]);

    }

    public void read(SofReader reader) throws SofException {
        this.correlationID = reader.readInt(0);
        this.channel = reader.readStringArray(1);
        this.replyToChannel = reader.readStringArray(2);
        this.payload = (SerialisableObject) reader.readObject(2);

    }

    public void write(SofWriter writer) throws SofException {
        writer.write(0, correlationID);
        writer.write(1, channel);
        writer.write(2, replyToChannel);
        writer.write(3, payload);
    }

    public String[] getChannel() {
        return channel;
    }

    public String[] getReplyToChannelArray() {
        return replyToChannel;
    }
    
    public String getReplyToChannel() {
        return Channels.join(replyToChannel);
    }
    
    public void setReplyToChannel(String[] replyToChannel) {
        this.replyToChannel = replyToChannel;
    }
    
    public void setReplyToChannel(String replyToChannel) {
        setReplyToChannel(parseChannel(replyToChannel));
    }
    
    public SerialisableObject getPayload() {
        return payload;
    }

    @Override public String toString() {
        return "ChannelMessage [channel=" + Arrays.toString(channel) + ", payload=" + payload + "]";
    }

    public int getCorrelationID() {
        return correlationID;
    }

    public void setCorrelationID(int correlationID) {
        this.correlationID = correlationID;
    }

    public String getRespondToChannel() {
        return getReplyToChannel();
         
    }

}
