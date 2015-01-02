package com.logginghub.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class DesEncrpytion {
    Cipher ecipher;
    Cipher dcipher;

    // 8-byte Salt
    byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03 };

    // Iteration count
    int iterationCount = 19;

    public static void main(String[] args) throws Exception {
        if(args.length != 3){
            System.out.println("DesEncrpytion <inputfile> <outputfile> <passphrase>");
            return;
        }
        
        String inputFile = args[0];
        String outputFile = args[1];
        String passphrase = args[2];
        
        DesEncrpytion encrypter = new DesEncrpytion(passphrase);        
        encrypter.decrypt(inputFile, outputFile);
    }
    

    private void decrypt(String inputFile, String outputFile) throws Exception {
        CipherInputStream cis = new CipherInputStream(new BufferedInputStream(new FileInputStream(new File(inputFile))), dcipher);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(outputFile)));
        
        FileUtils.copy(cis, bos);
        
        cis.close();
        bos.close();
    }


    public DesEncrpytion(String passPhrase) {
        try {
            // Create the key
            KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
            SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            ecipher = Cipher.getInstance(key.getAlgorithm());
            dcipher = Cipher.getInstance(key.getAlgorithm());

            // Prepare the parameter to the ciphers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

            // Create the ciphers
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        }
        catch (java.security.InvalidAlgorithmParameterException e) {}
        catch (java.security.spec.InvalidKeySpecException e) {}
        catch (javax.crypto.NoSuchPaddingException e) {}
        catch (java.security.NoSuchAlgorithmException e) {}
        catch (java.security.InvalidKeyException e) {}
    }


    public Cipher getECipher() {
        return ecipher;
    }
    
    public Cipher getDCipher() {
        return dcipher;
         
    }
}