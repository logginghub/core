package com.logginghub.logging.frontend.views.environmentsummary;

import com.logginghub.logging.frontend.model.EnvironmentLevelStatsModel;
import com.logginghub.logging.frontend.model.EnvironmentModel;

public interface DashboardSelectionListener {
    void onSelected(EnvironmentModel model, EnvironmentLevelStatsModel.Level level);
}
