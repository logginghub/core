package com.logginghub.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import com.logginghub.utils.WorkerThread;

public abstract class GCFileWatcher {

    private WorkerThread thread;

    public void start(String logFilePath) throws FileNotFoundException {
        final FileInputStream fis = new FileInputStream(new File(logFilePath));
        final BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        thread = new WorkerThread("GC Watcher Thread") {
            @Override protected void onRun() throws Throwable {
                try {
                    String readLine = br.readLine();
                    if (readLine != null) {
                        log("GC logging : " + readLine);
                    }

                    Thread.sleep(100);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                catch (InterruptedException e) {
//                    e.printStackTrace();
                }
            }
        };

        thread.start();

    }

    protected abstract void log(String gcLine);

    public void stop() {
        thread.stop();
    }
}
