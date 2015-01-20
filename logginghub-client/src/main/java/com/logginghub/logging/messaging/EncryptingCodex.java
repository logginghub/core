package com.logginghub.logging.messaging;

import java.nio.ByteBuffer;

import com.logginghub.logging.crypto.AES;
import com.logginghub.utils.ExpandingByteBuffer;

public class EncryptingCodex
{
    private AES m_aes;

    public EncryptingCodex()
    {
        m_aes = new AES("foo moo I'm a ke".getBytes());
    }

    public ByteBuffer decrypt(ByteBuffer buffer)
    {
        int size = buffer.getInt();
        
        byte[] contents = new byte[size];
        buffer.get(contents);
                
        byte[] decrypted = m_aes.decrypt(contents);
        return ByteBuffer.wrap(decrypted);
    }

    /**
     * Encrypt the bytes from start position to the current position of buffer,
     * replacing the contents inside the buffer.
     * 
     * @param expandingBuffer
     * @param position
     */
    public void encrypt(ExpandingByteBuffer expandingBuffer, int startPosition)
    {        
        byte[] contents = expandingBuffer.getContents(startPosition, expandingBuffer.position());
        
        byte[] encrypt = m_aes.encrypt(contents);
        int size = encrypt.length;

        expandingBuffer.position(startPosition);
        expandingBuffer.putInt(size);
        expandingBuffer.put(encrypt);
    }

}
