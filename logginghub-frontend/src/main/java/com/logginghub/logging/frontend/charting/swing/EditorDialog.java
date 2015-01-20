package com.logginghub.logging.frontend.charting.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

public abstract class EditorDialog extends JDialog {
    
    private JButton okButton = new JButton("Apply");
    private JButton cancelButton = new JButton("Cancel");
    private JPanel content = new JPanel(new BorderLayout());
    
    private EventSource eventSource = new EventSource("DialogOption");
    
    public EditorDialog() {
        getContentPane().setLayout(new MigLayout("fill", "[fill]", "[grow,fill][fill]"));        
        setResizable(false);
        
        setBackground(Color.white);
        getContentPane().setBackground(Color.white);
        
        content.setOpaque(false);
        content.add(new JTextArea("This is the content area"));
        
        getContentPane().add(content, "cell 0 0");
        
        okButton.setName("Apply");
        
        JPanel buttonBar = new JPanel();
        buttonBar.setOpaque(false);
        buttonBar.setLayout(new MigLayout("fill", "[center]", "[]"));
        buttonBar.add(okButton, "cell 0 0");
        buttonBar.add(cancelButton, "cell 0 0");
        getContentPane().add(buttonBar, "cell 0 1,alignx center");
        
        okButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                eventSource.fireEvent(true);
                dispose();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                eventSource.fireEvent(false);
                dispose();
            }
        });
    }
    
    public EventSource getEventSource() {
        return eventSource;
    }
    
    public void show(String title, JPanel panel, Component parent) {
        setTitle(title);
        content.add(panel, BorderLayout.CENTER);
        pack();
        
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        int parentHeight = parent.getHeight();
        int parentWidth = parent.getWidth();

        int width = getWidth();
        int height = getHeight();

        int offsetX = ((int) (parentWidth ) / 2) - (int)(width / 2);
        int offsetY = ((int) (parentHeight ) / 2) - (int)(height / 2);

        int parentX = parent.getLocationOnScreen().x;
        int parentY = parent.getLocationOnScreen().y;
        
        setLocation(parentX + offsetX, parentY + offsetY);
        setVisible(true);
    }

}
