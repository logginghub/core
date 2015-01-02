package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import org.junit.Test;

import com.logginghub.utils.DesEncrpytion;
import com.logginghub.utils.FileUtils;

public class TestDesEncrpytion {

    @Test public void testPlain() throws IOException {
        String input = "I'm a string to encrypt";
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream encrypted = new ByteArrayOutputStream();

        FileUtils.copy(bais, encrypted);

        assertThat(new String(encrypted.toByteArray()), is(input));
    }

    @Test public void testEncrypted() throws IOException {
        String input = "I'm a string to encrypt";
        
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream encrypted = new ByteArrayOutputStream();
        
        DesEncrpytion encrpytion = new DesEncrpytion("passPhrase");
        
        CipherOutputStream cos = new CipherOutputStream(encrypted, encrpytion.getECipher());
        FileUtils.copy(bais, cos);
        bais.close();
        cos.close();

        assertThat(new String(encrypted.toByteArray()), is(not(input)));
        
        DesEncrpytion encryption2 = new DesEncrpytion("passPhrase");
        ByteArrayInputStream encryptedData = new ByteArrayInputStream(encrypted.toByteArray());
        CipherInputStream cis = new CipherInputStream(encryptedData, encryption2.getDCipher());
        ByteArrayOutputStream decrypted = new ByteArrayOutputStream();
        
        FileUtils.copy(cis, decrypted);
        cis.close();
        decrypted.close();
        
        assertThat(new String(encrypted.toByteArray()), is(not(input)));
        assertThat(new String(decrypted.toByteArray()), is(input));
        
        
    }
}
