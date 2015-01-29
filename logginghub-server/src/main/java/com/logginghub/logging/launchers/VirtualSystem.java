package com.logginghub.logging.launchers;

import com.logginghub.logging.internallogging.LoggingHubStream;
import com.logginghub.utils.WorkerThread;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by james on 29/01/15.
 */
public class VirtualSystem {
    public static void main(String[] args) {
        final int port = Integer.parseInt(args[4]);

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
        stream.setEnvironment(args[2]);
        stream.setInstanceNumber(Integer.parseInt(args[3]));
        stream.getAppenderHelper().setStackTraceModuleEnabled(true);
        stream.setHost("localhost:15000");
        stream.start();
    }
}
