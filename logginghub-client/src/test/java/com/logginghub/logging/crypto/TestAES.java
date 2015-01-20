package com.logginghub.logging.crypto;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.crypto.AES;
import com.logginghub.logging.messages.PartialMessageException;
import com.logginghub.logging.messaging.LogEventCodex;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.ExpandingByteBuffer;
import com.logginghub.logging.LogEventComparer;

@RunWith(CustomRunner.class) public class TestAES {
    @Test public void testAESOnString() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
                    BadPaddingException {
        AES aes = new AES(AES.generateKey());

        String testString = "I'm a test string";

        byte[] encrypt = aes.encrypt(testString.getBytes());
        byte[] decrypted = aes.decrypt(encrypt);

        String rebuilt = new String(decrypted);
        assertEquals(testString, rebuilt);
    }

    @Test public void testAESWithDaftKeyOnString() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
                    IllegalBlockSizeException, BadPaddingException {
        AES aes = new AES("foo moo I'm a ke".getBytes());

        String testString = "I'm a test string";

        byte[] encrypt = aes.encrypt(testString.getBytes());
        byte[] decrypted = aes.decrypt(encrypt);

        String rebuilt = new String(decrypted);
        assertEquals(testString, rebuilt);
    }

    @Test public void testAESOnLogEvent() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
                    BadPaddingException, PartialMessageException {
        AES aes = new AES(AES.generateKey());

        LogEvent event = LogEventFactory.createFullLogEvent1("TestApp");

        ExpandingByteBuffer buffer = new ExpandingByteBuffer();
        LogEventCodex.encode(buffer, event);
        buffer.flip();

        byte[] contents = buffer.getContents();

        byte[] encrypt = aes.encrypt(contents);
        byte[] decrypted = aes.decrypt(encrypt);

//        System.out.println("Encoded length = " + contents.length);
//        System.out.println("Encrypted length = " + encrypt.length);

//        HexDump.dump(contents);
//        HexDump.dump(encrypt);

        ByteBuffer wrap = ByteBuffer.wrap(decrypted);
        LogEvent decode = LogEventCodex.decode(wrap);

        LogEventComparer.assertEquals(event, decode);
    }
}
