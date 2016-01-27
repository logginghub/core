package com.logginghub.logging.frontend.views.environmentsummary;

import com.logginghub.logging.frontend.model.EnvironmentModel;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.swing.TestFrame;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private DashboardSelectionListener dashboardSelectionListener = null;
    private JLabel lblDashboard;

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        TestFrame.show(new DashboardPanel(), 800, 600);
    }

    public DashboardPanel() {
        setLayout(new MigLayout("ins 2, gap 2", "[grow]", "[][grow][grow]"));
        setBackground(Win8Colours.darkBlues1);

        setName("DashboardPanel");

        lblDashboard = new JLabel("Dashboard");
        lblDashboard.setFont(new Font("Segoe UI", Font.PLAIN, 26));
        lblDashboard.setForeground(Color.WHITE);
        add(lblDashboard, "cell 0 0");

        EnvironmentSummaryPanel environmentSummaryPanel = new EnvironmentSummaryPanel();
        add(environmentSummaryPanel, "cell 0 1,grow");
    }

    public void bind(ObservableList<EnvironmentModel> environments) {
        removeAll();
        add(lblDashboard, "wrap");
        environments.addListenerAndNotifyCurrent(new ObservableListListener<EnvironmentModel>() {
            @Override
            public void onAdded(EnvironmentModel environmentModel) {
                addEnvironment(environmentModel);
            }

            @Override
            public void onRemoved(EnvironmentModel environmentModel, int index) {
                removeEnvironmet(environmentModel);
            }

            @Override
            public void onCleared() {

            }
        });

    }

    public void setDashboardSelectionListener(DashboardSelectionListener dashboardSelectionListener) {
        this.dashboardSelectionListener = dashboardSelectionListener;
    }

    protected void addEnvironment(EnvironmentModel environmentModel) {
        EnvironmentSummaryPanel environmentSummaryPanel = new EnvironmentSummaryPanel();
        environmentSummaryPanel.setName("EnvironmentSummaryPanel-" + environmentModel.getName().get());
        environmentSummaryPanel.bind(environmentModel, dashboardSelectionListener);
        environmentSummaryPanel.setMaximumSize(new Dimension(10000,  400));
        add(environmentSummaryPanel, "wrap");
    }

    protected void removeEnvironmet(EnvironmentModel t) {}
}
