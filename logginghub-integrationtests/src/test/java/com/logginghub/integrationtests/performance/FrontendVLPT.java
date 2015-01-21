package com.logginghub.integrationtests.performance;

//import static com.logginghub.performance.console.configuration.VLPTConfigurationBuilder.agent;
//import static com.logginghub.performance.console.configuration.VLPTConfigurationBuilder.builder;
//import static com.logginghub.performance.console.configuration.VLPTConfigurationBuilder.embeddedAgent;
//import static com.logginghub.performance.console.configuration.VLPTConfigurationBuilder.test;
//
//import java.net.InetSocketAddress;
//
//import com.logginghub.logging.LogEventBuilder;
//import com.logginghub.logging.messages.LogEventMessage;
//import com.logginghub.logging.messaging.SocketClient;
//import com.logginghub.utils.VLPorts;
//import com.logginghub.performance.common.PerformanceTestAdaptor;
//import com.logginghub.performance.common.TestContext;

public class FrontendVLPT {

//    public abstract static class Base extends PerformanceTestAdaptor {
//
//        private SocketClient client = new SocketClient("a");
//
//        @Override public void setup(TestContext pti) throws Exception {
//            client.addConnectionPoint(new InetSocketAddress("localhost", VLPorts.getSocketHubDefaultPort()));
//            client.setAutoGlobalSubscription(false);
//            client.connect();
//        }
//
//        @Override public void runIteration(TestContext pti) throws Exception {
//            String message = getMessage();
//            client.send(new LogEventMessage(LogEventBuilder.start().setMessage(message).toLogEvent()));
//
//        }
//
//        public abstract String getMessage();
//
//    }
//
//    public static class PatternA extends Base {
//        @Override public String getMessage() {
//            return "Balls";
//        }
//    }
//
//    public static void main(String[] args) {
//        builder().addTest(test(PatternA.class).threads(1))
//                 .addAgent(embeddedAgent())
//                 .addAgent(agent().address("localhost").port(444).name("agent1"))
//                 .autostart(1)
//                 .execute();
//    }

}