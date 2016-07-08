package com.logginghub.logging.frontend.charting.swing;

import com.logginghub.logging.frontend.charting.NewChartingController;
import com.logginghub.logging.frontend.charting.model.AggregationConfiguration;
import com.logginghub.logging.frontend.charting.model.BatchedArraryListTableModel;
import com.logginghub.logging.frontend.charting.model.ChartSeriesFilterModel;
import com.logginghub.logging.frontend.charting.model.ChartSeriesModel;
import com.logginghub.logging.frontend.charting.model.ExpressionConfiguration;
import com.logginghub.logging.frontend.charting.model.NewChartingModel;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.observable.ObservableList;
import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

@SuppressWarnings("Since15")
public class ChartSeriesModelEditor extends JPanel {

    private final static LabelIndexWrapper blankWrapper = new LabelIndexWrapper(-1, "");
    private static Logger logger = Logger.getLoggerFor(ChartSeriesModelEditor.class);
    // private JTextField patternTextField;
    // private JTextField legendTextField;
    // private JTextField labelTextField;
    private Binder2 binder;
    // private JTextField typeTextField;
    private JTextField intervalTextField;
    private JComboBox typeCombo;
    private JTextField eventPartsTextField;
    private JComboBox patternCombo;
    private DefaultComboBoxModel<NamedModelWrapper> existingDataSourcesComboModel;
    private DefaultComboBoxModel<PatternModelWrapper> patternComboModel;
    private DefaultComboBoxModel<String> groupByComboModel;
    private DefaultComboBoxModel<LabelIndexWrapper> tableEditorLabelComboModel;
    private JComboBox existingDataSources;
    private JComboBox labelCombo;
    private DefaultComboBoxModel<LabelIndexWrapper> labelComboModel;
    private NewChartingController controller;
    private JLabel lblNewLabel_6;
    private JComboBox groupByCombo;
    private JCheckBox generateEmptyTicks;
    private JTable table;
    private JScrollPane scrollPane;
    private JLabel lblFilters;
    private BatchedArraryListTableModel<ChartSeriesFilterModel> tableModel;
    private JButton btnNewButton;
    private ChartSeriesModel model;
    private JButton removeFilterButton;
    private JLabel lblNewLabel_7;
    private JComboBox tableEditorLabelComboBox;
    private NewChartingModel chartingModel;

