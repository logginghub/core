package com.logginghub.utils.net;

public interface InputStreamReaderListener
{
    void onBytesRead(byte[] buffer, int offset, int length);
}
