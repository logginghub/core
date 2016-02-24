package com.logginghub.logging.frontend.model;

import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

public class LoggingFrontendModel extends Observable {

    private NewChartingModel chartingModel = new NewChartingModel();
    private ObservableList<EnvironmentModel> environments = createListProperty("environments", EnvironmentModel.class);

    private ObservableProperty<String> title = createStringProperty("title", "no title");
    private ObservableProperty<Boolean> showDashboard = createBooleanProperty("showDashboard", false);

    private ObservableInteger localRPCPort = createIntProperty("localRPCPort", LoggingFrontendConfiguration.DONT_USE_LOCAL_RPC);
    private ObservableProperty<Boolean> popoutCharting = createBooleanProperty("popoutCharting", false);
    private ObservableProperty<Boolean> showHubClearEvents = createBooleanProperty("showHubClearEvents", false);

    private ObservableProperty<RowFormatModel> selectedRowFormat = createProperty("selectedRowFormat", RowFormatModel.class, new RowFormatModel());

    public LoggingFrontendModel() {

    }

    public ObservableInteger getLocalRPCPort() {
        return localRPCPort;
    }

    public ObservableProperty<Boolean> getPopoutCharting() {
        return popoutCharting;
    }

    public ObservableProperty<Boolean> getShowHubClearEvents() {
        return showHubClearEvents;
    }

    public NewChartingModel getChartingModel() {
        return chartingModel;
    }


    public ObservableProperty<RowFormatModel> getSelectedRowFormat() {
        return selectedRowFormat;
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

    public ObservableProperty<Boolean> getShowDashboard() {
        return showDashboard;
    }

    public ObservableProperty<String> getTitle() {
        return title;
    }

    public EnvironmentModel getEnvironment(final String string) {
        EnvironmentModel found = null;
        for (EnvironmentModel environment : environments) {
            if(environment.getName().get().equals(string)) {
                found = environment;
                break;
            }
        }

        return found;

    }
}
