package com.logginghub.logging.frontend.aggregateddataview;

import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableColumn;

import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.frontend.services.PatternManagementService;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.swingutils.table.ExtensibleTable;
import com.logginghub.swingutils.table.ExtensibleTableModel;
import com.logginghub.utils.StringUtils;

public class AggregationsViewTable extends ExtensibleTable<Aggregation> {

    public final static class PatternComboWrapper {
        public Pattern pattern;
        public PatternComboWrapper(Pattern pattern) {
            this.pattern = pattern;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        @Override public String toString() {
            return StringUtils.format("[{}] {}", pattern.getPatternID(), pattern.getName());             
        }
    }
    
    public AggregationsViewTable(ExtensibleTableModel<Aggregation> model, PatternManagementService patternService) {
        super(model);

        setupPatternEditorCombo(patternService);     
        setupTypeEditorCombo();
        
        setRowHeight(26);
    }

    
    private void setupPatternEditorCombo(PatternManagementService patternService) {
        TableColumn column = getColumnModel().getColumn(AggregationsViewTableModel.COLUMN_PATTERN_ID);
        DefaultComboBoxModel<PatternComboWrapper> model = new DefaultComboBoxModel<PatternComboWrapper>();

        List<Pattern> patterns = patternService.getPatterns();
        for (Pattern pattern : patterns) {
            model.addElement(new PatternComboWrapper(pattern));
        }

        JComboBox<PatternComboWrapper> comboBox = new JComboBox<PatternComboWrapper>(model);
        column.setCellEditor(new DefaultCellEditor(comboBox));
    }
    
    private void setupTypeEditorCombo() {
        TableColumn variableColumn = getColumnModel().getColumn(AggregationsViewTableModel.COLUMN_TYPE);
        DefaultComboBoxModel<String> aggregationTypeModel = new DefaultComboBoxModel<String>();

        AggregationType[] values = AggregationType.values();
        for (AggregationType aggregationType : values) {
            aggregationTypeModel.addElement(aggregationType.name());
        }

        JComboBox<String> aggregationTypeComboBox = new JComboBox<String>(aggregationTypeModel);
        variableColumn.setCellEditor(new DefaultCellEditor(aggregationTypeComboBox));
    }

}
