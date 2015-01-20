package com.logginghub.logging.frontend.charting.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.frontend.services.PatternManagementService;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.Visitor;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;

public class NewChartingModel extends Observable {

    private ObservableList<PatternModel> patternModels = createListProperty("patternModels", PatternModel.class);
    // TODO : think we want to get rid of streams
    private ObservableList<StreamConfiguration> streamModels = createListProperty("streamModels", StreamConfiguration.class);
    private ObservableList<AggregationConfiguration> aggregationModels = createListProperty("aggregationModels", AggregationConfiguration.class);
    private ObservableList<PageModel> pages = createListProperty("pages", PageModel.class);

    private static AtomicInteger nextInstance = new AtomicInteger(0);
    private int instanceNumber = nextInstance.getAndIncrement();

    public NewChartingModel() {

    }

    public ObservableList<PageModel> getPages() {
        return pages;
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public ObservableList<AggregationConfiguration> getAggregationModels() {
        return aggregationModels;
    }

    public ObservableList<PatternModel> getPatternModels() {
        return patternModels;
    }

    public ObservableList<StreamConfiguration> getStreamModels() {
        return streamModels;
    }

    public AbstractChartModel findChartForSeries(final ChartSeriesModel chartSeriesModel) {
        AbstractChartModel chart = null;

        ObservableList<PageModel> pages = getPages();
        for (PageModel pageModel : pages) {
            ObservableList<LineChartModel> chartingModels = pageModel.getChartingModels();
            for (LineChartModel lineChartModel : chartingModels) {
                ObservableList<ChartSeriesModel> matcherModels = lineChartModel.getMatcherModels();
                for (ChartSeriesModel otherChartSeriesModel : matcherModels) {
                    if (otherChartSeriesModel == chartSeriesModel) {
                        chart = lineChartModel;
                    }
                }
            }

            ObservableList<PieChartModel> pieChartModels = pageModel.getPieChartModels();
            for (PieChartModel pieChartModel : pieChartModels) {
                ObservableList<ChartSeriesModel> matcherModels = pieChartModel.getMatcherModels();
                for (ChartSeriesModel otherChartSeriesModel : matcherModels) {
                    if (otherChartSeriesModel == chartSeriesModel) {
                        chart = pieChartModel;
                    }
                }
            }
        }

        return chart;
    }

    public void visitCharts(Visitor<AbstractChartModel> visitor) {
        ObservableList<PageModel> pages = getPages();
        for (PageModel pageModel : pages) {
            ObservableList<LineChartModel> chartingModels = pageModel.getChartingModels();
            for (LineChartModel lineChartModel : chartingModels) {
                visitor.visit(lineChartModel);
            }

            ObservableList<PieChartModel> pieChartModels = pageModel.getPieChartModels();
            for (PieChartModel pieChartModel : pieChartModels) {
                visitor.visit(pieChartModel);
            }
        }
    }

    public String getPatternLabelForID(int patternID, int labelIndex) {

        String labelName = null;
        PatternModel patternForID = getPatternForID(patternID);
        if (patternForID != null) {

            ValueStripper2 stripper = new ValueStripper2();
            stripper.setPattern(patternForID.getPattern().get());

            labelName = stripper.getLabels().get(labelIndex);
        }

        return labelName;

    }

    public String getPatternNameForID(int patternID) {

        String patternName = null;
        PatternModel patternForID = getPatternForID(patternID);
        if (patternForID != null) {
            patternName = patternForID.getName().get();
        }

        return patternName;

    }

    public PatternModel getPatternForID(int patternID) {

        PatternModel pattern = null;
        ObservableList<PatternModel> patternModels = getPatternModels();
        for (PatternModel patternModel : patternModels) {
            if (patternModel.getPatternID().get() == patternID) {
                pattern = patternModel;
                break;
            }
        }

        return pattern;

    }

    public PatternManagementService getPatternService() {
        return new PatternManagementService() {

            @Override public ObservableList<PatternModel> listPatterns() {
                return null;

            }

            @Override public ObservableList<Pattern> getPatterns() {
                return null;

            }

            @Override public List<String> getPatternNames() {
                return null;

            }

            @Override public String getPatternName(int patternID) {
                String name = null;

                Pattern patternByID = getPatternByID(patternID);
                if (patternByID != null) {
                    name = patternByID.getName();
                }

                return name;

            }

            @Override public Pattern getPatternByID(int patternID) {

                Pattern pattern = null;
                ObservableList<PatternModel> patternModels = getPatternModels();
                for (PatternModel patternModel : patternModels) {
                    if (patternModel.getPatternID().get() == patternID) {

                        pattern = new Pattern();
                        pattern.setCleanup(patternModel.getCleanUp().get());
                        pattern.setDebug(patternModel.getDebug().get());
                        pattern.setName(patternModel.getName().get());
                        pattern.setPattern(patternModel.getPattern().get());
                        pattern.setPatternID(patternModel.getPatternID().get());

                        break;
                    }
                }

                return pattern;

            }

            @Override public String getLabelName(int patternID, int labelIndex) {
                String label = null;

                Pattern patternByID = getPatternByID(patternID);
                if (patternByID != null) {
                    ValueStripper2 stripper = new ValueStripper2(patternByID.getPattern());
                    label = stripper.getLabels().get(labelIndex);
                }

                return label;

            }

        };
    }

}
