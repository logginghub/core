package com.logginghub.utils.swing;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;

public class TestFrame {

    public static JFrame show(Component c) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(c);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return frame;
    }

    public static JFrame show(Component c, int width, int height) {
        return show("TestFrame", c, width, height);
    }

    public static JFrame show(String title, Component c, int width, int height) {
        JFrame frame = new JFrame(title);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(c, BorderLayout.CENTER);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return frame;
    }

}
