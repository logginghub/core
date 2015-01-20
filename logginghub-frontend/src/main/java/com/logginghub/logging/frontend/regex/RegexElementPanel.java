package com.logginghub.logging.frontend.regex;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class RegexElementPanel extends JPanel
{
    private JTextField textField;

    /**
     * Create the panel.
     */
    public RegexElementPanel()
    {
        setLayout(new MigLayout("", "[86px][97px][72.00px]", "[23px]"));
        
        textField = new JTextField();
        add(textField, "cell 0 0 2 1,growx,aligny center");
        textField.setColumns(10);
        
        JCheckBox chckbxEnable = new JCheckBox("Enable");
        chckbxEnable.setSelected(true);
        add(chckbxEnable, "cell 2 0,alignx center,aligny top");

    }

}
