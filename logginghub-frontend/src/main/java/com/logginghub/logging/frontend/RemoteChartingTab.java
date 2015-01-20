package com.logginghub.logging.frontend;

import com.logginghub.logging.frontend.charting.NewChartingView;
import com.logginghub.logging.frontend.configuration.RemoteChartConfiguration;

public class RemoteChartingTab extends NewChartingView {

    private RemoteChartConfiguration remoteChartConfiguration;

    public void configure(RemoteChartConfiguration remoteChartConfiguration) {
        this.remoteChartConfiguration = remoteChartConfiguration;
    }
    
    public RemoteChartConfiguration getRemoteChartConfiguration() {
        return remoteChartConfiguration;
    }
    

}
