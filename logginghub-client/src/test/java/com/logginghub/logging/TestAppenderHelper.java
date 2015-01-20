package com.logginghub.logging;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.logginghub.logging.log4j.PublishingListener;
import com.logginghub.testutils.CustomRunner;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Timeout;


@RunWith(CustomRunner.class)
public class TestAppenderHelper {

    @Before public void setup() {
//        MemorySnapshot.runMonitor();
    }
    
    @Test public void test_reconnect_standdown() throws Exception {
     
        AppenderHelper helper = new AppenderHelper("Test", new AppenderHelperCustomisationInterface() {
            
            public HeapLogger createHeapLogger() {
                return null;
            }
            
            public GCFileWatcher createGCWatcher() {
                return null;
            }
            
            public CpuLogger createCPULogger() {
                return null;
            }
        });

        helper.addConnectionPoint(new InetSocketAddress("localhost", NetUtils.findFreePort()));
        
        helper.setDontThrowExceptionsIfHubIsntUp(true);
        
        final Bucket<Long> eventTimes = new Bucket<Long>();
        
        helper.setPublishingListener(new PublishingListener() {
            public void onUnsuccessfullyPublished(LogEvent event, Exception failureReason) {
                eventTimes.add(System.currentTimeMillis());
            }
            public void onSuccessfullyPublished(LogEvent event) {}
        });
        
        
        helper.append(new AppenderHelperEventConvertor() {
            public EventSnapshot createSnapshot() {
                return new EventSnapshot() {
                    public LogEvent rebuildEvent() {
                        return createLogEvent();
                    }
                };
            }
            
            public LogEvent createLogEvent() {
                return LogEventFactory.createFullLogEvent1();
            }
        });
        
        helper.setFailureDelay(50);
        helper.setFailureDelayMaximum(500);
        
        eventTimes.setTimeout(new Timeout(30, TimeUnit.SECONDS));
        eventTimes.waitForMessages(5);
        
        // TODO : figure out what to assert
        helper.close();
        
    }
}
