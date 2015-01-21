package com.logginghub.integrationtests.logging;

//import static com.logginghub.performance.console.configuration.VLPTConfigurationBuilder.start;
//import static com.logginghub.performance.console.configuration.VLPTConfigurationBuilder.test;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.is;
//
//import org.junit.Ignore;
//import org.junit.Test;
//
//import com.logginghub.logging.LogEventBuilder;
//import com.logginghub.logging.messages.HistoricalDataRequest;
//import com.logginghub.logging.messages.HistoricalIndexRequest;
//import com.logginghub.logging.modules.InMemoryHistoryModule;
//import com.logginghub.logging.modules.configuration.InMemoryHistoryConfiguration;
//import com.logginghub.utils.logging.Logger;
//import com.logginghub.utils.module.ProxyServiceDiscovery;
//import com.logginghub.performance.common.PerformanceTestAdaptor;
//import com.logginghub.performance.common.TestContext;
//import com.logginghub.performance.console.VLPTConsoleController;
//import com.logginghub.performance.console.model.TransactionResultModel;

//@Ignore // move VLPT to its own project
public class TestVLPTInMemoryHistoryModule {
//
//    private static InMemoryHistoryConfiguration configuration = new InMemoryHistoryConfiguration();
//    private static InMemoryHistoryModule history = new InMemoryHistoryModule();
//
//    static {
//        history.configure(configuration, new ProxyServiceDiscovery());
//    }
//
//    public final static class Sender extends PerformanceTestAdaptor {
//        @Override public void runIteration(TestContext pti) throws Exception {
//            history.handleNewEvent(LogEventBuilder.create(System.currentTimeMillis(), Logger.info, "Message"));
//        }
//    }
//
//    public final static class IndexLoader extends PerformanceTestAdaptor {
//        @Override public void runIteration(TestContext pti) throws Exception {
//            history.handleIndexRequest(new HistoricalIndexRequest(0, Long.MAX_VALUE));
//        }
//    }
//
//    public final static class DataLoader extends PerformanceTestAdaptor {
//        @Override public void runIteration(TestContext pti) throws Exception {
//            history.handleDataRequest(new HistoricalDataRequest(0, Long.MAX_VALUE));
//        }
//    }
//
//    @Test public void test_just_sender() {
//        VLPTConsoleController controller = start().addTest(test(Sender.class).targetRate(-1).recordAllValues(false))
//                                                  .warmupTime(1000)
//                                                  .testDuration(5000)
//                                                  .executeHeadlessNoExit();
//
//        TransactionResultModel results = controller.getModel().getTransactionResultModelForTest("Sender");
//
//        assertThat(results.getFailedTransactions(), is(0L));
//        // TODO : these break the compiler for some reason, but not in eclipse
////        assertThat(results.getSuccessTransactions(), is(both(greaterThan(1400000L)).and(lessThan(15000000L))));
////        assertThat(results.getSuccessfulTransactionsAverageNanos(), is(both(greaterThan(2000d)).and(lessThan(4000d))));
//
//    }
//
//    @Ignore // TODO : contention really hurts!!
//    @Test public void test_just_sender_contention() {
//        VLPTConsoleController controller = start().addTest(test(Sender.class).targetRate(-1).recordAllValues(false).threads(5).threadStep(5))
//                                                  .warmupTime(1000)
//                                                  .testDuration(5000)
//                                                  .executeHeadlessNoExit();
//
//        TransactionResultModel results = controller.getModel().getTransactionResultModelForTest("Sender");
//
//        assertThat(results.getFailedTransactions(), is(0L));
////        assertThat(results.getSuccessTransactions(), is(both(greaterThan(1400000L)).and(lessThan(15000000L))));
////        assertThat(results.getSuccessfulTransactionsAverageNanos(), is(both(greaterThan(2000d)).and(lessThan(4000d))));
//
//    }
//
//    @Ignore // TODO: fix VLPT so it cleans up properly and can run multiple tests
//    @Test public void test_all_together() {
//        VLPTConsoleController controller = start().addTest(test(Sender.class).targetRate(-1).recordAllValues(false))
//                                                  .addTest(test(IndexLoader.class).targetRate(1).recordAllValues(false))
//                                                  .addTest(test(DataLoader.class).targetRate(1).recordAllValues(false))
//                                                  .warmupTime(1000)
//                                                  .testDuration(5000)
//                                                  .executeHeadlessNoExit();
//
//        TransactionResultModel results = controller.getModel().getTransactionResultModelForTest("Sender");
//
//        assertThat(results.getFailedTransactions(), is(0L));
////        assertThat(results.getSuccessTransactions(), is(both(greaterThan(1400000L)).and(lessThan(15000000L))));
////        assertThat(results.getSuccessfulTransactionsAverageNanos(), is(both(greaterThan(2000d)).and(lessThan(4000d))));
//    }

}
