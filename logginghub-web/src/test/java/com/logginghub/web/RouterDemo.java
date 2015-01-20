package com.logginghub.web;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class RouterDemo {

    public static void main(String[] args) throws MalformedURLException, IOException {

        readUrl("http://192.168.2.1/Timelogout.cgi?usrUserName=fail");
        
        String readUrl = readUrl("http://192.168.2.1/fw_virt.html");
        System.out.println(readUrl);

        String urlParameters = "password=MyPassword?page=login?dummy=";
        String request = "http://192.168.2.1/processlogin.cgi";
        URL url = new URL(request);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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

        System.out.println("Reading input..");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String decodedString;
        while ((decodedString = in.readLine()) != null) {
            System.out.println(decodedString);
        }
        in.close();

        connection.disconnect();

        System.out.println("Restarting...");
        readUrl("http://192.168.2.1/reset_success.cgi");        
    }
    
    public static String readUrl(String string) {
        try {
            return read(new URL(string).openStream());
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Failed to read url %s", string), e);
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to read url %s", string), e);
        }
    }

    public static String read(InputStream openStream) {
        return new String(readFully(openStream));
    }

    public static byte[] readFully(InputStream inputStream) {

        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[100 * 1024];
            int read = 0;
            while ((read = inputStream.read(buffer)) > 0) {
                bais.write(buffer, 0, read);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Faield to read input stream"), e);
        }

        return bais.toByteArray();
    }
}
