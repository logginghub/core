package com.logginghub.logging.launchers.virtualsystem;

import com.logginghub.utils.Out;
import com.logginghub.utils.ProcessUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.WorkerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by james on 29/01/15.
 */
public class RunVirtualSystem {
    public static void main(String[] args) throws IOException {

        String environment = "LoggingHubVirtual";

        int appServers = 4;
        int cacheServers = 2;
        int machines = 2;

        int port = startHeartbeatServer();

        for(int i = 0; i < machines; i++) {

            String machine = "lhvirtldn" + (i+1);
            String address = "123.123.123." + (i+1);

            for(int j = 0; j < appServers; j++) {
                startChild(VirtualSystemAppserver.class.getName(), environment, machine, address, "Appserver", j, port);
            }

            for(int j = 0; j < cacheServers; j++) {
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
            @Override public void run() {
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
        StringBuilder command = new StringBuilder();

        command.append("java -cp ");
        command.append(System.getProperty("java.class.path"));
        command.append(" ");
        command.append(" -DvirtualSystem=true");
        command.append(" ");
        command.append(" -Xmx64m");
        command.append(" ");
        command.append(classname);
        command.append(" ");
        command.append(type).append("-" + (instanceNumber + 1));
        command.append(" ");
        command.append(machine);
        command.append(" ");
        command.append(address);
        command.append(" ");
        command.append(environment);
        command.append(" ");
        command.append(instanceNumber + 1);
        command.append(" ");
        command.append(port);

        Out.out(command);
        Process exec = Runtime.getRuntime().exec(command.toString());
        ProcessUtils.startDaemonOutputThreads(exec);
    }
}
