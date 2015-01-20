package com.logginghub.logging.frontend.views.environmentsummary;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.model.EnvironmentLevelStatsModel;
import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.logging.frontend.model.EnvironmentSummaryModel;
import com.logginghub.logging.frontend.model.EnvironmentLevelStatsModel.Level;
import com.logginghub.utils.swing.TestFrame;

public class EnvironmentSummaryPanel extends JPanel {
    private ResizingLabel environmentName;

    private ColouredStatusIndicator severeIndicator;
    private ColouredStatusIndicator warningIndicator;
    private ColouredStatusIndicator infoIndicator;

    public EnvironmentSummaryPanel() {
        setLayout(new MigLayout("gap 2, ins 2", "[][][][][][][]", "[][]"));
        setOpaque(false);

        environmentName = new ResizingLabel((String) null);
        environmentName.setText("Pricing");
        environmentName.setForeground(Color.white);
        environmentName.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        environmentName.setBackground(Win8Colours.darkBlues2);
        environmentName.setOpaque(true);
        environmentName.setName("environmentNameResizingLabel");
        add(environmentName, "cell 0 0 1 2,alignx center,aligny center");

        severeIndicator = new ColouredStatusIndicator();
        severeIndicator.setForeground(Color.white);
        severeIndicator.setBackground(Win8Colours.reds2);
        severeIndicator.setName("severeIndicatorResizingLabel");
        add(severeIndicator, "cell 1 0");

        warningIndicator = new ColouredStatusIndicator();
        warningIndicator.setForeground(Color.black);
        warningIndicator.setBackground(Win8Colours.yellows2);
        warningIndicator.setName("warningIndicatorResizingLabel");
        add(warningIndicator, "cell 3 0");

        infoIndicator = new ColouredStatusIndicator();
        infoIndicator.setForeground(Color.white);
        infoIndicator.setBackground(Win8Colours.lightBlues2);
        infoIndicator.setName("infoIndicatorResizingLabel");
        add(infoIndicator, "cell 5 0");

    }

    public static void main(String[] args) {
        TestFrame.show(new EnvironmentSummaryPanel(), 800, 600);
    }

    public void bind(EnvironmentModel environmentModel, DashboardSelectionListener listener) {
        environmentName.setText(environmentModel.getName());

        EnvironmentSummaryModel environmentSummaryModel = environmentModel.getEnvironmentSummaryModel();

        severeIndicator.bind(environmentSummaryModel.getSevereLevelStatsModel(), environmentSummaryModel.getSevereLevelStatsPerSecondModel());
        warningIndicator.bind(environmentSummaryModel.getWarningLevelStatsModel(), environmentSummaryModel.getWarningLevelStatsPerSecondModel());
        infoIndicator.bind(environmentSummaryModel.getInfoLevelStatsModel(), environmentSummaryModel.getInfoLevelStatsPerSecondModel());

        severeIndicator.addMouseListener(new LabelMouseListener(listener, environmentModel, EnvironmentLevelStatsModel.Level.Severe));
        warningIndicator.addMouseListener(new LabelMouseListener(listener, environmentModel, EnvironmentLevelStatsModel.Level.Warning));
        infoIndicator.addMouseListener(new LabelMouseListener(listener, environmentModel, EnvironmentLevelStatsModel.Level.Info));

//        severeIndicatorPerSecond.addMouseListener(new LabelMouseListener(listener, environmentModel, EnvironmentLevelStatsModel.Level.Severe));
//        warningIndicatorPerSecond.addMouseListener(new LabelMouseListener(listener, environmentModel, EnvironmentLevelStatsModel.Level.Warning));
//        infoIndicatorPerSecond.addMouseListener(new LabelMouseListener(listener, environmentModel, EnvironmentLevelStatsModel.Level.Info));
    }

    private static class LabelMouseListener extends MouseAdapter {
        private EnvironmentModel environmentModel;
        private Level level;
        private DashboardSelectionListener listener;

        public LabelMouseListener(DashboardSelectionListener listener, EnvironmentModel environmentModel, Level severe) {
            this.listener = listener;
            this.environmentModel = environmentModel;
            this.level = severe;
        }

        @Override public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                listener.onSelected(environmentModel, level);
            }
        }
    }
}
