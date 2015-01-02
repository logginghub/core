package com.logginghub.utils.timer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TimerGui
{
    private List<InternalTimer> m_internalTimers = new ArrayList<InternalTimer>();

    class InternalTimer
    {
        public long target;

        public JLabel label;
    }

    public static void main(String[] args)
    {
        TimerGui timer = new TimerGui();
        timer.run();
    }

    private void run()
    {
        JFrame frame = new JFrame("Simple timers");

        JPanel panel = new JPanel(new BorderLayout());
        final JPanel content = new JPanel(new GridLayout(-1, 1));
        content.setBorder(BorderFactory.createTitledBorder("Timers appear here"));
        frame.getContentPane().add(panel);
        frame.setSize(400, 300);

        JPanel buttons = new JPanel();

        final JComboBox list = new JComboBox(new Object[] { 5, 10, 15, 20, 25, 30, 45, 60 });
        
        buttons.setBorder(BorderFactory.createTitledBorder("Add new timers here"));
        JButton addTimer = new JButton("Add");
        addTimer.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                final JPanel timerPanel = new JPanel(new BorderLayout());
                JLabel label = new JLabel("Timer value should be here");
                label.setHorizontalAlignment(JLabel.CENTER);
                Font font = label.getFont();
                Font deriveFont = font.deriveFont(40f);
                label.setFont(deriveFont);

                int minutes = (Integer)list.getSelectedObjects()[0];
                final InternalTimer timer = new InternalTimer();
                timer.target = System.currentTimeMillis() + (minutes * 60 * 1000);
                timer.label = label;
                m_internalTimers.add(timer);

                timerPanel.add(label, BorderLayout.CENTER);

                Icon icon = new ImageIcon(this.getClass()
                                              .getResource("delete.png"));
                JLabel delete = new JLabel(icon);                
                delete.addMouseListener(new MouseAdapter()                
                {
                    @Override public void mouseReleased(MouseEvent e)
                    {
                        content.remove(timerPanel);
                        m_internalTimers.remove(timer);
                        content.doLayout();
                        content.repaint();
                    }
                });
                delete.setToolTipText("Delete this timer");
                timerPanel.add(delete, BorderLayout.EAST);

                content.add(timerPanel);
                content.doLayout();
            }
        });
        buttons.add(addTimer);
        buttons.add(list);


        panel.add(buttons, BorderLayout.SOUTH);
        panel.add(content, BorderLayout.CENTER);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        startTimer();
    }

    private void startTimer()
    {
        Timer timer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override public void run()
            {
                long now = System.currentTimeMillis();
                for (InternalTimer timer : m_internalTimers)
                {
                    long delta = timer.target - now;

                    float seconds = delta / 1000f;

                    int minutes = (int) (seconds / 60);
                    int remainderSeconds = (int) (seconds % 60);
                    int remainderMillis = (int) (delta % 1000);

                    timer.label.setText(String.format("%02d:%02d:%03d",
                                                      minutes,
                                                      remainderSeconds,
                                                      remainderMillis));
                }
            }
        };

        timer.schedule(task, 10, 10);
    }
}
