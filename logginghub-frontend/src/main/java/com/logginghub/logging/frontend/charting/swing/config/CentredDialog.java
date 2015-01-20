package com.logginghub.logging.frontend.charting.swing.config;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class CentredDialog extends JDialog {

    public static JDialog create(String title, Container parentComponent) {

        JDialog dialog = null;

        boolean done = false;
        Container parent = parentComponent;
        while (!done) {
            parent = parent.getParent();

            if (parent instanceof JDialog) {
                JDialog parentDialog = (JDialog) parent;
                dialog = new JDialog(parentDialog);
                done = true;
            }
            else if (parent instanceof JFrame) {
                JFrame frame = (JFrame) parent;
                dialog = new JDialog(frame);
                done = true;
            }
            else if (parent == null) {
                done = true;
            }
        }

        if (dialog == null) {
            dialog = new JDialog();
        }

        int width = (int) (parent.getWidth() * 0.8);
        int height = (int) (parent.getHeight() * 0.8f);

        int offsetX = (int) (parent.getWidth() * 0.2) / 2;
        int offsetY = (int) (parent.getHeight() * 0.2f) / 2;

        dialog.setLocation(parent.getX() + offsetX, parent.getY() + offsetY);
        dialog.setSize(width, height);

        dialog.setTitle(title);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        return dialog;

    }

    public static void centre(JDialog dialog, Component relative) {
        
        int thisWidth = dialog.getWidth();
        int thisHeight = dialog.getHeight();
        
        int otherWidth = relative.getWidth();
        int otherHeight = relative.getHeight();
        
        int midX = relative.getX() + (otherWidth/2) - (thisWidth/2);
        int midY = relative.getY() + (otherHeight/2) - (thisHeight/2);
        
//        int offsetX = midX - thisWidth/2;
//        int offsetY = midY - thisHeight/2;

//        dialog.setLocation(offsetX, offsetY);
        dialog.setLocation(midX, midY);
    }

}
