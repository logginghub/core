package com.logginghub.logging.launchers.virtualsystem;

import com.logginghub.utils.WorkerThread;

import java.io.IOException;

/**
 * Created by james on 05/02/15.
 */
public class DebuggingVirtualSystem {
    public static void main(String[] args) throws IOException {

        final int port = RunVirtualSystem.startHeartbeatServer();

        WorkerThread.execute("Cacheserver", new Runnable() {
            @Override public void run() {
                VirtualSystemCacheserver.main(new String[]{"Cachserver-1", "machine1", "123.123.123.1", "env", "1", "" + port});
            }
        });

//        WorkerThread.execute("Appserver", new Runnable() {
//            @Override public void run() {
//                VirtualSystemAppserver.main(new String[]{"Appserver-1", "machine1", "123.123.123.1", "env", "1", "" + port});
//            }
//        });


    }
}
