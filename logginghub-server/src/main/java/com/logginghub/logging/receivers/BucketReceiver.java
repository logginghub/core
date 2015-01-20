package com.logginghub.logging.receivers;

import com.logginghub.logging.messaging.SocketReceiverThread;
import com.logginghub.logging.utils.LogEventBucket;

public class BucketReceiver extends SocketReceiverThread
{
    private LogEventBucket m_bucket;

    public BucketReceiver()
    {
        m_bucket = new LogEventBucket();
        addLogEventListener(m_bucket);
    }
    
    public LogEventBucket getBucket()
    {
        return m_bucket;
    }
}
