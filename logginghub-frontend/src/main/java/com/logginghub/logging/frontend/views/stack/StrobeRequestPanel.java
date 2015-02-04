package com.logginghub.logging.frontend.views.stack;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.messages.StackStrobeRequest;
import com.logginghub.utils.Stream;
import com.logginghub.utils.TimeUtils;

public class StrobeRequestPanel extends JPanel {
    private JTextField instanceSelector;
    
    private Stream<StackStrobeRequest> requestStream = new Stream<StackStrobeRequest>();

    private JTextField strobeDuration;

    private JSpinner snapshotCountSpinner;

    public StrobeRequestPanel() {
        setLayout(new MigLayout("", "[][][][46.00,fill][][grow][]", "[]"));

        JLabel lblNewLabel = new JLabel("Instance Selector");
        add(lblNewLabel, "cell 0 0,alignx trailing");

        instanceSelector = new JTextField();
        instanceSelector.setText("*");
        add(instanceSelector, "cell 1 0,growx");
        instanceSelector.setColumns(10);

        JLabel lblNewLabel_1 = new JLabel("Numer of snapshots");
        add(lblNewLabel_1, "flowx,cell 2 0");

        snapshotCountSpinner = new JSpinner();
        snapshotCountSpinner.setModel(new SpinnerNumberModel(new Integer(10), new Integer(0), null, new Integer(1)));
        add(snapshotCountSpinner, "cell 3 0");

        JLabel lblNewLabel_2 = new JLabel("Strobe duration");
        add(lblNewLabel_2, "cell 4 0,alignx trailing");

        strobeDuration = new JTextField();
        strobeDuration.setText("10 seconds");
        add(strobeDuration, "cell 5 0,growx");
        strobeDuration.setColumns(10);

        JButton btnNewButton = new JButton("Strobe");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendStrobeRequest();
            }
        });
        add(btnNewButton, "cell 6 0");
    }

    protected void sendStrobeRequest() {
        StackStrobeRequest request = new StackStrobeRequest();
        request.setInstanceSelector(instanceSelector.getText());
        request.setIntervalLength(TimeUtils.parseInterval(strobeDuration.getText()));
        request.setSnapshotCount((Integer) snapshotCountSpinner.getValue());
        requestStream.send(request);
    }
    
    public Stream<StackStrobeRequest> getRequestStream() {
        return requestStream;
    }

}
