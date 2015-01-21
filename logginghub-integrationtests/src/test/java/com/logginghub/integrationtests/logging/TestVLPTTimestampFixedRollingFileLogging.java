package com.logginghub.integrationtests.logging;

//import static com.logginghub.performance.console.configuration.VLPTConfigurationBuilder.start;
//
//import java.io.File;
//
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import com.logginghub.logging.DefaultLogEvent;
//import com.logginghub.logging.LogEventBuilder;
//import com.logginghub.logging.LogEventFactory;
//import com.logginghub.logging.logeventformatters.FullEventSingleLineTextFormatter;
//import com.logginghub.logging.messages.LogEventMessage;
//import com.logginghub.logging.modules.TimestampFixedRollingFileLogger;
//import com.logginghub.utils.FileUtils;
//import com.logginghub.utils.StringUtils;
//import com.logginghub.utils.logging.Logger;
//import com.logginghub.performance.common.PerformanceTestAdaptor;
//import com.logginghub.performance.common.TestContext;
//import com.logginghub.performance.console.VLPTConsoleController;
//import com.logginghub.performance.console.configuration.VLPTConfigurationBuilder;

public class TestVLPTTimestampFixedRollingFileLogging {

//    private DefaultLogEvent event;
//
//    private static TimestampFixedRollingFileLogger logger = new TimestampFixedRollingFileLogger();
//
//    private LogEventMessage message;
//    private File tempFolder;
//
//    private long singleItemSize;
//    private long singleItemSizeWithNewline;
//
//    @Before public void setup() {
//
//        tempFolder = FileUtils.createRandomTestFolderForClass(this.getClass());
//        System.out.println(tempFolder.getAbsolutePath());
//
//        logger.setFolder(tempFolder);
//        logger.setFileName("log");
//        logger.setFileExtension(".txt");
//        logger.setTimeFormat("yyyy_MM_dd_HHmmss");
//
//        event = LogEventFactory.createFullLogEvent1();
//        message = new LogEventMessage(event);
//
//        FullEventSingleLineTextFormatter formatter = new FullEventSingleLineTextFormatter();
//        singleItemSize = formatter.format(event).getBytes().length;
//        singleItemSizeWithNewline = singleItemSize + StringUtils.newline.getBytes().length;
//    }
//
//    public final static class Writer extends PerformanceTestAdaptor {
//        int count = 0;
//        @Override public void runIteration(TestContext pti) throws Exception {
//            logger.send(LogEventBuilder.create(System.currentTimeMillis(), Logger.info, "Message : " + count++ + " : " + Thread.currentThread().getName()));
//        }
//    }
//
//    @Ignore // Need to use tags or something for the longer running things?
//    @Test public void test() {
//        VLPTConsoleController controller = start().addTest(VLPTConfigurationBuilder.test(Writer.class).targetRate(-1).threads(10).recordAllValues(false))
//                                                  .warmupTime(1000)
//                                                  .testDuration(5 * 60000)
//                                                  .executeHeadlessNoExit();
//
////        TransactionResultModel results = controller.getModel().getTransactionResultModelForTest("Sender");
////
////        assertThat(results.getFailedTransactions(), is(0L));
//    }

}
