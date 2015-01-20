package com.logginghub.swingutils.stack;

import java.lang.Thread.State;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.logginghub.swingutils.MigPanel;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.swing.TestFrame;

public class StackAnalyser extends MigPanel {

    private static final Logger logger = Logger.getLoggerFor(StackAnalyser.class);

    // private FactoryMap<String, CircularArray<Snapshot>> data = new FactoryMap<String,
    // CircularArray<Snapshot>>() {
    // @Override protected CircularArray<Snapshot> createEmptyValue(String key) {
    // return new CircularArray<Snapshot>(Snapshot.class, 1024);
    // }
    // };

    private FactoryMap<String, ThreadData> data = new FactoryMap<String, ThreadData>() {
        @Override protected ThreadData createEmptyValue(String key) {
            return new ThreadData(key);
        }
    };

    private long interval;

    public StackAnalyser(String a, String b, String c) {
        super(a, b, c);
    }

    public StackAnalyser() {
        super("", "", "");
    }

    public void start(long time, TimeUnit units) {
        this.interval = units.toMillis(time);
        TimerUtils.every("StackCollector", time, units, new Runnable() {
            public void run() {
                capture();
            }
        });

        TimerUtils.everySecond("StatsProcessor", new Runnable() {
            public void run() {
                process();
            }
        });
    }

    protected void process() {
        Set<String> keySet = data.keySet();
        for (String string : keySet) {
            ThreadData threadData = data.get(string);
            threadData.dump();
        }
    }

    public void capture() {
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        Set<Entry<Thread, StackTraceElement[]>> entrySet = allStackTraces.entrySet();
        for (Entry<Thread, StackTraceElement[]> entry : entrySet) {
            process(entry.getKey(), entry.getValue());
        }
    }

    private void process(Thread key, StackTraceElement[] value) {

        String uniqueID = key.getName() + "::" + key.getId();

        boolean alive = key.isAlive();
        State state = key.getState();

        Snapshot snapshot = new Snapshot();
        snapshot.setTime(System.currentTimeMillis());
        snapshot.setInterval(interval);
        snapshot.setUniqueID(uniqueID);
        snapshot.setState(state);
        if (value.length > 0) {
            snapshot.setMethod(value[0].getClassName() + "::" + value[0].getMethodName());
            data.get(uniqueID).update(snapshot);
        }

    }
    
    public static void showAnalyser() {
        StackAnalyser c = new StackAnalyser();
        c.start(10, TimeUnit.MILLISECONDS);
        TestFrame.show(c);
    }
    

    public static void main(String[] args) {
        StackAnalyser.showAnalyser();
    }

    public FactoryMap<String, ThreadData> getData() {
        return data;
    }

}
