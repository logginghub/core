import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.ThreadUtils;

import java.net.InetSocketAddress;

/**
 * Created by james on 13/05/2015.
 */
public class SpikingTest {

    public static void main(String[] args) throws ConnectorException, LoggingMessageSenderException {

        SocketClient client = new SocketClient();
        client.addConnectionPoint(new InetSocketAddress("localhost", 15000));
        client.connect();

        int time = 1000;
        int messages = 1000000;
        for(int i = 0; i < messages; i++) {
            String message = StringUtils.format("PerformTask for [Task=1] [ServiceSessionId=3047553611231886434] [Engine=LocalHost-0] completed in 4443517872ns ({}ms)", time++);
            client.send(new LogEventMessage(LogEventBuilder.start().setMessage(message).toLogEvent()));
            ThreadUtils.sleep(1000);
        }

    }
}
