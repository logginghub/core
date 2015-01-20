package com.logginghub.logging.frontend.charting.swing;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.utils.observable.Binder2;

public class PatternModelEditor extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    private JTextField nameField = new JTextField();
    private JTextField patternField = new JTextField();
    private boolean hasBeenEdited = false;
    
    private Binder2 binder = new Binder2();
    
    public PatternModelEditor() {
        
        setLayout(new MigLayout("fill", "[][grow,fill]", "[][]"));
        
        JLabel lblNewLabel = new JLabel("Pattern name");
        add(lblNewLabel, "cell 0 0");

        add(nameField, "cell 1 0");
        
        patternField.addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}
            @Override public void keyReleased(KeyEvent e) {
                hasBeenEdited = true;
            }
            @Override public void keyPressed(KeyEvent e) {}
        });
        
        JLabel lblNewLabel_1 = new JLabel("Pattern");
        add(lblNewLabel_1, "cell 0 1");
        add(patternField, "cell 1 1");
    }

    public boolean isEdited() {
        return hasBeenEdited;
    }
    
    public void bind(PatternModel model) { 
        binder.bind(model.getName(), nameField);
        binder.bind(model.getPattern(), patternField);
    }

    public void setCaretAtStart() {
        nameField.setCaretPosition(0);
        patternField.setCaretPosition(0);
    }

    public void setEdited(boolean b) {
        this.hasBeenEdited = b;
    }

    public void unbind(PatternModel model) {
        binder.unbind();
    }
    
}
