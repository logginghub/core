package com.logginghub.logging.messaging;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logginghub.utils.ExpandingByteBuffer;

public abstract class AbstractCodex {
    protected final static int nullValue = -1;
    private static Logger logger = Logger.getLogger(AbstractCodex.class.getName());

    /**
     * If we get asked to decode anything bigger than this then assume something
     * has gone wrong...
     */
    private static final int stringSizeCutoff = Integer.getInteger("abstractCodex.stringSizeCutoff", 100 * 1024 * 1024);

    protected static void encodeInetAddress(ExpandingByteBuffer buffer, InetAddress sourceHost) {
        encodeString(buffer, sourceHost.getHostName());
        encodeString(buffer, sourceHost.getHostAddress());
    }

    protected static InetAddress decodeInetAddress(ByteBuffer buffer) {
        String hostname = decodeString(buffer);
        String address = decodeString(buffer);

        // TODO : jshaw - this smells of bugs, because we can't just construct
        // the address
        // based on the ip and hostname sent to us, we'll probably end up
        // doing a DNS lookup on the receiving end because of this
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(hostname);
        }
        catch (UnknownHostException e) {
            try {
                byte[] bytes = toBytes(address);
                inetAddress = InetAddress.getByAddress(hostname, bytes);
            }
            catch (UnknownHostException e1) {
                // jshaw - I'm pretty sure this can't happen
                throw new RuntimeException("Failed to resolve host for hostname " + hostname);
            }
        }

        return inetAddress;
    }

    protected static byte[] toBytes(String address) {
        String[] split = address.split("\\.");
        byte[] ip = new byte[split.length];
        for (int i = 0; i < ip.length; i++) {
            ip[i] = (byte) Integer.parseInt(split[i]);
        }
        return ip;
    }

    protected static void encodeStringArray(ExpandingByteBuffer buffer, String[] formattedObject) {
        if (formattedObject == null) {
            buffer.putInt(nullValue);
        }
        else {
            int count = formattedObject.length;
            buffer.putInt(count);
            for (String string : formattedObject) {
                encodeString(buffer, string);
            }
        }
    }

    protected static String[] decodeStringArray(ByteBuffer buffer) {
        String[] array;

        int count = buffer.getInt();
        if (count == nullValue) {
            array = null;
        }
        else {
            array = new String[count];

            for (int i = 0; i < count; i++) {
                array[i] = decodeString(buffer);
            }
        }

        return array;
    }

    protected static void encodeString(ExpandingByteBuffer buffer, String string) {
        if (string == null) {
            buffer.putInt(nullValue);
        }
        else {
            // Remember not to use the length of the string - different encoding
            // schemes will use variable-length chars!
            byte[] bytes = string.getBytes();
            buffer.putInt(bytes.length);
            buffer.put(bytes);
        }
    }

    protected static String decodeString(ByteBuffer buffer) {
        int length = buffer.getInt();

        String string;

        if (length == nullValue) {
            string = null;
        }
        else {
            if (length < 0 || length > stringSizeCutoff) {
                throw new RuntimeException(String.format("Illegal string length decoded : %d", length));
            }

            if (logger.isLoggable(Level.FINER)) {
                logger.finer(String.format("Decoding string with length '%d'", length));
            }

            // System.out.println("Decoding string length " + length +
            // " from buffer " + buffer);
            byte[] message = new byte[length];
            buffer.get(message);
            string = new String(message);

            if (logger.isLoggable(Level.FINER)) {
                logger.finer(String.format("String decoded '%s'", string));
            }
        }

        return string;
    }

}
