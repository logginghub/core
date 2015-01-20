package com.logginghub.logging.frontend;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import com.logginghub.utils.DelayedAction;
import com.logginghub.utils.Metadata;

/**
 * JFrame with a few extra bits, notably settings support.
 * 
 * @author James
 */
public class SmartJFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private Metadata dynamicSettings;
    private DelayedAction action = new DelayedAction(5, TimeUnit.SECONDS);
    private String name = "";

    public SmartJFrame(Metadata settings) {
        this.dynamicSettings = settings;
        initialise();
    }

    public SmartJFrame(String name, Metadata settings) {
        this.dynamicSettings = settings;
        this.name = name + ".";
        initialise();
    }

    public SmartJFrame() {

        dynamicSettings = new Metadata();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int border = 50;
        dynamicSettings.put(name + "frame.state", JFrame.NORMAL);
        dynamicSettings.put(name + "frame.extendedState", JFrame.NORMAL);
        dynamicSettings.put(name + "frame.x", border);
        dynamicSettings.put(name + "frame.y", border);
        dynamicSettings.put(name + "frame.width", screenSize.width - border - border);
        dynamicSettings.put(name + "frame.height", screenSize.height - border - border);

        initialise();
    }

    private void initialise() {

        int border = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        setState(this.dynamicSettings.getInt(name + "frame.state", JFrame.NORMAL));
        setExtendedState(this.dynamicSettings.getInt(name + "frame.extendedState", JFrame.NORMAL));
        setLocation(this.dynamicSettings.getInt(name + "frame.x", 10), this.dynamicSettings.getInt(name + "frame.y", 10));
        setSize(this.dynamicSettings.getInt(name + "frame.width", screenSize.width - border - border), this.dynamicSettings.getInt(name + "frame.height", screenSize.height - border - border));

        addComponentListener(new ComponentListener() {

            public void componentHidden(ComponentEvent e) {

            }

            public void componentMoved(ComponentEvent e) {
                Point location = getLocation();
                SmartJFrame.this.dynamicSettings.put(name + "frame.x", location.x);
                SmartJFrame.this.dynamicSettings.put(name + "frame.y", location.y);
            }

            public void componentResized(ComponentEvent e) {
                Dimension size = getSize();
                SmartJFrame.this.dynamicSettings.put(name + "frame.width", size.width);
                SmartJFrame.this.dynamicSettings.put(name + "frame.height", size.height);
            }

            public void componentShown(ComponentEvent e) {

            }
        });

        addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                SmartJFrame.this.dynamicSettings.put(name + "frame.extendedState", getExtendedState());
                SmartJFrame.this.dynamicSettings.put(name + "frame.state", getState());
            }
        });

    }
}
