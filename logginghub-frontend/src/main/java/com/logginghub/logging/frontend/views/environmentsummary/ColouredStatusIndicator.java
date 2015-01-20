package com.logginghub.logging.frontend.views.environmentsummary;

import java.awt.Dimension;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.model.EnvironmentLevelStatsModel;
import com.logginghub.logging.frontend.model.ObservableModelListener;
import com.logginghub.logging.frontend.model.ObservableModel.FieldEnumeration;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.swing.TestFrame;

public class ColouredStatusIndicator extends JPanel {
    private static final long serialVersionUID = 1L;
    private JLabel lblUp;
    private JLabel lblDown;
    private JLabel lblNewLabel;
    private ResizingLabel resizingLabel;

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

    @Override public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override public Dimension getPreferredSize() {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static void main(String[] args) {
        TestFrame.show(new ColouredStatusIndicator(), 800, 600);
    }

    public void bind(final EnvironmentLevelStatsModel model, final EnvironmentLevelStatsModel perSecondModel) {

        model.addListener(new ObservableModelListener() {
            @Override public void onFieldChanged(FieldEnumeration fe, Object value) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        update(model, perSecondModel);
                    }
                });
            }
        });

        perSecondModel.addListener(new ObservableModelListener() {
            @Override public void onFieldChanged(FieldEnumeration fe, Object value) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        update(model, perSecondModel);
                    }
                });
            }
        });
    }

    private NumberFormat numberFormatInstance = NumberFormat.getInstance();

    private void update(EnvironmentLevelStatsModel model, EnvironmentLevelStatsModel perSecondModel) {

        int value = model.getValue();
        int perSecond = perSecondModel.getValue();

        if (perSecond > 0) {
            resizingLabel.setText(StringUtils.format("{} (+{})", numberFormatInstance.format(value), numberFormatInstance.format(perSecond)));
        }
        else {
            resizingLabel.setText(numberFormatInstance.format(value));
        }

        switch (model.getTrend()) {
            case Down:
            case Same:
            case Up:
        }

    }
  
}
