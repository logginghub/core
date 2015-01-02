package com.logginghub.utils.net;

public interface ObjectInputStreamReaderListener
{
    void onObjectRead(Object object);
    void onStreamClosed();
}
