package com.logginghub.messaging.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.logginghub.utils.logging.Logger;

public class IntegerHeaderFrameEncoder extends SimpleChannelHandler {
    private static final Logger logger = Logger.getLoggerFor(IntegerHeaderFrameEncoder.class);
    
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) {        
        ChannelBuffer buffer = (ChannelBuffer) e.getMessage();

        int length = buffer.readableBytes();
        int fullLength = 4 + length;
        
        ChannelBuffer buf = ChannelBuffers.buffer(fullLength);
        
        buf.writeInt(length);
        buf.writeBytes(buffer);
        
        Channels.write(ctx, e.getFuture(), buf);
        logger.trace("Encoding integer frame header for buffer {} - payload length is {}, full message length is {}", buffer, length, fullLength);
    }
}