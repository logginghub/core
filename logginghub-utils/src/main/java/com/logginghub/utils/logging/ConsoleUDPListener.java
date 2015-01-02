package com.logginghub.utils.logging;

import java.util.Arrays;

import com.logginghub.utils.Out;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.logging.UDPListener.PatternisedUDPData;

public class ConsoleUDPListener {

    public static int port = VLPorts.getSocketHubUDPPort();

    public static void main(String[] args) {
        UDPListener listener = new UDPListener();
        listener.setPort(port);
        listener.getEventStream().addListener(new StreamListener<UDPListener.PatternisedUDPData>() {
            public void onNewItem(PatternisedUDPData t) {
                Out.out("{} : {} : {}", Logger.toDateString(System.currentTimeMillis()), t.patternID, Arrays.toString(t.parameters));
            }
        });
    }
}
