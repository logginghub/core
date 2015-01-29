package com.logginghub.logging.launchers;

/**
 * Created by james on 28/01/15.
 */
public class HubWithPeriodicStackCapture{
    public static void main(String[] args) {
        RunHub.fromConfiguration("src/main/resources/configs/hub/hub.with.periodic.stack.capture.xml");
    }
}
