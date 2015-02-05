package com.logginghub.logging.launchers.virtualsystem;

import com.logginghub.logging.internallogging.LoggingHubStream;
import com.logginghub.logging.repository.SofString;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofSerialiser;
import com.logginghub.utils.sof.StreamReaderAbstraction;
import com.logginghub.utils.sof.StreamWriterAbstraction;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by james on 29/01/15.
 */
public class VirtualSystemCacheserver {
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
        stream.setInstanceIdentifier(args[4]);
        stream.getAppenderHelper().setStackTraceModuleEnabled(true);
        stream.setHost("localhost:15000");
        stream.setJava7GCLogging(true);
        stream.setReportsModuleEnabled(true);
        stream.setReportsModuleConfiguration("src/main/resources/reports-cacheserver.json");
        stream.start();

        Logger.root().addStream(stream);

        run();
    }

    private static void run() {

        int portStart = 16000;

        boolean bound = false;

        final Logger logger = Logger.getLoggerFor("Cacheserver");

        ServerSocket socket = null;
        while (!bound) {
            try {
                socket = new ServerSocket(portStart);
                bound = true;
            } catch (IOException e) {
                portStart++;
            }
        }

        logger.info("Bound to port {}", portStart);

        final SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(SofString.class, 1);

        final ServerSocket finalSocket = socket;
        WorkerThread.executeDaemonOngoing("Acceptor", new Runnable() {
            @Override public void run() {

                try {
                    final Socket accept = finalSocket.accept();

                    logger.info("Accepted '{}'", accept);
                    try {
                        final InputStream inputStream = new BufferedInputStream(accept.getInputStream());
                        final OutputStream outputStream = new BufferedOutputStream(accept.getOutputStream());

                        final StreamReaderAbstraction reader = new StreamReaderAbstraction(inputStream, Long.MAX_VALUE);
                        final StreamWriterAbstraction writer = new StreamWriterAbstraction(outputStream);

                        WorkerThread.executeDaemonOngoing("Handler-" + accept, new Runnable() {
                            @Override public void run() {

                                try {
                                    SofString read = SofSerialiser.read(reader, configuration);

                                    Stopwatch start = Stopwatch.start("String reverse (server)");

                                    String value = read.getValue();
                                    String reverse = value;

                                    for (int i = 0; i < 5000; i++) {
                                        reverse = StringUtils.reverse(reverse);
                                    }

                                    SofSerialiser.write(writer, new SofString(reverse), configuration);
                                    writer.flush();

                                    logger.info(start.stopAndFormat());

                                } catch (Exception e) {

                                    FileUtils.closeQuietly(inputStream);
                                    FileUtils.closeQuietly(outputStream);

                                    throw new WorkerThread.StopRunningException();
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }
}
