package com.logginghub.integrationtests.logging;

//import static com.logginghub.performance.console.configuration.VLPTConfigurationBuilder.start;
//import static com.logginghub.performance.console.configuration.VLPTConfigurationBuilder.test;
//
//import java.net.InetSocketAddress;
//
//import org.junit.Ignore;
//import org.junit.Test;
//
//import com.logginghub.logging.AppenderHelper;
//import com.logginghub.logging.AppenderHelperCustomisationInterface;
//import com.logginghub.logging.AppenderHelperEventConvertor;
//import com.logginghub.logging.CpuLogger;
//import com.logginghub.logging.EventSnapshot;
//import com.logginghub.logging.GCFileWatcher;
//import com.logginghub.logging.HeapLogger;
//import com.logginghub.logging.LogEvent;
//import com.logginghub.logging.LogEventBuilder;
//import com.logginghub.logging.servers.SocketHub;
//import com.logginghub.performance.common.PerformanceTestAdaptor;
//import com.logginghub.performance.common.TestContext;
//import com.logginghub.performance.console.VLPTConsoleController;
//import com.logginghub.performance.console.model.TransactionResultModel;

//@Ignore // Need to move VLPT tests to their own project!
public class TestVLPTAppenderHelper {

//    private static AppenderHelper appender = new AppenderHelper("Appender", new AppenderHelperCustomisationInterface() {
//
//        @Override public HeapLogger createHeapLogger() {
//            return null;
//        }
//
//        @Override public GCFileWatcher createGCWatcher() {
//            return null;
//        }
//
//        @Override public CpuLogger createCPULogger() {
//            return null;
//        }
//    });
//
//    private static LogEvent event = LogEventBuilder.start().toLogEvent();
//
//    static {
//    }
//
//    public final static class Sender extends PerformanceTestAdaptor {
//
//        AppenderHelperEventConvertor convertor = new AppenderHelperEventConvertor() {
//            @Override public EventSnapshot createSnapshot() {
//                return new EventSnapshot() {
//                    @Override public LogEvent rebuildEvent() {
//                        return event;
//                    }
//                };
//            }
//
//            @Override public LogEvent createLogEvent() {
//                return event;
//            }
//        };
//
//        @Override public void runIteration(TestContext pti) throws Exception {
//            appender.append(convertor);
//        }
//    }
//
//    @Test public void test_just_sender() {
//        SocketHub hub = SocketHub.createTestHub();
//        hub.start();
//
//        appender.addConnectionPoint(new InetSocketAddress(hub.getPort()));
//
//
//        VLPTConsoleController controller = start().addTest(test(Sender.class).targetRate(-1).threads(10).threadStep(10).recordAllValues(false))
//                                                  .warmupTime(1000)
//                                                  .testDuration(5000)
//                                                  .executeHeadlessNoExit();
//
//        TransactionResultModel results = controller.getModel().getTransactionResultModelForTest("Sender");
//
//        hub.stop();
//
//    }

}
