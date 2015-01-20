package com.logginghub.logging.messaging;

import java.nio.ByteBuffer;

import com.logginghub.logging.LogEvent;

public class LogEventCompressionCodex
{
    private LogEventCodex m_decorateCodex;

    public LogEventCompressionCodex(LogEventCodex decoratedCodex)
    {
        m_decorateCodex = decoratedCodex;
    }
    
    public static void encode(ByteBuffer buffer, LogEvent event)
    {
        
    }
}
