package com.logginghub.analytics.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.logginghub.analytics.model.TimeSeriesData;

public class PingCollector {

    public static void main(String[] args) throws UnknownHostException, IOException {

        PingCollector.ping("hosting");

    }

    public static void ping(final String host) {

        final TimeSeriesData data = new TimeSeriesData();

        File file = new File("demo/" + host + ".2.data");
        
        data.setKeysLegend(new String [] { "ip" });
        data.setValuesLegend(new String [] { "time", "ttl" });
        
        //String regex = "(\\d*) bytes from (.+?) (.*): icmp_seq=(\\d*) ttl=(\\d*) time=(\\d*) ms";
        String regex = "(\\d*) bytes from (.+?) (.*): icmp_seq=(\\d*) ttl=(\\d*) time=([\\.\\d]*) ms";
        final Pattern pattern = Pattern.compile(regex);
        
        Executors.newFixedThreadPool(1).execute(new Runnable() {

            public void run() {
                try {
                    Process proc = new ProcessBuilder("ping", host).start();

                    InputStream inputStream = proc.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                       
                        Matcher matcher = pattern.matcher(line);
                        if(matcher.matches()){
                            
                            String bytes = matcher.group(1);
                            String host = matcher.group(2);
                            String ip = matcher.group(3);
                            Integer seq = Integer.parseInt(matcher.group(4));
                            Integer ttl = Integer.parseInt(matcher.group(5));
                            Double time = Double.parseDouble(matcher.group(6));
                            
                            System.out.println(String.format("%s : %d : %d", ip, time, ttl));
                            
                            data.add(System.currentTimeMillis(), new String[] { ip }, new double[] { time, ttl});
                        }
                    }

                    proc.waitFor();
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}
