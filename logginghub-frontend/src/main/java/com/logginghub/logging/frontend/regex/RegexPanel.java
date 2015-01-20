package com.logginghub.logging.frontend.regex;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.listeners.LogEventListener;

public class RegexPanel extends JPanel implements LogEventListener
{
    private JTextPane output;
    private StringBuffer buffer = new StringBuffer();
    private String newline = System.getProperty("line.separator");

    /**
     * Create the panel.
     */
    public RegexPanel()
    {
        setLayout(new MigLayout("", "[grow]", "[][64.00][grow]"));

        JButton btnAdd = new JButton("Add");
        add(btnAdd, "cell 0 0");

        JPanel panel = new JPanel();
        add(panel, "cell 0 1,grow");

        RegexElementPanel regexElementPanel = new RegexElementPanel();
        panel.add(regexElementPanel);

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, "cell 0 2,grow");

        output = new JTextPane();
        scrollPane.setViewportView(output);
        output.setText("dsdfgsdfgfsdgfsdgfsd");             
    }

    public void onNewLogEvent(LogEvent event)
    {
        buffer.append(event.getMessage());
        buffer.append(newline);
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                output.setText(buffer.toString());
            }
        });
    }
}
