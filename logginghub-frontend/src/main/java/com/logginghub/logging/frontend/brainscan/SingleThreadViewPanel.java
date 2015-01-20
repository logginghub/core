package com.logginghub.logging.frontend.brainscan;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.observable.ObservablePropertyListener;

public class SingleThreadViewPanel extends JPanel {
    private static final Font monospaced10 = new Font("Monospaced", Font.PLAIN, 10);
    private static final Font tahoma11 = new Font("Tahoma", Font.BOLD, 11);
    private JTextArea stackArea;
    private JLabel stateLabel;
    private Binder2 binder;
    private JLabel nameLabel;
    private SingleThreadViewModel model;

    public SingleThreadViewPanel() {
        setLayout(new MigLayout("", "[grow][]", "[][grow]"));

        nameLabel = new JLabel("");
        nameLabel.setFont(tahoma11);
        add(nameLabel, "cell 0 0");

        stateLabel = new JLabel("");
        stateLabel.setFont(tahoma11);
        add(stateLabel, "cell 1 0");

        stackArea = new JTextArea();
        stackArea.setFont(monospaced10);
        stackArea.setEditable(false);
        add(stackArea, "cell 0 1 2 1,grow");
        
        stackArea.setOpaque(false);

    }

    public void bind(SingleThreadViewModel model) {
        this.model = model;
        binder = new Binder2();
        binder.bind(model.getName(), nameLabel);
        binder.bind(model.getStack(), stackArea);
        binder.bind(model.getState(), stateLabel);

        binder.attachPropertyListenerAndNotifyCurrent(model.getState(), new ObservablePropertyListener<String>() {
            @Override public void onPropertyChanged(String oldValue, String newValue) {
                if (newValue.equals("RUNNABLE")) {
                    setBackground(ColourUtils.mildGreen);
                }
                if (newValue.equals("NEW")) {
                    setBackground(ColourUtils.mildBlue);
                }
                else if (newValue.equals("WAITING")) {
                    setBackground(ColourUtils.mildYellow);
                }
                else if (newValue.equals("TIMED_WAITING")) {
                    setBackground(ColourUtils.mildOrange);
                }
                else if (newValue.equals("TERMINATED")) {
                    setBackground(ColourUtils.mildRed);
                }
                else if (newValue.equals("BLOCKED")) {
                    setBackground(ColourUtils.mildGrey);
                }
            }
        });

    }

    public SingleThreadViewModel getModel() {
        return model;
    }

    public void unbind(SingleThreadViewModel model) {
        binder.unbind();
    }

}
