package com.logginghub.utils;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

public class StandardFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    public StandardFrame(String title, float screenSizeFactor) {
        super(title);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        setSize(new Dimension((int) (screenSize.width * screenSizeFactor), (int) (screenSize.height *screenSizeFactor)));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {}

            public void windowIconified(WindowEvent e) {}

            public void windowDeiconified(WindowEvent e) {}

            public void windowDeactivated(WindowEvent e) {}

            public void windowClosing(WindowEvent e) {}

            public void windowClosed(WindowEvent e) {}

            public void windowActivated(WindowEvent e) {}
        });
    }

}
