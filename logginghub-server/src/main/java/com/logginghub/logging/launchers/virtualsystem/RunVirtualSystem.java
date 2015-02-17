package com.logginghub.logging.launchers.virtualsystem;

import com.logginghub.utils.Out;
import com.logginghub.utils.ProcessUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.WorkerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by james on 29/01/15.
 */
public class RunVirtualSystem {
    public static void main(String[] args) throws IOException {

        String environment = "LoggingHubVirtual";

        int appServers = 2;
        int cacheServers = 2;
        int machines = 2;

        int port = startHeartbeatServer();

        for (int i = 0; i < machines; i++) {

            String machine = "lhvirtldn" + (i + 1);
            String address = "123.123.123." + (i + 1);

            for (int j = 0; j < appServers; j++) {
                startChild(VirtualSystemAppserver.class.getName(), environment, machine, address, "Appserver", j, port);
            }

            for (int j = 0; j < cacheServers; j++) {
                startChild(VirtualSystemCacheserver.class.getName(), environment, machine, address, "CacheServer", j, port);
            }
        }

        ThreadUtils.sleep("10 hours");


    }

    public static int startHeartbeatServer() throws IOException {
        final ServerSocket socket = new ServerSocket();
        socket.bind(null);
        int port = socket.getLocalPort();
        WorkerThread.executeDaemonOngoing("Acceptor", new Runnable() {
            @Override
            public void run() {
                try {
                    Socket accept = socket.accept();
                    accept.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return port;
    }

    private static void startChild(String classname, String environment, String machine, String address, String type, int instanceNumber, int port) throws IOException {
        //StringBuilder command = new StringBuilder();

        String java = "/Library/Java/JavaVirtualMachines/jdk1.8.0_31.jdk/Contents/Home/bin/java";

        String[] command = new String[]{
                java, "-cp", System.getProperty("java.class.path"), "-DvirtualSystem=true", "-Xmx64m", classname,
                type + "-" + (instanceNumber + 1), machine, address, environment, "" + (instanceNumber + 1), "" + port
        };

        Out.out(Arrays.toString(command));
        Process exec = Runtime.getRuntime().exec(command);
        ProcessUtils.startDaemonOutputThreads(exec);
    }
}
