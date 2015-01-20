package com.logginghub.logging.frontend.regexbuilder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventMultiplexer;
import com.logginghub.logging.filters.MessageRegexFilter;
import com.logginghub.logging.frontend.analysis.TimeChunkingGenerator;
import com.logginghub.logging.frontend.analysis.XYScatterChartPanel;
import com.logginghub.logging.generators.RandomEventGenerator;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.swing.DelayedProcessingTextField;
import com.logginghub.utils.swing.MainFrame;

public class RegexBuilderPanel extends JPanel implements LogEventListener {

    private DelayedProcessingTextField regexTextField;
    private JTable table;
    private SimpleEventTableModel model;
    private MessageRegexFilter filter = new MessageRegexFilter(".*");
    private JPanel matchedGroupsPanel;
    private Map<Integer, JLabel> matchResults = new HashMap<Integer, JLabel>();
    private transient boolean paused = false;
    private XYScatterChartPanel scatterChartPanel;

    private LogEventMultiplexer eventMultiplexer = new LogEventMultiplexer();
    private int groupCount;
    private ButtonGroup buttonGroup;
    private ValueStripper2 currentValueStripper;

    public RegexBuilderPanel() {
        setLayout(new MigLayout("", "[left][grow][grow,center][grow]", "[][grow][grow][grow]"));

        JLabel regexLabel = new JLabel("Regex");
        add(regexLabel, "cell 0 0,alignx trailing");

        regexTextField = new DelayedProcessingTextField(500) {
            private static final long serialVersionUID = 1L;

            @Override protected void onTextFieldChanged(String text) {
                updateFilter(text);
            }
        };

        add(regexTextField, "flowx,cell 1 0 2 1,growx");
        regexTextField.setColumns(10);

        JPanel matchingEventsPanel = new JPanel();
        matchingEventsPanel.setBorder(new TitledBorder(null, "Matching events", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(matchingEventsPanel, "cell 0 1 3 2,grow");

        model = new SimpleEventTableModel();
        model.setFilter(filter);
        matchingEventsPanel.setLayout(new MigLayout("", "[grow,fill]", "[grow,fill]"));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOneTouchExpandable(true);
        matchingEventsPanel.add(splitPane, "cell 0 0,grow");
        table = new JTable(model);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setupColumns(table);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    if (row > 0 && row < model.getRowCount()) {
                        LogEvent logEvent = model.getRowAt(row);
                        String message = logEvent.getMessage();
                        regexTextField.setText(message);
                        updateFilter(message);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        splitPane.setLeftComponent(scrollPane);

        matchedGroupsPanel = new JPanel();
        splitPane.setRightComponent(matchedGroupsPanel);
        matchedGroupsPanel.setBorder(new TitledBorder(null, "Matched groups", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        matchedGroupsPanel.setLayout(new MigLayout("", "[]", "[]"));

        JPanel resultTypePanel = new JPanel();
        resultTypePanel.setBorder(new TitledBorder(null, "Result type", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(resultTypePanel, "cell 3 1 1 2,grow");
        resultTypePanel.setLayout(new MigLayout("", "[109px]", "[23px][][][]"));

        buttonGroup = new ButtonGroup();
        AggregationType[] values = AggregationType.values();
        for (AggregationType mode : values) {
            JRadioButton rdbtnNewRadioButton = new JRadioButton(mode.name());
            resultTypePanel.add(rdbtnNewRadioButton, "wrap");
            buttonGroup.add(rdbtnNewRadioButton);
        }

        buttonGroup.getElements().nextElement().setSelected(true);

        JPanel chartPanel = new JPanel();
        chartPanel.setBorder(new TitledBorder(null, "Chart", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(chartPanel, "cell 0 3 4 1,grow");
        chartPanel.setLayout(new MigLayout("", "[grow,fill]", "[grow,fill]"));

        scatterChartPanel = new XYScatterChartPanel();
        chartPanel.add(scatterChartPanel, "cell 0 0,alignx left,aligny top");
        scatterChartPanel.setLayout(new MigLayout("", "[grow,fill]", "[grow,fill]"));

        JButton btnNewButton = new JButton("Pause/resume updates");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramActionEvent) {
                paused = !paused;
            }
        });
        add(btnNewButton, "cell 3 0");
    }

    protected void updateFilter(String text) {
        if (text.trim().isEmpty()) {
            text = ".*";
        }
        resetMatchedGroups();
        filter.setMessageMatchesRegex(text);
        model.updateFilter();
    }

    private void resetMatchedGroups() {
        matchedGroupsPanel.removeAll();
        matchResults.clear();
    }

    private void setupColumns(JTable table) {
        table.getColumnModel().getColumn(SimpleEventTableModel.Column.SourceHost.ordinal()).setPreferredWidth(150);
        table.getColumnModel().getColumn(SimpleEventTableModel.Column.SourceApplication.ordinal()).setPreferredWidth(150);
        table.getColumnModel().getColumn(SimpleEventTableModel.Column.Message.ordinal()).setPreferredWidth(1500);
    }

    private void setRegex(String string) {
        regexTextField.setText(string);
        updateFilter(string);
    }

    @Override public void onNewLogEvent(LogEvent event) {
        if (!paused) {
            if (filter.passes(event)) {
                model.onNewLogEvent(event);

                Matcher lastMatcher = filter.getLastMatcher();

                int groupCount = lastMatcher.groupCount() + 1;
                if (groupCount > 0) {
                    ensureEnoughControlsInMatchesGroupsPanel(groupCount);
                    for (int i = 0; i < groupCount; i++) {
                        String matchResult = lastMatcher.group(i);
                        matchResults.get(i).setText(matchResult);
                    }
                }
            }
        }

        eventMultiplexer.onNewLogEvent(event);
    }

    private void ensureEnoughControlsInMatchesGroupsPanel(int groupCount) {
        if (matchResults.size() != groupCount) {
            resetMatchedGroups();
            this.groupCount = groupCount;
            for (int i = 0; i < groupCount; i++) {
                JLabel groupLabel = new JLabel("Group " + i);
                addChartingClickListener(i, groupLabel);
                matchedGroupsPanel.add(groupLabel);
                JLabel label = new JLabel("");
                matchedGroupsPanel.add(label, "wrap");
                matchResults.put(i, label);
            }
        }
    }

    private void addChartingClickListener(final int groupID, JLabel groupLabel) {
        groupLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent paramMouseEvent) {
                startCharting(groupID);
            }
        });
    }

    protected void startCharting(int groupID) {
        scatterChartPanel.getChart().reset();

        if (currentValueStripper == null) {
            eventMultiplexer.removeLogEventListener(currentValueStripper);
        }

        currentValueStripper = new ValueStripper2();
        currentValueStripper.setMatcherIndices(groupID);
        currentValueStripper.setRegex(regexTextField.getText());
        List<String> labels = new ArrayList<String>();
        for (int i = 0; i < groupCount; i++) {
            labels.add("Group" + i);
        }
        currentValueStripper.setLabels(labels);

        JRadioButton selection = getSelection(buttonGroup);
        String mode = selection.getText();
        AggregationType timeChunkerMode = AggregationType.valueOf(mode);

        TimeChunkingGenerator chunkingGenerator = new TimeChunkingGenerator();
        chunkingGenerator.setPublishingModes(timeChunkerMode);
        currentValueStripper.addResultListener(chunkingGenerator);

        chunkingGenerator.addChunkedResultHandler(scatterChartPanel.getChart());
        eventMultiplexer.addLogEventListener(currentValueStripper);
    }

    public static JRadioButton getSelection(ButtonGroup group) {
        for (Enumeration<AbstractButton> e = group.getElements(); e.hasMoreElements();) {
            JRadioButton b = (JRadioButton) e.nextElement();
            if (b.getModel() == group.getSelection()) {
                return b;
            }
        }
        return null;
    }

    public static void main(String[] args) {

        MainFrame frame = new MainFrame("RegexBuilderPanel");
        RegexBuilderPanel regexBuilderPanel = new RegexBuilderPanel();
        frame.setSize(0.5f);
        frame.getContentPane().add(regexBuilderPanel);
        frame.setVisible(true);

        RandomEventGenerator randomEventGenerator = new RandomEventGenerator();
        randomEventGenerator.start(10);
        randomEventGenerator.addLogEventListener(regexBuilderPanel);

        regexBuilderPanel.setRegex("GetOrder succeeded in (.*) ms : encoded order size was (.*) bytes \\((.*) compressed\\)");
    }
}
