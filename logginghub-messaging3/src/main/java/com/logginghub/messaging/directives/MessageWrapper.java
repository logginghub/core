package com.logginghub.messaging.directives;

public class MessageWrapper {

    private String deliverToChannel;
    private String replyToChannel;

    private String deliverToLocalChannel;
    private String replyToLocalChannel;

    private int responseID = -1;

    private Object payload;

    public MessageWrapper() {}

    public MessageWrapper(String deliverToChannel, String replyToChannel, Object payload) {
        super();
        this.deliverToChannel = deliverToChannel;
        this.replyToChannel = replyToChannel;
        this.payload = payload;
    }

    public MessageWrapper(String deliverToChannel, Object payload) {
        super();
        this.deliverToChannel = deliverToChannel;
        this.payload = payload;
    }

    public String getDeliverToLocalChannel() {
        return deliverToLocalChannel;
    }

    public String getReplyToLocalChannel() {
        return replyToLocalChannel;
    }

    public void setDeliverToLocalChannel(String deliverToLocalChannel) {
        this.deliverToLocalChannel = deliverToLocalChannel;
    }

    public void setReplyToLocalChannel(String replyToLocalChannel) {
        this.replyToLocalChannel = replyToLocalChannel;
    }

    public void setResponseID(int responseID) {
        this.responseID = responseID;
    }

    public int getResponseID() {
        return responseID;
    }

    public String getDeliverToChannel() {
        return deliverToChannel;
    }

    public String getReplyToChannel() {
        return replyToChannel;
    }

    public void setDeliverToChannel(String deliverToChannel) {
        this.deliverToChannel = deliverToChannel;
    }

    public void setReplyToChannel(String replyToChannel) {
        this.replyToChannel = replyToChannel;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override public String toString() {

        String deliverTo = deliverToChannel;
        if (deliverToLocalChannel != null) {
            deliverTo += "::" + deliverToLocalChannel;
        }

        String replyTo = replyToChannel;
        if (replyToLocalChannel != null) {
            replyTo += "::" + replyToLocalChannel;
        }

        if (responseID > 0) {
            return "MessageWrapper [deliverTo=" + deliverTo + ", replyTo=" + replyTo + ", responseID=" + responseID + ", payload=" + payload + "]";
        }
        else {
            return "MessageWrapper [deliverTo=" + deliverTo + ", replyTo=" + replyTo + ", payload=" + payload + "]";
        }
    }

}
