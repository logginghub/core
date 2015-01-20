package com.logginghub.messaging_experimental;

public class Message
{
    private byte[] payload;
    private int fromID;
    private int toID;
    private int correlationID;
    private int payloadID;

    public void setCorrelationID(int correlationID)
    {
        this.correlationID = correlationID;
    }

    public void setFromID(int fromID)
    {
        this.fromID = fromID;
    }

    public void setPayload(byte[] payload)
    {
        this.payload = payload;
    }

    public void setPayloadID(int payloadID)
    {
        this.payloadID = payloadID;
    }

    public void setToID(int toID)
    {
        this.toID = toID;
    }

    public byte[] getPayload()
    {
        return payload;
    }

    public int getCorrelationID()
    {
        return correlationID;
    }

    public int getFromID()
    {
        return fromID;
    }

    public int getPayloadID()
    {
        return payloadID;
    }

    public int getToID()
    {
        return toID;
    }

}
