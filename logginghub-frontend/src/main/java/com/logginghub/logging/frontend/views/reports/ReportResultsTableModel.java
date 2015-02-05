package com.logginghub.logging.frontend.views.reports;

import com.logginghub.logging.messages.ReportExecuteResponse;
import com.logginghub.logging.messages.ReportExecuteResult;
import com.logginghub.utils.Result;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 05/02/15.
 */
public class ReportResultsTableModel extends AbstractTableModel {

    private List<ReportExecuteResponse> responses = new ArrayList<ReportExecuteResponse>();

    private String[] columnHeaders = new String[]{"Environment", "Host", "Address", "InstanceType", "InstanceIdentifier", "PID", "Result State", "Code", "Output"};

    public void add(ReportExecuteResponse reponse) {
        responses.add(reponse);
    }

    @Override public String getColumnName(int column) {
        return columnHeaders[column];
    }

    @Override public int getRowCount() {
        return responses.size();
    }

    @Override public int getColumnCount() {
        return columnHeaders.length;
    }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {

        final ReportExecuteResponse response = responses.get(rowIndex);

        Object value;
        switch (columnIndex) {
            case 0:
                value = response.getInstanceKey().getEnvironment();
                break;
            case 1:
                value = response.getInstanceKey().getHost();
                break;
            case 2:
                value = response.getInstanceKey().getAddress();
                break;
            case 3:
                value = response.getInstanceKey().getInstanceType();
                break;
            case 4:
                value = response.getInstanceKey().getInstanceIdentifier();
                break;
            case 5:
                value = response.getInstanceKey().getPid();
                break;
            case 6:
                value = response.getResult().getState();
                break;
            case 7: {
                final Result<ReportExecuteResult> result = response.getResult();
                if (result.isSuccessful()) {
                    value = result.getValue().getReturnCode();
                } else {
                    value = "";
                }
                break;
            }
            case 8: {
                final Result<ReportExecuteResult> result = response.getResult();
                if (result.isSuccessful()) {
                    value = result.getValue().getResult();
                } else {
                    value = "";
                }
                break;
            }
            default: {
                value = "?";
            }
        }

        return value;
    }

    public void clear() {
        responses.clear();
    }
}
