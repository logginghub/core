package com.logginghub.logging.frontend;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

public class WebStyleChooser extends JPanel
{
    private int m_choiceIndex = -1;
    private JEditorPane m_editorPane;
    private String[] m_options;

    public WebStyleChooser(String[] options)
    {
        m_editorPane = new JEditorPane();
        m_options = options;

        m_editorPane.setContentType("text/html");
        m_editorPane.setBackground(Color.white);
        
        m_editorPane.setEditable(false);
        m_editorPane.addHyperlinkListener(new HyperlinkListener()
        {
            public void hyperlinkUpdate(HyperlinkEvent hyperlink)
            {
                EventType eventType = hyperlink.getEventType();
                if(eventType == eventType.ACTIVATED)
                {
                    String description = hyperlink.getDescription();
                    System.out.println("Selected: " + description);
                    m_choiceIndex = Integer.parseInt(description);
                    redraw();
                }
            }
        });

        redraw();

        setLayout(new BorderLayout());
        add(m_editorPane, BorderLayout.CENTER);
    }

    private void redraw()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<style type='text/css'>");
        sb.append("a { text-decoration: none; }");
        sb.append("a.selected { text-decoration: none; font-size: 110%; color: #ffcc00 }");
        sb.append("a.not-selected { text-decoration: none; }");
        sb.append("</style>");

        sb.append("</head>");
        sb.append("<body>");

        int index = 0;
        for(String word : m_options)
        {
            String style;

            if(index == m_choiceIndex)
            {
                style = "selected";
            }
            else
            {
                style = "not-selected";
            }

            sb.append("<a class='" + style
                      + "' href='"
                      + Integer.toString(index)
                      + "'>"
                      + word
                      + "</a><br/>");
            index++;
        }

        sb.append("</html></body>");

        m_editorPane.setText(sb.toString());
    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame();
        frame.setSize(400, 400);
        frame.getContentPane().add(new WebStyleChooser(new String[]
        {
            "Option 1", "Option 2", "Option 3"
        }));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public String getChoice()
    {
        String choice;
        
        if(m_choiceIndex != -1)
        {
            choice = m_options[m_choiceIndex];
        }
        else
        {
            choice = null;
        }
        
        return choice;
    }
}
