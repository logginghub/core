package com.logginghub.logging.frontend.charting.swing;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.charting.NewChartingController;
import com.logginghub.logging.frontend.charting.model.PatternisedDataModel;
import com.logginghub.logging.frontend.charting.model.PatternisedDataSeriesModel;
import com.logginghub.logging.frontend.charting.swing.fancypattern.FancyPatternModelEditor;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.Stream;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Binder;
import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ComprehensivePatternModelEditor extends JPanel implements LogEventListener {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLoggerFor(ComprehensivePatternModelEditor.class);
    private SimpleEventMessageTable simpleEventMessageTable;

    private FancyPatternModelEditor patternEditor = new FancyPatternModelEditor();
    private final ValueStripper2 stripper2 = new ValueStripper2();
    private JButton addUpdateButton = new JButton("Add pattern");
    private JButton pauseButton = new JButton("Pause events");
    private PatternModel newPattern = new PatternModel();
    private PatternExtractTable patternExtractTable = new PatternExtractTable();
    private volatile boolean paused = false;
    private boolean editingExisting = false;
    private final JPanel panel = new JPanel();
    private final JPanel panel_1 = new JPanel();

    private Stream<Boolean> doneStream = new Stream<Boolean>();

    private JTextField filter;

    public ComprehensivePatternModelEditor() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new MigLayout("fill, ins 2", "[grow,fill]", "[400px][300px][300px][bottom]"));
        setName("ComprehensivePatternModelEditor");

        JScrollPane mainScrollPane = new JScrollPane(mainPanel);

        final PatternisedDataSeriesModel extractedData = new PatternisedDataSeriesModel();

        stripper2.addResultListener(new ValueStripper2.ValueStripper2ResultListener() {
            @Override
            public void onNewResult(String label, boolean isNumeric, String value, LogEvent entry) {
            }

            @Override
            public void onNewPatternisedResult(String[] labels, String[] patternVariables, boolean[] isNumeric, LogEvent entry) {
                PatternisedDataModel data = new PatternisedDataModel(entry, patternVariables, patternVariables);
                extractedData.getPatternised().add(data);
            }
        });
        patternEditor.setBorder(new TitledBorder(null, "2. Configure the pattern", TitledBorder.LEADING, TitledBorder.TOP, null, null));

        patternEditor.bind(newPattern);
        panel.setBorder(new TitledBorder(null, "1. Choose a log event you want to create a pattern from:", TitledBorder.LEADING,
                TitledBorder.TOP, null, null));

        mainPanel.add(panel, "cell 0 0,grow");
        panel.setLayout(new MigLayout("fill, ins 2", "[grow,fill]", "[top][grow,fill][bottom]"));
        simpleEventMessageTable = new SimpleEventMessageTable();
        // simpleEventMessageTable.set

        JScrollPane eventTableScroller = new JScrollPane(simpleEventMessageTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel.add(eventTableScroller, "cell 0 1,alignx left,aligny top");
        panel.add(pauseButton, "cell 0 2");

        filter = new JTextField();
        filter.setName("Filter");
        // TODO : add a listener and feed it into the filter on the simple event
        // message table
        ObservableProperty<String> filterText = new ObservableProperty<String>("");
        Binder.bind(filterText, filter);

        filterText.addListener(new ObservablePropertyListener<String>() {
            public void onPropertyChanged(String oldValue, String newValue) {
                simpleEventMessageTable.setFilter(newValue);
            }
        });

        panel.add(new JLabel("Filter:"), "cell 0 0");
        panel.add(filter, "cell 0 0");

        simpleEventMessageTable.getLogEventSelectedEvent().addHandler(new EventHandler() {
            @Override
            public void onEvent(Event event) {
                List<LogEvent> events = event.getPayload();
                if (events.size() > 0) {

                    boolean isOk = false;

                    if (patternEditor.isEdited()) {
                        if (JOptionPane.showConfirmDialog(ComprehensivePatternModelEditor.this,
                                "You have a pattern currently being edited, do you want to lose your changes?") == JOptionPane.OK_OPTION) {
                            isOk = true;
                        }
                    } else {
                        isOk = true;
                    }

                    if (isOk) {
                        LogEvent logEvent = events.get(0);
                        setSelectedEvent(logEvent);
                    }
                }
            }

        });
        mainPanel.add(patternEditor, "cell 0 1");

        final ObservablePropertyListener<String> patternUpdateListener = new ObservablePropertyListener<String>() {
            @Override
            public void onPropertyChanged(String oldValue, final String newValue) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        logger.fine("Updating value stripper with new pattern '{}'", newValue);
                        stripper2.setPattern(newValue);
                        List<String> labels = stripper2.getLabels();
                        extractedData.getPatternised().clearQuietly();
                        patternExtractTable.setLabels(labels);
                    }
                });
            }
        };
        newPattern.getPattern().addListenerAndNotifyCurrent(patternUpdateListener);

        patternExtractTable.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
                "3. View the extracted data to make sure it looks good", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        patternExtractTable.bind(extractedData);

        mainPanel.add(patternExtractTable, "cell 0 2");
        panel_1.setBorder(new TitledBorder(null, "4. Push the button!", TitledBorder.LEADING, TitledBorder.TOP, null, null));

        mainPanel.add(panel_1, "flowx,cell 0 3");
        panel_1.setLayout(new BorderLayout(0, 0));
        panel_1.add(addUpdateButton);
        
        addUpdateButton.setName("Add pattern button");

        add(mainScrollPane, BorderLayout.CENTER);
    }

    public void bind(final PatternModel existingModel, final NewChartingController controller) {

        if (existingModel != null) {
            patternEditor.bind(existingModel);
            addUpdateButton.setText("Update pattern");
        }

        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (addUpdateButton.getText().startsWith("Update")) {
                    logger.fine("Updating pattern '{}'", newPattern);
                    doneStream.send(true);
                } else {
                    logger.fine("Adding pattern '{}'", newPattern);
                    controller.addPattern(newPattern);
                    patternEditor.unbind();
                    newPattern = new PatternModel();
                    patternEditor.bind(newPattern);
                    doneStream.send(true);
                }
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paused = !paused;
            }
        });

    }

//    protected static void savePatterns(PatternsModel model, File file) {
//
//        XMLObjectWriter writer = new XMLObjectWriter();
//        String write = writer.write("patternModel", model);
//        FileUtils.write(write, file);
//
//    }S

    @Override
    public void onNewLogEvent(LogEvent event) {
        if (!paused) {
            simpleEventMessageTable.onNewLogEvent(event);
        }
        stripper2.onNewLogEvent(event);
    }

    public void setSelectedEvent(LogEvent logEvent) {
        patternEditor.getPatternToEdit().set(logEvent.getMessage());
        newPattern.getPattern().set(logEvent.getMessage());
        patternEditor.setEdited(false);
        patternEditor.setCaretAtStart();
    }

    public Stream<Boolean> getDoneStream() {
        return doneStream;
    }
}
