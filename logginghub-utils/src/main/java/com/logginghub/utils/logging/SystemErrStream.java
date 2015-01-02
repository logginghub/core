package com.logginghub.utils.logging;

public class SystemErrStream implements LoggerStream {
    public static int gapThreshold = 250;
    private long lastLogTime = 0;
    private LogEventFormatter formatter = new SingleLineStreamFormatter();
    public void onNewLogEvent(LogEvent event) {
        long now = System.currentTimeMillis();
        if(now - lastLogTime > gapThreshold) {
            System.err.println();
        }
        System.err.print(formatter.format(event));
        System.err.flush();
        lastLogTime = now;
    }
}
