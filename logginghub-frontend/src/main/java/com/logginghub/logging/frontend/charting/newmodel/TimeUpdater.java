package com.logginghub.logging.frontend.charting.newmodel;

import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.observable.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by james on 31/01/15.
 */
public class TimeUpdater implements Asynchronous {
    private final ChartDetailsModel chartDetailsModel;
    private WorkerThread workerThread;
    private TimeProvider timeProvider = new SystemTimeProvider();
    private List<XYChartDataModel> dataModels = new ArrayList<XYChartDataModel>();

    public TimeUpdater(ChartDetailsModel chartDetailsModel, XYChartDataModel... models) {
        this.chartDetailsModel = chartDetailsModel;
        for (XYChartDataModel model : models) {
            dataModels.add(model);
        }
    }

    public void start() {
        workerThread = WorkerThread.every("TimeUpdater", 50, TimeUnit.MILLISECONDS, new Runnable() {
            @Override public void run() {

                long endTime = timeProvider.getTime();
                long startTime = endTime - chartDetailsModel.getDuration().get();

                chartDetailsModel.getEndTime().set(endTime);
                chartDetailsModel.getStartTime().set(startTime);

                for (XYChartDataModel dataModel : dataModels) {
                    ObservableList<XYSeriesModel> series = dataModel.getSeries();
                    for (XYSeriesModel seriesModel : series) {
                        ObservableList<XYValue> values = seriesModel.getValues();
                        if (!values.isEmpty()) {
                            boolean timesOk = false;
                            while (!timesOk) {
                                // Check the last item
                                int lastIndex = values.size() - 1;
                                XYValue xyValue = values.get(lastIndex);
                                if (xyValue.x < startTime) {
                                    values.remove(lastIndex);
                                } else {
                                    timesOk = true;
                                }
                            }
                        }
                    }
                }

            }
        });
    }

    @Override public void stop() {
        if (workerThread != null) {
            workerThread.stop();
            workerThread = null;
        }
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }
}