    public ChartSeriesModelEditor() {

        setName("ChartSeriesModelEditor");
        setLayout(new MigLayout("", "[][grow]", "[][][][][][][][][][134.00,grow]"));

        setBackground(Color.white);
        setOpaque(true);

        existingDataSourcesComboModel = new DefaultComboBoxModel<NamedModelWrapper>();
        existingDataSources = new JComboBox(existingDataSourcesComboModel);
        existingDataSources.setName("Aggregations Combo");

        MutableInt row = new MutableInt(0);

        addRow(row, "Existing data sources", existingDataSources);

        patternComboModel = new DefaultComboBoxModel();
        patternCombo = new JComboBox(patternComboModel);
        patternCombo.setName("Pattern Combo");

        addRow(row, "Pattern", patternCombo);

        labelComboModel = new DefaultComboBoxModel();
        labelCombo = new JComboBox(labelComboModel);
        labelCombo.setName("Label Combo");
        labelCombo.setEditable(false);

        addRow(row, "Label", labelCombo);

        DefaultComboBoxModel dcm = new DefaultComboBoxModel();
        AggregationType[] values = AggregationType.values();
        for (AggregationType mode : values) {
            dcm.addElement(mode.toString());
        }

        typeCombo = new JComboBox(dcm);
        typeCombo.setName("Type Combo");

        addRow(row, "Type", typeCombo);

        intervalTextField = new JTextField();
        intervalTextField.setColumns(10);

        addRow(row, "Interval", intervalTextField);

        JLabel eventPartsExamples = new JLabel("For examples 'Source Host/Source IP/Source Application'");
        eventPartsExamples.setForeground(Color.GRAY);
        eventPartsExamples.setFont(new Font("Tahoma", Font.ITALIC, 11));
        add(eventPartsExamples, "cell 1 5,alignx trailing");

        JLabel lblNewLabel_5 = new JLabel("Event parts");
        add(lblNewLabel_5, "cell 0 6,alignx trailing");

        eventPartsTextField = new JTextField();
        add(eventPartsTextField, "cell 1 6,growx");
        eventPartsTextField.setColumns(10);

        lblNewLabel_6 = new JLabel("Group by");
        add(lblNewLabel_6, "cell 0 7,alignx trailing");

        groupByComboModel = new DefaultComboBoxModel();
        groupByCombo = new JComboBox(groupByComboModel);
        groupByCombo.setEditable(true);
        groupByCombo.setName("Group by");

        add(groupByCombo, "cell 1 7,growx");

        add(new JLabel("Generate empty ticks"), "cell 1 8,alignx trailing");
        generateEmptyTicks = new JCheckBox();
        generateEmptyTicks.setOpaque(false);
        add(generateEmptyTicks, "cell 1 8");

        btnNewButton = new JButton("Add filter");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addFilter();
            }
        });
        btnNewButton.setName("Add filter");
        add(btnNewButton, "flowx,cell 1 9");

        lblFilters = new JLabel("Filters");
        lblFilters.setHorizontalAlignment(SwingConstants.RIGHT);
        lblFilters.setVerticalAlignment(SwingConstants.TOP);
        add(lblFilters, "cell 0 10");

        scrollPane = new JScrollPane();

        tableModel = new BatchedArraryListTableModel<ChartSeriesFilterModel>() {

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Boolean.class;
                    case 1:
                        return String.class;
                    case 2:
                        return String.class;
                    case 3:
                        return String.class;

                }
                return String.class;

            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                ChartSeriesFilterModel item = getItemAtRow(rowIndex);
                switch (columnIndex) {
                    case 0:
                        item.getEnabled().set((Boolean) aValue);
                        break;
                    case 1:
                        LabelIndexWrapper wrapper = (LabelIndexWrapper) aValue;
                        item.getVariableIndex().set(wrapper.getIndex());
                        break;
                    case 2:
                        item.getWhitelist().set((String) aValue);
                        break;
                    case 3:
                        item.getBlacklist().set((String) aValue);
                        break;

                }
            }

            @Override
            public String[] getColumnNames() {
                return new String[]{"Enabled", "Variable", "Whitelist", "Blacklist"};

            }

            @Override
            public Object extractValue(ChartSeriesFilterModel item, int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return item.getEnabled().get();
                    case 1:
                        return getVariableLabel(item.getVariableIndex().get());
                    case 2:
                        return item.getWhitelist().get();
                    case 3:
                        return item.getBlacklist().get();

                }
                return "??";
            }

        };

        add(scrollPane, "cell 1 10,grow");

        table = new JTable(tableModel);
        table.setName("Filter table");
        scrollPane.setViewportView(table);

        removeFilterButton = new JButton("Remove filter");
        removeFilterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeSelectedFilter();
            }
        });
        add(removeFilterButton, "cell 1 9");

        lblNewLabel_7 = new JLabel("Hold the alt key whilst clicking cells to open in a larger editor window");
        lblNewLabel_7.setForeground(Color.GRAY);
        lblNewLabel_7.setFont(new Font("Tahoma", Font.ITALIC, 11));
        add(lblNewLabel_7, "cell 1 9");

        table.setRowHeight(24);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (table.getSelectedRow() == -1) {
                    removeFilterButton.setEnabled(false);
                } else {
                    removeFilterButton.setEnabled(true);
                }

            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && e.isAltDown()) {
                    JTable target = (JTable) e.getSource();
                    final int row = target.getSelectedRow();
                    final int column = target.getSelectedColumn();
                    final JTextArea ta = new JTextArea();
                    ta.setRows(20);
                    ta.setColumns(50);
                    String text = (String) table.getModel().getValueAt(row, column);
                    ta.setText(text);
                    ta.setLineWrap(true);
                    JScrollPane sp = new JScrollPane(ta);
                    sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

                    JPanel panel = new JPanel();
                    panel.add(sp);

                    EditorDialog dialog = new EditorDialog() {
                    };
                    dialog.getEventSource().addHandler(new EventHandler() {
                        @Override
                        public void onEvent(Event event) {
                            boolean ok = (Boolean) event.getPayload();
                            if (ok) {
                                table.getModel().setValueAt(ta.getText(), row, column);
                            }
                        }
                    });
                    dialog.setModal(true);
                    dialog.show("Edit text", panel, table);
                }

            }
        });

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getColumnModel().getColumn(0).setMaxWidth(55);

        TableColumn variableColumn = table.getColumnModel().getColumn(1);
        tableEditorLabelComboModel = new DefaultComboBoxModel();
        tableEditorLabelComboBox = new JComboBox(tableEditorLabelComboModel);
        variableColumn.setCellEditor(new DefaultCellEditor(tableEditorLabelComboBox));
    }

    private void addRow(MutableInt row, String label, JComponent component) {
        JLabel lblNewLabel = new JLabel(label);
        add(lblNewLabel, StringUtils.format("cell 0 {},alignx trailing", row.getValue()));
        add(component, StringUtils.format("cell 1 {},growx", row.getValue()));
        row.increment();
    }

    protected void addFilter() {
        if (model != null) {
            ChartSeriesFilterModel chartSeriesFilterModel = new ChartSeriesFilterModel();
            // Default to the first label
            chartSeriesFilterModel.getVariableIndex().set(0);
            model.getFilters().add(chartSeriesFilterModel);
        }
    }

    protected String getVariableLabel(int index) {

        String label = "";

        if (index != -1) {
            PatternModelWrapper patternWrapper = (PatternModelWrapper) patternCombo.getSelectedItem();
            if (patternWrapper != null) {
                ObservableList<PatternModel> patternModels = chartingModel.getPatternModels();
                for (PatternModel patternModel : patternModels) {
                    if (patternModel.getPatternID().get().equals(patternWrapper.patternModel.getPatternID().get())) {

                        ValueStripper2 stripper = new ValueStripper2();
                        stripper.setPattern(patternModel.getPattern().get());

                        label = stripper.getLabels().get(index);
                        break;
                    }
                }
            } else {
                label = "<no pattern selected>";
            }
        }

        return label;

    }

    protected void removeSelectedFilter() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            ChartSeriesFilterModel selected = tableModel.getItemAtRow(selectedRow);
            model.getFilters().remove(selected);
        }
    }

    public void bind(final NewChartingController controller, final ChartSeriesModel model, final NewChartingModel chartingModel) {
        this.controller = controller;
        this.model = model;
        this.chartingModel = chartingModel;
        binder = new Binder2();

        bindDataSourcesCombo(binder, model, chartingModel);

        int selectedPatternID = model.getPatternID().get();
        if (selectedPatternID == -1) {
            // Default to select the first pattern
            if (!chartingModel.getPatternModels().isEmpty()) {
                model.getPatternID().set(chartingModel.getPatternModels().get(0).getPatternID().get());
            }
        }

        ObservableList<PatternModel> patternModels = chartingModel.getPatternModels();
        for (PatternModel patternModel : patternModels) {
            PatternModelWrapper wrapper = new PatternModelWrapper(patternModel);
            this.patternComboModel.addElement(wrapper);

            if (wrapper.getPatternModel().getPatternID().get() == selectedPatternID) {
                patternCombo.setSelectedItem(wrapper);
            }
        }

        patternCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PatternModelWrapper wrapper = (PatternModelWrapper) patternComboModel.getSelectedItem();
                logger.info("Selected pattern '{}'", wrapper.getPatternModel().getName().get());
                model.getPatternID().set(wrapper.getPatternModel().getPatternID().get());
                setupLabels(model, chartingModel);
            }
        });

        setupLabels(model, chartingModel);

        // Custom binder for the label combo box
        final ItemListener labelComboListener = new ItemListener() {
            @SuppressWarnings("unchecked")
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    LabelIndexWrapper selectedItem = (LabelIndexWrapper) labelCombo.getSelectedItem();
                    model.getLabelIndex().set(selectedItem.getIndex());
                }
            }
        };

        labelCombo.addItemListener(labelComboListener);

        binder.addUnbinder(new Runnable() {
            @Override
            public void run() {
                labelCombo.removeItemListener(labelComboListener);
            }
        });

        // Custom binder for the group combo box
        final ItemListener groupByComboListener = new ItemListener() {
            @SuppressWarnings("unchecked")
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String groupBy = (String) groupByCombo.getSelectedItem();
                    model.getGroupBy().set(groupBy);
                }
            }
        };

        groupByCombo.addItemListener(groupByComboListener);

        binder.addUnbinder(new Runnable() {
            @Override
            public void run() {
                groupByCombo.removeItemListener(groupByComboListener);
            }
        });

        binder.bind(model.getType(), typeCombo);
        binder.bind(model.getInterval(), intervalTextField);
        binder.bind(model.getEventParts(), eventPartsTextField);
        binder.bind(model.getGenerateEmptyTicks(), generateEmptyTicks);

        // Set a default label
        int label = model.getLabelIndex().get();
        // TODO : should we do something with that label value?
        LabelIndexWrapper wrapper = labelComboModel.getElementAt(0);
        if (wrapper != null) {
            model.getLabelIndex().set(wrapper.getIndex());
        }

        // Bind the filter table by hand
        tableModel.clear();
        tableModel.bindTo(model.getFilters());
        tableModel.setAsync(false);

    }

    private void bindDataSourcesCombo(Binder2 binder, final ChartSeriesModel model, NewChartingModel chartingModel) {

        existingDataSourcesComboModel.removeAllElements();
        this.existingDataSourcesComboModel.addElement(AggregationConfigurationWrapper.empty);

        ObservableList<AggregationConfiguration> aggregationModels = chartingModel.getAggregationConfigurations();
        for (AggregationConfiguration aggregationModel : aggregationModels) {
            this.existingDataSourcesComboModel.addElement(new AggregationConfigurationWrapper(aggregationModel));
        }

        ObservableList<ExpressionConfiguration> expressionConfigurations = chartingModel.getExpressionConfigurations();
        for (ExpressionConfiguration expressionConfiguration : expressionConfigurations) {
            this.existingDataSourcesComboModel.addElement(new ExpressionConfigurationWrapper(expressionConfiguration));
        }

        final ItemListener listener = new ItemListener() {
            @SuppressWarnings("unchecked")
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    NamedModelWrapper wrapper = (NamedModelWrapper) existingDataSources.getSelectedItem();

                    if(wrapper == AggregationConfigurationWrapper.empty) {
                        model.getExistingAggregation().set(null);
                        model.getExistingExpression().set(null);
                    }else{
                        if(wrapper instanceof AggregationConfigurationWrapper) {
                            AggregationConfigurationWrapper aggregationConfigurationWrapper = (AggregationConfigurationWrapper) wrapper;
                            model.getExistingAggregation().set(aggregationConfigurationWrapper.getAggregationConfiguration());
                            model.getExistingExpression().set(null);
                        }else if(wrapper instanceof ExpressionConfigurationWrapper) {
                            ExpressionConfigurationWrapper expressionConfigurationWrapper = (ExpressionConfigurationWrapper) wrapper;
                            model.getExistingAggregation().set(null);
                            model.getExistingExpression().set(expressionConfigurationWrapper.getExpressionConfiguration());
                        }else {
                            throw new FormattedRuntimeException("Unsupported wrapper type '{}'", wrapper.getClass().getName());
                        }
                    }
                }
            }
        };

        existingDataSources.addItemListener(listener);

        binder.addUnbinder(new Runnable() {
            @Override
            public void run() {
                existingDataSources.removeItemListener(listener);
            }
        });

    }

    protected void setupLabels(ChartSeriesModel model, NewChartingModel chartingModel) {

        PatternModelWrapper wrapper = (PatternModelWrapper) patternComboModel.getSelectedItem();
        if (wrapper != null) {
            int patternID = wrapper.getPatternModel().getPatternID().get();

            PatternModel selected = null;
            ObservableList<PatternModel> patternModels = chartingModel.getPatternModels();
            for (PatternModel patternModel : patternModels) {

                if (patternModel.getPatternID().get() == patternID) {
                    selected = patternModel;
                    break;
                }
            }

            if (selected != null) {

                ValueStripper2 vs = controller.getValueStripper(patternID);
                if (vs != null) {

                    List<String> labels = vs.getLabels();
                    labelComboModel.removeAllElements();
                    groupByComboModel.removeAllElements();
                    tableEditorLabelComboModel.removeAllElements();

                    // Add a blank entry to the group by combo to indicate no group by
                    groupByComboModel.addElement("");

                    int selectedLabel = model.getLabelIndex().get();
                    String selectedGroupBy = model.getGroupBy().get();

                    LabelIndexWrapper selectedWrapper = null;

                    for (int i = 0; i < labels.size(); i++) {
                        String labelText = labels.get(i);

                        LabelIndexWrapper labelIndexWrapper = new LabelIndexWrapper(i, labelText);

                        labelComboModel.addElement(labelIndexWrapper);
                        groupByComboModel.addElement("{" + labelText + "}");
                        tableEditorLabelComboModel.addElement(labelIndexWrapper);

                        if (selectedLabel == i) {
                            selectedWrapper = labelIndexWrapper;
                        }
                    }

                    if (selectedWrapper != null) {
                        labelComboModel.setSelectedItem(selectedWrapper);
                    } else {
                        if (labels.size() > 0) {
                            labelComboModel.setSelectedItem(labelComboModel.getElementAt(0));
                        }
                    }

                    if (selectedGroupBy != null) {
                        groupByComboModel.setSelectedItem(selectedGroupBy);
                    }

                }
            }
        }
    }

    public void commitEditingChanges() {
        table.editCellAt(-1, -1);
    }

    public void unbind() {
        binder.unbind();
    }


    public interface NamedModelWrapper {
        String toString();
    }

    public static class AggregationConfigurationWrapper implements NamedModelWrapper {
        public static AggregationConfigurationWrapper empty = new AggregationConfigurationWrapper(null);
        private AggregationConfiguration aggregationConfiguration;

        public AggregationConfigurationWrapper(AggregationConfiguration aggregationConfiguration) {
            this.aggregationConfiguration = aggregationConfiguration;
        }

        public AggregationConfiguration getAggregationConfiguration() {
            return aggregationConfiguration;
        }

        @Override
        public String toString() {
            if(this == AggregationConfigurationWrapper.empty) {
                return "Create new aggregation...";
            }else {
                return aggregationConfiguration.getName().get();
            }
        }
    }

    public static class ExpressionConfigurationWrapper implements NamedModelWrapper {
        private ExpressionConfiguration expressionConfiguration;

        public ExpressionConfigurationWrapper(ExpressionConfiguration expressionConfiguration) {
            this.expressionConfiguration = expressionConfiguration;
        }

        public ExpressionConfiguration getExpressionConfiguration() {
            return expressionConfiguration;
        }

        @Override
        public String toString() {
            return expressionConfiguration.getName().get();
        }
    }


    public static class PatternModelWrapper {
        private PatternModel patternModel;

        public PatternModelWrapper(PatternModel model) {
            patternModel = model;
        }

        public PatternModel getPatternModel() {
            return patternModel;
        }

        @Override
        public String toString() {
            return patternModel.getName().get();
        }
    }

    public final static class LabelIndexWrapper {
        private int index;
        private String label;

        public LabelIndexWrapper(int index, String label) {
            this.index = index;
            this.label = label;
        }

        public int getIndex() {
            return index;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

}
