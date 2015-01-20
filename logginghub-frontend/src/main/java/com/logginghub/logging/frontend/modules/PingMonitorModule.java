package com.logginghub.logging.frontend.modules;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Container2.Hint;

public class PingMonitorModule implements Asynchronous {
    
    private static final Logger logger = Logger.getLoggerFor(PingMonitorModule.class); 
    
    private String host;
    private String name;

    private JLabel label;
    private String layout;
    private Process process;
    private WorkerThread thread;

    public PingMonitorModule(LayoutService layoutService, @Hint(attribute="layout") String layout) {
        label = new JLabel("", JLabel.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.orange);
        layoutService.add(label, layout);
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                label.setText(StringUtils.format("{} {} {} ms", host, "?", "?"));
            }
        });
        
        this.name = name;
    }

    @Override public void start() {
        stop();

        String regex = "(\\d*) bytes from ([^ (]*)[ ]*(.*): icmp_seq=(\\d*) ttl=(\\d*) time=([\\d\\.]*) ms";
        final Pattern pattern = Pattern.compile(regex);
        
        thread = WorkerThread.execute("Ping-" + name, new Runnable() {
            public void run() {
                try {
                    logger.fine("Pinging '{}'", host);
                    process = new ProcessBuilder("ping", host).start();

                    InputStream inputStream = process.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;
                    
                    logger.fine("Stream open, waiting for output...");
                    
                    while ((line = br.readLine()) != null) {
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.matches()) {

                            String bytes = matcher.group(1);
                            final String host = matcher.group(2);
                            final String ip = matcher.group(3);
                            Integer seq = Integer.parseInt(matcher.group(4));
                            Integer ttl = Integer.parseInt(matcher.group(5));
                            final Double time = Double.parseDouble(matcher.group(6));

                            logger.finer("Match found: {} {} {}", host, ip, time);
                            
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override public void run() {
                                    label.setText(StringUtils.format("{} ({}) {} ms", name, ip, time));
                                    label.setBackground(Color.green);
                                }
                            });
                        }else{
                            logger.warn("Failed to match line '{}'", line);
                        }
                    }
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override public void stop() {
        if (thread != null) {
            thread.dontRunAgain();
        }

        if (process != null) {
            process.destroy();
            try {
                process.waitFor();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            process = null;
        }

        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }

}
