package com.logginghub.logging.frontend.charting.swing;

import com.logginghub.logging.frontend.charting.NewChartingController;
import com.logginghub.logging.frontend.charting.model.ExpressionConfiguration;
import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Binder2;
import net.miginfocom.swing.MigLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;

@SuppressWarnings("Since15")
/**
 * Provides a bindable editor control for {@link com.logginghub.logging.frontend.charting.model.ExpressionConfiguration}
 */
public class ExpressionConfigurationEditor extends JPanel {

    private static Logger logger = Logger.getLoggerFor(ExpressionConfigurationEditor.class);
    private final JTextField nameField = new JTextField();
    private final JTextField expressionField = new JTextField();
    private final JTextField groupByField = new JTextField();
    private Binder2 binder;
    private NewChartingModel chartingModel;
    private NewChartingController controller;
    private ExpressionConfiguration model;

    public ExpressionConfigurationEditor() {

        setName("ExpressionConfigurationEditor");
        setLayout(new MigLayout("", "[100][200, grow]", "[][][]"));

        setBackground(Color.white);
        setOpaque(true);

        MutableInt row = new MutableInt(0);
        addRow(row, "Expression name", nameField);
        addRow(row, "Expression", expressionField);
        addRow(row, "Group by", groupByField);

    }

    private void addRow(MutableInt row, String label, JComponent component) {
        JLabel lblNewLabel = new JLabel(label);
        add(lblNewLabel, StringUtils.format("cell 0 {},alignx trailing", row.getValue()));
        add(component, StringUtils.format("cell 1 {},growx", row.getValue()));
        component.setName(label);
        row.increment();
    }

    public void bind(final NewChartingController controller, final ExpressionConfiguration model, final NewChartingModel chartingModel) {
        this.controller = controller;
        this.model = model;
        this.chartingModel = chartingModel;
        binder = new Binder2();

        binder.bind(model.getName(), nameField);
        binder.bind(model.getExpression(), expressionField);
        binder.bind(model.getGroupBy(), groupByField);

    }


    public void unbind() {
        binder.unbind();
    }

    public void commitEditingChanges() {

    }

}
