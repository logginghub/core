package com.logginghub.messaging;

public interface ObjectInputStreamReaderListener
{
    void onObjectRead(Object object);
    void onStreamClosed();
}
