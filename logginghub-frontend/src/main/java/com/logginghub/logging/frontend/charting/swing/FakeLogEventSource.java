package com.logginghub.logging.frontend.charting.swing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.LogEventMultiplexer;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.RandomWithMomentum;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.WorkerThread;

public class FakeLogEventSource {

    private LogEventMultiplexer logEventMultiplexer = new LogEventMultiplexer();

    private List<String> names = new ArrayList<String>();
    private List<String> hosts = new ArrayList<String>();
    private List<String> instances = new ArrayList<String>();
    private List<String> requests = new ArrayList<String>();
    private List<String> failureReasons = new ArrayList<String>();
    private List<String> unsuccessReasons = new ArrayList<String>();

    public void start() {

        final RandomWithMomentum momentumSuccess = new RandomWithMomentum(0, 1, 50, 5, 20);
        final RandomWithMomentum momentumUnsuccess = new RandomWithMomentum(0, 1, 25, 5, 20);
        final RandomWithMomentum momentumeFailure = new RandomWithMomentum(0, 1, 500, 5, 20);

        final Random random = new Random(0);

        requests.add("index.html");
        requests.add("/images/header.png");
        requests.add("/blog/20130729.1.html");
        
        names.add("jshaw");
        names.add("jsmith");
        names.add("wclarke");
        names.add("fx-upstream1");
        names.add("fx-upstream2");
        names.add("dpotter");

        instances.add("pdn-ldn-instance1");
        instances.add("pdn-ldn-instance2");
        instances.add("pdn-ldn-instance3");
        instances.add("pdn-lds-instance1");
        instances.add("pdn-lds-instance2");
        instances.add("pdn-lds-instance3");
        
        hosts.add("pdn-ldn-1");
        hosts.add("pdn-ldn-2");
        hosts.add("pdn-ldn-3");
        hosts.add("pdn-lds-1");
        hosts.add("pdn-lds-2");
        hosts.add("pdn-lds-3");
        
        unsuccessReasons.add("user not authorised");
        unsuccessReasons.add("resource wasn't found");
        
        failureReasons.add("cache request timed out");
        failureReasons.add("database connection failed");
        failureReasons.add("connection lost");
        
        WorkerThread execute = WorkerThread.executeOngoing("FakeSource", new Runnable() {
            @Override public void run() {

                String successPattern = "Load resource completed successfully in {} ms : user '{}' requested url '{}' - resource successfully loaded, {} bytes returned";
                String unsuccessPattern = "Load resource completed unsuccessfully in {} ms : user '{}' requested url '{}' - reason '{}'";
                String failurePattern = "Load resource failed in {} ms : user '{}' requested url '{}' - failure reason '{}'";

                String user = names.get(random.nextInt(names.size()));
                String request = requests.get(random.nextInt(requests.size()));
                String host = hosts.get(random.nextInt(hosts.size()));
                String instance = instances.get(random.nextInt(instances.size()));

                String message;
                int level;

                int nextInt = random.nextInt(100);
                if (nextInt > 99) {
                    String reason = failureReasons.get(random.nextInt(failureReasons.size()));
                    message = StringUtils.format(failurePattern,  momentumeFailure.next(), user, request, reason);
                    level = Level.WARNING.intValue();
                }
                else if (nextInt > 80) {
                    String reason = unsuccessReasons.get(random.nextInt(unsuccessReasons.size()));
                    message = StringUtils.format(unsuccessPattern, momentumUnsuccess.next(), user, request, reason);
                    level = Level.INFO.intValue();
                }
                else {
                    message = StringUtils.format(successPattern, momentumSuccess.next(), user, request, 50 + random.nextInt(100000));
                    level = Level.INFO.intValue();
                }

                DefaultLogEvent logEvent = LogEventBuilder.start().setMessage(message).setLevel(level).setSourceHost(host).setSourceApplication(instance).toLogEvent();
                logEventMultiplexer.onNewLogEvent(logEvent);
            }
        });
        execute.setIterationDelay(100);
    }

    public LogEventMultiplexer getLogEventMultiplexer() {
        return logEventMultiplexer;
    }
    
    public static void main(String[] args) {
        
        FakeLogEventSource eventSource = new FakeLogEventSource();
        eventSource.getLogEventMultiplexer().addLogEventListener(new LogEventListener() {
            @Override public void onNewLogEvent(LogEvent event) {
                System.out.println(event);
            }
        });
        eventSource.start();
        
    }
}

