package com.logginghub.messaging.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.logginghub.messaging2.encoding.ChannelBufferEncodeHelper;
import com.logginghub.messaging2.encoding.reflection.ReflectionSerialiser;
import com.logginghub.utils.logging.Logger;

public class ObjectEncoder extends SimpleChannelHandler {

    private ReflectionSerialiser reflectionSerialiser = new ReflectionSerialiser();
    private static final Logger logger = Logger.getLoggerFor(ObjectDecoder.class);
    
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) {        
        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
        try {
            Object object = e.getMessage();
            logger.trace("Encoding object {} to buffer {}...", object, buf);
            reflectionSerialiser.encodeWithClass(object, new ChannelBufferEncodeHelper(buf));
            
            logger.trace("Encoding successfull, buffer is now {}. Writing...", buf);
            Channels.write(ctx, e.getFuture(), buf);
            logger.trace("Writing complete");
        }
        catch (IllegalArgumentException e1) {
            logger.warning(e1);
            e1.printStackTrace();
        }
        catch (IllegalAccessException e1) {
            logger.warning(e1);
            e1.printStackTrace();
        }
    }
}