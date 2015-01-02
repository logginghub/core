package com.logginghub.utils;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

public class SwingHelper {

    public static void dispatch(Runnable runnable) {
        if(SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        }else{
            SwingUtilities.invokeLater(runnable);
        }
    }

    public static void invoke(Runnable runnable) {
        dispatch(runnable);
    }

    public static void waitForQueueToFlush() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {}
            });
        }
        catch (InterruptedException e) {         
        }
        catch (InvocationTargetException e) {         
        }
    }

}
