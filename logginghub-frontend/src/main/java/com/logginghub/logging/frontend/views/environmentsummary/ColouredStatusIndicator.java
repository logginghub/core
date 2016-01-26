package com.logginghub.logging.frontend.views.environmentsummary;

import com.logginghub.logging.frontend.model.EnvironmentLevelStatsModel;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.observable.ObservableItemContainer;
import com.logginghub.utils.observable.ObservableListener;
import com.logginghub.utils.swing.TestFrame;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

public class ColouredStatusIndicator extends JPanel {
    private static final long serialVersionUID = 1L;
    private JLabel lblUp;
    private JLabel lblDown;
    private JLabel lblNewLabel;
    private ResizingLabel resizingLabel;
    private NumberFormat numberFormatInstance = NumberFormat.getInstance();

    public ColouredStatusIndicator() {
        setLayout(new MigLayout("", "[276.00,grow,fill]", "[grow][grow][grow][]"));

        resizingLabel = new ResizingLabel((String) null);
        resizingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resizingLabel.setText("0");
        resizingLabel.setName("valueResizingLabel");
        add(resizingLabel, "cell 0 0 1 3");

        lblUp = new ResizingLabel("˄");
        // lblUp.setFont(getFont().deriveFont(500));
        // add(lblUp, "cell 1 0");

        lblNewLabel = new ResizingLabel("=");
        // add(lblNewLabel, "cell 1 1");

        lblDown = new ResizingLabel("˅");
    }

    public static void main(String[] args) {
        TestFrame.show(new ColouredStatusIndicator(), 800, 600);
    }

    public void bind(final EnvironmentLevelStatsModel model, final EnvironmentLevelStatsModel perSecondModel) {

        model.addListener(new ObservableListener() {
            @Override
            public void onChanged(ObservableItemContainer observable, Object childPropertyThatChanged) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        update(model, perSecondModel);
                    }
                });
            }
        });


        perSecondModel.addListener(new ObservableListener() {
            @Override
            public void onChanged(ObservableItemContainer observable, Object childPropertyThatChanged) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        update(model, perSecondModel);
                    }
                });
            }
        });
    }

    private void update(EnvironmentLevelStatsModel model, EnvironmentLevelStatsModel perSecondModel) {

        int value = model.getValue().get();
        int perSecond = perSecondModel.getValue().get();

        if (perSecond > 0) {
            resizingLabel.setText(StringUtils.format("{} (+{})", numberFormatInstance.format(value), numberFormatInstance.format(perSecond)));
        } else {
            resizingLabel.setText(numberFormatInstance.format(value));
        }

        switch (model.getTrend().get()) {
            case Down:
            case Same:
            case Up:
        }

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}
