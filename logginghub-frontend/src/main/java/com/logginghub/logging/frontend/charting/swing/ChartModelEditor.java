package com.logginghub.logging.frontend.charting.swing;

import com.logginghub.logging.frontend.charting.NewChartingController;
import com.logginghub.logging.frontend.charting.model.LineChartModel;
import com.logginghub.logging.frontend.charting.model.PieChartModel;
import com.logginghub.logging.frontend.charting.model.TableChartModel;
import com.logginghub.utils.Convertor;
import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.observable.ObservableInteger;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ChartModelEditor extends JPanel {

    private JTextField yAxisLabel;
    private JTextField xAxisLabel;
    private JTextField chartTitle;
    private Binder2 binder;
    private JTextField dataPoints;
    private JTextField yAxisLock;
    private JTextField layout;
    private JTextField warningThreshold;
    private JTextField severeThreshold;
    private JCheckBox sideLegend;
    private JCheckBox showLegend;
    private JCheckBox forceZero;
    private JCheckBox smoothed;

    private JCheckBox showOther;
    private JTextField top;
    private JLabel lblResetAtTime;
    private JTextField resetAtTextField;

    public ChartModelEditor() {
        setBackground(Color.white);
        setLayout(new MigLayout("", "[46px][grow][]", "[14px][][][][][][][][][][][][][]"));

        JLabel lblNewLabel = new JLabel("Chart title");
        add(lblNewLabel, "cell 0 0,alignx trailing,aligny top");

        chartTitle = new JTextField();
        add(chartTitle, "cell 1 0,growx");
        chartTitle.setColumns(10);

        JLabel lblNewLabel_1 = new JLabel("X Axis Label");
        add(lblNewLabel_1, "cell 0 1,alignx trailing");

        xAxisLabel = new JTextField();
        add(xAxisLabel, "cell 1 1,growx");
        xAxisLabel.setColumns(10);

        JLabel lblNewLabel_2 = new JLabel("Y Axis Label");
        add(lblNewLabel_2, "cell 0 2,alignx trailing");

        yAxisLabel = new JTextField();
        add(yAxisLabel, "cell 1 2,growx");
        yAxisLabel.setColumns(10);

        JLabel lblNewLabel_3 = new JLabel("Data points");
        add(lblNewLabel_3, "cell 0 3,alignx trailing");

        dataPoints = new JTextField();
        add(dataPoints, "cell 1 3,growx");
        dataPoints.setColumns(10);

        JLabel lblNewLabel_4 = new JLabel("Y Axis Lock");
        add(lblNewLabel_4, "cell 0 4,alignx trailing");

        yAxisLock = new JTextField();
        add(yAxisLock, "cell 1 4,growx");
        yAxisLock.setColumns(10);

        JLabel lblNewLabel_5 = new JLabel("Layout");
        add(lblNewLabel_5, "cell 0 5,alignx trailing");

        layout = new JTextField();
        add(layout, "cell 1 5,growx");
        layout.setColumns(10);

        JLabel lblNewLabel_6 = new JLabel("Warning Threshold");
        add(lblNewLabel_6, "cell 0 6,alignx trailing");

        warningThreshold = new JTextField();
        add(warningThreshold, "cell 1 6,growx");
        warningThreshold.setColumns(10);

        JLabel lblNewLabel_7 = new JLabel("Severe Threshold");
        add(lblNewLabel_7, "cell 0 7,alignx trailing");

        severeThreshold = new JTextField();
        add(severeThreshold, "cell 1 7,growx");
        severeThreshold.setColumns(10);

        lblResetAtTime = new JLabel("Reset at time");
        add(lblResetAtTime, "cell 0 8,alignx trailing");

        resetAtTextField = new JTextField();
        resetAtTextField.setColumns(10);
        add(resetAtTextField, "cell 1 8,growx");

        JLabel lblNewLabel_8 = new JLabel("Side legend");
        add(lblNewLabel_8, "cell 0 9,alignx trailing");

        sideLegend = new JCheckBox("");
        sideLegend.setOpaque(false);
        add(sideLegend, "cell 1 9");

        JLabel lblNewLabel_9 = new JLabel("Show legend");
        add(lblNewLabel_9, "cell 0 10,alignx trailing");

        showLegend = new JCheckBox("");
        showLegend.setOpaque(false);
        add(showLegend, "cell 1 10");

        add(new JLabel("Y-axis show zero"), "cell 0 11,alignx trailing");
        forceZero = new JCheckBox("");
        forceZero.setOpaque(false);
        add(forceZero, "cell 1 11");

        add(new JLabel("Smoothed"), "cell 0 12,alignx trailing");
        smoothed = new JCheckBox("");
        smoothed.setOpaque(false);
        add(smoothed, "cell 1 12");
        
        add(new JLabel("Show <other> total"), "cell 0 13,alignx trailing");
        showOther = new JCheckBox("");
        showOther.setOpaque(false);
        add(showOther, "cell 1 13");

        add(new JLabel("Top filter"), "cell 0 14,alignx trailing");
        top = new JTextField("");
        add(top, "cell 1 14,growx");

    }

    public void bind(final NewChartingController controller, final LineChartModel lineChartModel) {
        // this.controller = controller;
        // this.lineChartModel = lineChartModel;

        binder = new Binder2();
        binder.bind(lineChartModel.getTitle(), chartTitle);
        binder.bind(lineChartModel.getxAxisLabel(), xAxisLabel);
        binder.bind(lineChartModel.getyAxisLabel(), yAxisLabel);
        binder.bind(lineChartModel.getDataPoints(), dataPoints);
        binder.bind(lineChartModel.getyAxisLock(), yAxisLock);
        binder.bind(lineChartModel.getLayout(), layout);
        binder.bind(lineChartModel.getWarningThreshold(), warningThreshold);
        binder.bind(lineChartModel.getSevereThreshold(), severeThreshold);
        binder.bind(lineChartModel.getSideLegend(), sideLegend);
        binder.bind(lineChartModel.getShowLegend(), showLegend);
        binder.bind(lineChartModel.getForceYZero(), forceZero);
        binder.bind(lineChartModel.getResetAt(), resetAtTextField);
        binder.bind(lineChartModel.getSmoothed(), smoothed);

        warningThreshold.setEnabled(true);
        severeThreshold.setEnabled(true);
        forceZero.setEnabled(true);
        xAxisLabel.setEnabled(true);
        yAxisLabel.setEnabled(true);
        dataPoints.setEnabled(true);
        yAxisLock.setEnabled(true);
        warningThreshold.setEnabled(true);
        severeThreshold.setEnabled(true);
        smoothed.setEnabled(true);

        top.setEnabled(false);
        showOther.setEnabled(false);
    }

    public void unbind() {
        binder.unbind();
    }

    public void bind(NewChartingController controller, PieChartModel model) {
        // this.controller = controller;
        // this.lineChartModel = lineChartModel;

        binder = new Binder2();
        binder.bind(model.getTitle(), chartTitle);
        binder.bind(model.getxAxisLabel(), xAxisLabel);
        binder.bind(model.getyAxisLabel(), yAxisLabel);
        binder.bind(model.getDataPoints(), dataPoints);
        binder.bind(model.getyAxisLock(), yAxisLock);
        binder.bind(model.getLayout(), layout);
        binder.bind(model.getWarningThreshold(), warningThreshold);
        binder.bind(model.getSevereThreshold(), severeThreshold);
        binder.bind(model.getSideLegend(), sideLegend);
        binder.bind(model.getShowLegend(), showLegend);
        binder.bind(model.getForceYZero(), forceZero);
        binder.bind(model.getResetAt(), resetAtTextField);

        binder.bind(model.getShowOtherSeries(), showOther);
        binder.bind(model.getTop(), top, new Convertor<String, ObservableInteger>() {
            @Override public String convert(ObservableInteger y) {
                int intValue = y.intValue();
                String converted;
                if (intValue == Integer.MAX_VALUE) {
                    converted = "";
                }
                else {
                    converted = Integer.toString(intValue);
                }
                return converted;
            }
        });

        warningThreshold.setEnabled(false);
        severeThreshold.setEnabled(false);
        forceZero.setEnabled(false);
        xAxisLabel.setEnabled(false);
        yAxisLabel.setEnabled(false);
        dataPoints.setEnabled(false);
        yAxisLock.setEnabled(false);
        warningThreshold.setEnabled(false);
        severeThreshold.setEnabled(false);
        smoothed.setEnabled(false);

        top.setEnabled(true);
        showOther.setEnabled(true);
    }

    public void bind(NewChartingController controller, TableChartModel model) {
        // this.controller = controller;
        // this.lineChartModel = lineChartModel;

        binder = new Binder2();
        binder.bind(model.getTitle(), chartTitle);
        binder.bind(model.getxAxisLabel(), xAxisLabel);
        binder.bind(model.getyAxisLabel(), yAxisLabel);
        binder.bind(model.getDataPoints(), dataPoints);
        binder.bind(model.getyAxisLock(), yAxisLock);
        binder.bind(model.getLayout(), layout);
        binder.bind(model.getWarningThreshold(), warningThreshold);
        binder.bind(model.getSevereThreshold(), severeThreshold);
        binder.bind(model.getSideLegend(), sideLegend);
        binder.bind(model.getShowLegend(), showLegend);
        binder.bind(model.getForceYZero(), forceZero);
        binder.bind(model.getResetAt(), resetAtTextField);

        binder.bind(model.getShowOtherSeries(), showOther);
        binder.bind(model.getTop(), top, new Convertor<String, ObservableInteger>() {
            @Override public String convert(ObservableInteger y) {
                int intValue = y.intValue();
                String converted;
                if (intValue == Integer.MAX_VALUE) {
                    converted = "";
                }
                else {
                    converted = Integer.toString(intValue);
                }
                return converted;
            }
        });

        warningThreshold.setEnabled(false);
        severeThreshold.setEnabled(false);
        forceZero.setEnabled(false);
        xAxisLabel.setEnabled(false);
        yAxisLabel.setEnabled(false);
        dataPoints.setEnabled(false);
        yAxisLock.setEnabled(false);
        warningThreshold.setEnabled(false);
        severeThreshold.setEnabled(false);
        smoothed.setEnabled(false);

        top.setEnabled(true);
        showOther.setEnabled(true);
    }

}
