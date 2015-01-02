package com.logginghub.utils.swing;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JTextField;

/**
 * A text field that will invoke the abstract method onTextFieldChanged after a
 * particular delay. The timer gets reset if another character is typed within
 * the timeout milliseconds.
 * 
 * @author James
 * 
 */
public abstract class DelayedProcessingTextField extends JTextField {

    private static final long serialVersionUID = 1L;
    private Timer timer;
    private long timeout;

    public DelayedProcessingTextField(long timeoutMilliseconds) {
        timeout = timeoutMilliseconds;

        addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {

            }

            public void keyReleased(KeyEvent e) {
                updateTimer();
            }

            public void keyPressed(KeyEvent e) {

            }
        });
    }

    protected void updateTimer() {
        // Kill off the existing task if it exists
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        // Create a new instance
        TimerTask task = new TimerTask() {
            @Override public void run() {
                try {
                    onTextFieldChanged(getText());
                }
                catch (Exception e) {
                    System.out.println("Quick filter execution failed : ");
                    e.printStackTrace();
                }
            }
        };

        timer = new Timer("DelayedProcessingTextFieldTimer-" + getName(), true);
        timer.schedule(task, timeout);
    }

    protected abstract void onTextFieldChanged(String text);
}
