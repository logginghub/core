package com.logginghub.messaging.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import com.logginghub.utils.logging.Logger;

public class IntegerHeaderFrameDecoder extends FrameDecoder {

    private static final Logger logger = Logger.getLoggerFor(IntegerHeaderFrameDecoder.class);

    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buf) throws Exception {

        logger.trace("Decoding integer frame header from buffer {}...", buf);
        
        // Make sure if the length field was received.
        if (buf.readableBytes() < 4) {
            // The length field was not received yet - return null.
            // This method will be invoked again when more packets are
            // received and appended to the buffer.
            logger.trace("... there weren't enough bytes in the buffer to read the size header");
            return null;
        }

        // The length field is in the buffer.

        // Mark the current buffer position before reading the length field
        // because the whole frame might not be in the buffer yet.
        // We will reset the buffer position to the marked position if
        // there's not enough bytes in the buffer.
        buf.markReaderIndex();

        // Read the length field.
        int length = buf.readInt();

        // Make sure if there's enough bytes in the buffer.
        if (buf.readableBytes() < length) {
            // The whole bytes were not received yet - return null.
            // This method will be invoked again when more packets are
            // received and appended to the buffer.

            // Reset to the marked position to read the length field again
            // next time.
            buf.resetReaderIndex();

            logger.trace("... there wasn't enough data available to fully read the payload of {} bytes - only had {} bytes", length, buf.readableBytes());
            return null;
        }

        // There's enough bytes in the buffer. Read it.
        ChannelBuffer frame = buf.readBytes(length);
        logger.trace("... successfully read {} bytes of payload", length);

        // Successfully decoded a frame. Return the decoded frame.
        return frame;
    }

}
