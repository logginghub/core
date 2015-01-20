package com.logginghub.logging.frontend.visualisations;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.logginghub.utils.swing.TestFrame;

public class Launcher {

    public static void main(String[] args) {
        
        JPanel panel = new JPanel(new MigLayout());
        addButton(panel, "visualisations/basic.xml", "Basic");
        addButton(panel, "visualisations/cache.xml", "Cache view");
        
        TestFrame.show(panel);
    }

    private static void addButton(JPanel panel, final String string, String string2) {
        JButton button = new JButton(string2);
        panel.add(button, "wrap");
        button.addActionListener(new ActionListener() {
            
            @Override public void actionPerformed(ActionEvent e) {
                launch(string);
            }
        });
    }

    protected static void launch(String string) {
        Visualiser visualiser = new Visualiser();
        visualiser.run(new File(string));
    }
    
}
