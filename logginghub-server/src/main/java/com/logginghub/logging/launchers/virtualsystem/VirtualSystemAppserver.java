package com.logginghub.logging.launchers.virtualsystem;

import com.logginghub.logging.internallogging.LoggingHubStream;
import com.logginghub.logging.repository.SofString;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofSerialiser;
import com.logginghub.utils.sof.StreamReaderAbstraction;
import com.logginghub.utils.sof.StreamWriterAbstraction;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * Created by james on 29/01/15.
 */
public class VirtualSystemAppserver {
    public static void main(String[] args) {
        final int port = Integer.parseInt(args[5]);

        WorkerThread.every("Heartbeat", "5 seconds", new Runnable() {
            @Override public void run() {
                try {
                    Socket socket = new Socket("localhost", port);
                    socket.close();
                } catch (IOException e) {
                    System.exit(1);
                }
            }
        });

        LoggingHubStream stream = new LoggingHubStream();
        stream.setSourceApplication(args[0]);
        stream.setHostOverride(args[1]);
        stream.setHostAddressOverride(args[2]);
        stream.setEnvironment(args[3]);
        stream.setInstanceNumber(Integer.parseInt(args[4]));
        stream.getAppenderHelper().setStackTraceModuleEnabled(true);
        stream.setJava7GCLogging(true);
        stream.setHost("localhost:15000");
        stream.start();

        Logger.root().addStream(stream);

        run();
    }

    private static void run() {

        final SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(SofString.class, 1);

        final Random random = new Random();

        final Logger logger = Logger.getLoggerFor("Appserver");

        WorkerThread.executeDaemonOngoing("Worker", new Runnable() {
            @Override public void run() {

                int portStart = 16000;
                int portRange = 10;

                boolean connected = false;

                InputStream inputStream = null;
                OutputStream outputStream = null;

                Socket socket = null;
                while (!connected) {

                    int portToTry = portStart + random.nextInt(portRange);

                    try {
                        logger.info("Trying to connect to port {}", portToTry);
                        socket = new Socket("localhost", portToTry);
                        logger.info("Connected to port {}", portToTry);

                        inputStream = socket.getInputStream();
                        outputStream = socket.getOutputStream();

                        final StreamWriterAbstraction writer = new StreamWriterAbstraction(new BufferedOutputStream(outputStream));
                        final StreamReaderAbstraction reader = new StreamReaderAbstraction(new BufferedInputStream(inputStream), Long.MAX_VALUE);

                        int messages = 1000;
                        for (int i = 0; i < messages; i++) {
                            String input = "This is a random string : " + StringUtils.randomString(100);

                            Stopwatch start = Stopwatch.start("String reverse (client)");

                            SofSerialiser.write(writer, new SofString(input), configuration);
                            writer.flush();
                            SofString read = SofSerialiser.read(reader, configuration);

                            logger.info(start);

                            ThreadUtils.sleep(200 + random.nextInt(200));
                        }

                        connected = true;
                    } catch (IOException e) {
                    } catch (SofException e) {
                        e.printStackTrace();
                    } finally {
                        FileUtils.closeQuietly(inputStream);
                        FileUtils.closeQuietly(outputStream);
                        FileUtils.closeQuietly(socket);
                    }
                }

            }
        });
    }
}
