package com.logginghub.logging.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Manages a single instance of AES encryption.
 * 
 * @author admin
 */
public class AES {
    private Cipher m_encryptCipher;
    private Cipher m_decryptCipher;

    public AES(byte[] key) {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

        try {
            m_encryptCipher = Cipher.getInstance("AES");
            m_encryptCipher.init(Cipher.ENCRYPT_MODE, skeySpec);

            m_decryptCipher = Cipher.getInstance("AES");
            m_decryptCipher.init(Cipher.DECRYPT_MODE, skeySpec);
        }
        catch (InvalidKeyException e) {
            throw new RuntimeException("Failed to construct AES crypto, the key was invalid", e);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to construct AES crypto, the JVM couldn't find the AES algorithm", e);
        }
        catch (NoSuchPaddingException e) {
            throw new RuntimeException("Failed to construct AES crypto, padding scheme was unavailable", e);
        }
    }

    public byte[] encrypt(byte[] data) {
        try {
            byte[] encrypted = m_encryptCipher.doFinal(data);
            return encrypted;
        }
        catch (IllegalBlockSizeException e) {
            throw new RuntimeException("Failed to encrypt using AES crypto, the block size was illegal", e);
        }
        catch (BadPaddingException e) {
            throw new RuntimeException("Failed to encrypt using AES crypto, the data was not padded correctly", e);
        }
    }

    public byte[] decrypt(byte[] data) {
        try {
            byte[] decrypted = m_decryptCipher.doFinal(data);
            return decrypted;
        }
        catch (IllegalBlockSizeException e) {
            throw new RuntimeException("Failed to encrypt using AES crypto, the block size was illegal", e);
        }
        catch (BadPaddingException e) {
            throw new RuntimeException("Failed to encrypt using AES crypto, the data was not padded correctly", e);
        }
    }

    public static byte[] generateKey() throws NoSuchAlgorithmException {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128); // 192 and 256 bits may not be available

        // Generate the secret key specs.
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();

        return raw;
    }
}