package com.logginghub.utils.uploader;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.text.NumberFormat;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.logginghub.utils.Base64;
import com.logginghub.utils.EnvironmentProperties;
import com.logginghub.utils.logging.Logger;

public class Uploader {

    private static final Logger logger = Logger.getLoggerFor(Uploader.class);
    private static String urlString = "https://" + EnvironmentProperties.getString("uploaderConnectionPoint", "www.vertexlabs.co.uk:8113") + "/upload/";

    public static void main(String[] args) throws IOException {
        logger.info("Uploading {}", Arrays.toString(args));

        disableSSLChecking();

        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
//                System.out.println("Warning: URL Host: " + urlHostName + " vs. " + session.getPeerHost());
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        for (String filename : args) {
            sendFile(filename);
        }
    }

    private static void sendFile(String filename) throws IOException {

        File file = new File(filename);
        logger.info("Sending file '{}' to '{}'", filename, urlString + file.getName());
        FileInputStream fis = new FileInputStream(file);

        long count = 0;
        ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
        int b;

        boolean first = true;

        long length = file.length();
        
        
        while ((b = fis.read()) != -1) {
            buffer.put((byte) b);

            if (!buffer.hasRemaining()) {
                logger.info("Sending batch of {} bytes - {} %", buffer.position(), NumberFormat.getInstance().format(100d * count/(double)length));
                sendBatch(first, file.getName(), buffer);
                first = false;
            }

            count++;
        }

        if (buffer.position() > 0) {
            logger.info("Sending final batch of {} bytes", buffer.position());
            sendBatch(first, file.getName(), buffer);
            first = false;
        }

        fis.close();
    }

    private static void sendBatch(boolean first, String filename, ByteBuffer buffer) throws IOException {

        byte[] data = new byte[buffer.position()];
        buffer.flip();
        buffer.get(data);

        String encoded = Base64.encodeBytes(data);
        encoded = URLEncoder.encode(encoded, "UTF8");
        logger.debug("Buffer is {} - position was {} and encoded length is {}", buffer, buffer.position(), encoded.length());

        buffer.clear();

        send(urlString + filename, "first=" + first + "&data=" + encoded);
    }

    private static void send(String urlString, String urlParameters) {
        URL url;
        HttpURLConnection connection;
        BufferedReader rd;
        String line;
        String result = "";
        try {
            url = new URL(urlString);
            
            /*
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("my.proxy.example.com", 3128));
            
            final String authUser = "user";
            final String authPassword = "password";
            Authenticator.setDefault(
               new Authenticator() {
                  public PasswordAuthentication getPasswordAuthentication() {
                     return new PasswordAuthentication(
                           authUser, authPassword.toCharArray());
                  }
               }
            );

            System.setProperty("http.proxyUser", authUser);
            System.setProperty("http.proxyPassword", authPassword);
            connection = (HttpURLConnection) url.openConnection(proxy);
            */

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
            connection.setUseCaches(false);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result += line;
            }
            rd.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println(result);
    }

    private static void disableSSLChecking() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }
        catch (GeneralSecurityException e) {}
    }

}
