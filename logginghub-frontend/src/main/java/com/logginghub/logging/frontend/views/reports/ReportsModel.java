package com.logginghub.logging.frontend.views.reports;

import com.logginghub.logging.messages.ReportDetails;
import com.logginghub.logging.messages.ReportExecuteResponse;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Created by james on 04/02/15.
 */
public class ReportsModel extends Observable {

    private ObservableList<ReportDetails> reportDetails = createListProperty("reportDetails", ReportDetails.class);
    private ObservableList<ReportExecuteResponse> responses = createListProperty("responses", ReportExecuteResponse.class);
    private ObservableProperty<Boolean> trimWhitespsace = createBooleanProperty("trimWhitespace", true);

    public ObservableList<ReportDetails> getReportDetails() {
        return reportDetails;
    }

    public ObservableList<ReportExecuteResponse> getResponses() {
        return responses;
    }

    public ObservableProperty<Boolean> getTrimWhitespsace() {
        return trimWhitespsace;
    }
}
