package com.logginghub.logging.launchers;

/**
 * Created by james on 28/01/15.
 */
public class HubWithPeriodicStackCaptureAndHistory {
    public static void main(String[] args) {
        RunHub.fromConfiguration("src/main/resources/configs/hub/hub.with.periodic.stack.capture.and.history.xml");
    }
}
