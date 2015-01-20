package com.logginghub.messaging.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import com.logginghub.messaging2.encoding.ChannelBufferEncodeHelper;
import com.logginghub.messaging2.encoding.reflection.ReflectionSerialiser;
import com.logginghub.utils.logging.Logger;

public class ObjectDecoder extends FrameDecoder {

    private ReflectionSerialiser reflectionSerialiser = new ReflectionSerialiser();
    private static final Logger logger = Logger.getLoggerFor(ObjectDecoder.class);

    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) {
        logger.trace("Decoding object from channel buffer {}", buffer);
        Object object;
        try {
            object = reflectionSerialiser.decode(new ChannelBufferEncodeHelper(buffer));
            logger.trace("Object decoded {}", object);
        }
        catch (IllegalArgumentException e) {
            logger.warning(e);
            throw new RuntimeException(e);
        }
        catch (ClassNotFoundException e) {
            logger.warning(e);
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            logger.warning(e);
            throw new RuntimeException(e);
        }

        return object;
    }
}
