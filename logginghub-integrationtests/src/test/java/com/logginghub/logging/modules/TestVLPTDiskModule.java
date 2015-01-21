package com.logginghub.logging.modules;

//import static com.logginghub.performance.console.configuration.VLPTConfigurationBuilder.start;
//
//import java.io.File;
//import java.util.concurrent.CountDownLatch;
//
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import com.logginghub.logging.DefaultLogEvent;
//import com.logginghub.logging.LogEventBuilder;
//import com.logginghub.logging.exceptions.LoggingMessageSenderException;
//import com.logginghub.logging.interfaces.QueueAwareLoggingMessageSender;
//import com.logginghub.logging.messages.HistoricalDataRequest;
//import com.logginghub.logging.messages.HistoricalDataResponse;
//import com.logginghub.logging.messages.LoggingMessage;
//import com.logginghub.logging.modules.DiskHistoryModule;
//import com.logginghub.logging.modules.configuration.DiskHistoryConfiguration;
//import com.logginghub.utils.FileUtils;
//import com.logginghub.utils.MutableInt;
//import com.logginghub.utils.StringUtils;
//import com.logginghub.utils.ThreadUtils;
//import com.logginghub.utils.logging.Logger;
//import com.logginghub.utils.module.ProxyServiceDiscovery;
//import com.logginghub.performance.common.PerformanceTestAdaptor;
//import com.logginghub.performance.common.TestContext;
//import com.logginghub.performance.console.VLPTConsoleController;
//import com.logginghub.performance.console.configuration.VLPTConfigurationBuilder;

public class TestVLPTDiskModule {

//    private File tempFolder;
//    private static DiskHistoryModule diskHistoryModule = new DiskHistoryModule();
//
//    @Before public void setup() {
//
//        tempFolder = FileUtils.createRandomTestFolderForClass(this.getClass());
//        tempFolder = new File("X:\\vlcoregit\\vl-integrationtests\\target\\test\\TestVLPTDiskModule50");
//        System.out.println(tempFolder.getAbsolutePath());
//
//        DiskHistoryConfiguration configuration = new DiskHistoryConfiguration();
//        configuration.setFolder(tempFolder.getAbsolutePath());
//        diskHistoryModule.configure(configuration, new ProxyServiceDiscovery());
//
//    }
//
//    public final static class Writer extends PerformanceTestAdaptor {
//        int count = 0;
//
//        private DefaultLogEvent event = LogEventBuilder.create(0, Logger.info, "Event");
//
//        @Override public void beforeIteration(TestContext pti) throws Exception {
//            event.setLocalCreationTimeMillis(count);
//            event.setMessage(StringUtils.format("This is event {} on thread {}", count++, Thread.currentThread().getName()));
//        }
//
//        @Override public void runIteration(TestContext pti) throws Exception {
//            diskHistoryModule.send(event);
//        }
//    }
//
//    public final static class Reader extends PerformanceTestAdaptor {
//
//        @Override public void runIteration(TestContext pti) throws Exception {
//
//            final MutableInt count = new MutableInt(0);
//            final CountDownLatch latch = new CountDownLatch(1);
//
//            HistoricalDataRequest request = new HistoricalDataRequest(pti.getIntegerProperty("startTime", 0), pti.getIntegerProperty("endTime", 1000));
//
//            QueueAwareLoggingMessageSender source = new QueueAwareLoggingMessageSender() {
//                @Override public void send(LoggingMessage message) throws LoggingMessageSenderException {
//                    HistoricalDataResponse response = (HistoricalDataResponse) message;
//                    count.increment(response.getEvents().length);
//                    if (response.isLastBatch()) {
//                        latch.countDown();
//                    }
//                }
//
//                @Override public boolean isSendQueueEmpty() {
//                    return true;
//                }
//            };
//
//            diskHistoryModule.handleDataRequestStreaming(request, source);
//
//            latch.await();
//            // System.out.println(count);
//        }
//    }
//
//    @Ignore
//    @Test public void test() {
//        VLPTConsoleController controller = start().addTest(VLPTConfigurationBuilder.test(Writer.class)
//                                                                                   .targetRate(0)
//                                                                                   .rateStep(10000)
//                                                                                   .threads(1)
//                                                                                   .recordAllValues(false))
//                                                  .addTest(VLPTConfigurationBuilder.test(Reader.class)
//                                                                                   .targetRate(1)
//                                                                                   .properties("startTime=0,endTime=1000")
//                                                                                   .name("Early read")
//                                                                                   .threads(1)
//                                                                                   .recordAllValues(false))
//                                                  .addTest(VLPTConfigurationBuilder.test(Reader.class)
//                                                                                   .targetRate(1)
//                                                                                   .properties("startTime=10001000,endTime=10001000")
//                                                                                   .name("Late read")
//                                                                                   .threads(1)
//                                                                                   .recordAllValues(false))
//                                                  .warmupTime(1000)
//                                                  .testDuration(5 * 60000)
//                                                  .outputEmbeddedAgentStats(false)
//                                                  .outputControllerStats(false)
//                                                  .autostart(1)
//                                                  .execute();
//
//        ThreadUtils.sleep(1000000);
//
//        // TransactionResultModel results =
//        // controller.getModel().getTransactionResultModelForTest("Sender");
//        //
//        // assertThat(results.getFailedTransactions(), is(0L));
//    }
}
