package com.logginghub.logging.frontend.model;

import com.logginghub.logging.frontend.charting.model.NewChartingModel;

public class LoggingFrontendModel extends ObservableModel {

    private NewChartingModel chartingModel = new NewChartingModel();
    private ObservableList<EnvironmentModel> environments = new ObservableArrayList<EnvironmentModel>();
    private boolean popoutCharting = false;
    
    private RowFormatModel selectedRowFormat = new RowFormatModel();
    private boolean showHubClearEvents;

    public boolean isShowHubClearEvents() {
        return showHubClearEvents;
    }

    public void setShowHubClearEvents(boolean showHubClearEvents) {
        this.showHubClearEvents = showHubClearEvents;
    }

    public enum Fields implements FieldEnumeration {
        Title, ShowDashboard;
    }
    
    public LoggingFrontendModel() {
        set(Fields.Title, "no title");
        set(Fields.ShowDashboard, false);
    }
    
    public NewChartingModel getChartingModel() {
        return chartingModel;
    }
    
    public void setSelectedRowFormat(RowFormatModel selectedRowFormat) {
        this.selectedRowFormat = selectedRowFormat;
    }
    
    public RowFormatModel getSelectedRowFormat() {
        return selectedRowFormat;
    }
    
    public boolean isPopoutCharting() {
        return popoutCharting;
    }
    
    public void setPopoutCharting(boolean popoutCharting) {
        this.popoutCharting = popoutCharting;
    }
    
    public ObservableList<EnvironmentModel> getEnvironments() {
        return environments;
    }
    
    public void setEnvironments(ObservableList<EnvironmentModel> environments) {
        this.environments = environments;
    }

    public void updateEachSecond() {
        for (EnvironmentModel model : environments) {
            model.updateEachSecond();            
        }
    }

    public void setShowDashboard(boolean showDashboard) {
        set(Fields.ShowDashboard, showDashboard);
    }    
    
    public boolean getShowDashboard() {
        return getBoolean(Fields.ShowDashboard);
    }

    public EnvironmentModel getEnvironment(final String string) {
        return environments.getFirst(new Matcher<EnvironmentModel>() {
            public boolean matches(EnvironmentModel model) {
                return model.getName().equals(string);
            }
        });
         
    }
}
