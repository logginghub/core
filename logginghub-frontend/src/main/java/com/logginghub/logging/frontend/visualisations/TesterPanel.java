package com.logginghub.logging.frontend.visualisations;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.frontend.modules.EnvironmentMessagingService;
import com.logginghub.logging.messages.LogEventMessage;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.WorkerThread;

public class TesterPanel extends JPanel {

    public TesterPanel() {
        setLayout(new MigLayout());
    }

    public void initialise(String description, final int level, final EnvironmentMessagingService messagingService) {

        addButton(1, 100, description, level, messagingService);
        addButton(10, 100, description, level, messagingService);
        addButton(100, 100, description, level, messagingService);
        addButton(1000, 100, description, level, messagingService);
        addButton(10000, 1000, description, level, messagingService);
        addButton(10000, 10000, description, level, messagingService);
        addButton(10000, 100000, description, level, messagingService);

    }

    private void addButton(final int count, final int period, String description, final int level, final EnvironmentMessagingService messagingService) {

        JButton button = new JButton(StringUtils.format("{}: {}/{}", description, count, period));
        button.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                WorkerThread.execute("LoggingHub-TesterPanel-EventSender", new Runnable() {

                    @Override public void run() {
                        long sleepDuration = period / count;
                        for (int i = 0; i < count; i++) {
                            try {
                                messagingService.send(new LogEventMessage(LogEventBuilder.create(System.currentTimeMillis(),
                                                                                                 level,
                                                                                                 "OperationA completed successfully in 1.23 ms : user data was 'cat'")));
                            }
                            catch (LoggingMessageSenderException e1) {
                                e1.printStackTrace();
                            }
                            ThreadUtils.sleep(sleepDuration);
                        }
                    }
                });
            }
        });

        add(button);
    }

}
