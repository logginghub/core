package com.logginghub.logging.frontend.charting.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.analysis.ChunkedResult;
import com.logginghub.logging.frontend.charting.LineChartView;
import com.logginghub.logging.frontend.charting.NewAggregatorSplitter;
import com.logginghub.logging.frontend.charting.NewChartingController;
import com.logginghub.logging.frontend.charting.model.AggregationConfiguration;
import com.logginghub.logging.frontend.charting.model.LineChartModel;
import com.logginghub.logging.frontend.charting.model.StreamBuilder;
import com.logginghub.logging.frontend.charting.model.StreamConfiguration;
import com.logginghub.logging.frontend.charting.model.StreamListener;
import com.logginghub.logging.frontend.charting.model.StreamResultItem;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.observable.ObservablePropertyListener;

public class VisualiseWizardPanel extends JPanel implements LogEventListener, StreamListener<StreamResultItem> {
    private static final long serialVersionUID = 1L;

    private StreamDefinitionView streamDefinitionView = new StreamDefinitionView();
    private AggregationPanel aggregationPanel = new AggregationPanel();

    private LineChartView lineChartPanel = new LineChartView();
    private AggregatedDataView aggregatedDataView = new AggregatedDataView();

    private NewChartingController controller;
    private StreamBuilder currentStreamBuilder;
    private JButton addButton = new JButton("Add");

    private AggregationConfiguration aggregationConfiguration = new AggregationConfiguration();

    public VisualiseWizardPanel() {
        setLayout(new MigLayout("fill", "[fill, grow]", "[fill]"));

        LineChartModel chartModel = new LineChartModel();
        lineChartPanel.bind(controller, chartModel);

        add(streamDefinitionView, "cell 0 0");
        add(aggregationPanel, "cell 0 1");
        add(addButton, "cell 0 2");
        add(aggregatedDataView, "cell 0 3");
        add(lineChartPanel, "cell 0 4");
    }

    public void bind(final NewChartingController controller) {
        this.controller = controller;
        streamDefinitionView.bind(controller.getModel().getStreamModels());

        streamDefinitionView.getSelectedStream().addListener(new ObservablePropertyListener<StreamConfiguration>() {
            @Override public void onPropertyChanged(StreamConfiguration oldValue, StreamConfiguration newValue) {

                StreamBuilder streamBuilderForModel = controller.getStreamBuilderForModel(newValue);

                if (currentStreamBuilder != null) {
                    currentStreamBuilder.getOutput().removeListener(VisualiseWizardPanel.this);
                }

                currentStreamBuilder = streamBuilderForModel;
                streamBuilderForModel.getOutput().addListener(VisualiseWizardPanel.this);
            }
        });

        aggregationPanel.bind(aggregationConfiguration);

        addButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {

                // Wire it up to the selected stream
                StreamConfiguration streamConfiguration = streamDefinitionView.getSelectedStream().get();
                String streamID = streamConfiguration.getStreamID().get();
                aggregationConfiguration.getStreamID().set(streamID);

                // Switch to a new aggregation configuration and add the old one to the model
                aggregationPanel.unbind(aggregationConfiguration);
                NewAggregatorSplitter aggregator = controller.addAggregation(aggregationConfiguration);
                aggregationConfiguration = new AggregationConfiguration();
                aggregationPanel.bind(aggregationConfiguration);

                // Show the results on our embedded chart
                aggregator.getOutputStream().addListener(new StreamListener<ChunkedResult>() {
                    @Override public void onNewItem(ChunkedResult t) {
                        lineChartPanel.getChart().onNewChunkedResult(t);
                    }
                });
            }
        });
    }

    public static VisualiseWizardPanel showInDialog(JFrame owner, NewChartingController controller) {
        JDialog dialog = new JDialog(owner);

        VisualiseWizardPanel wizardPanel = new VisualiseWizardPanel();
        dialog.getContentPane().setLayout(new MigLayout("fill", "[fill, grow]", "[fill, grow]"));
        dialog.getContentPane().add(wizardPanel);
        // TODO : chose a good size?
        // dialog.pack();
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        //
        // final PatternsModel model = new PatternsModel();
        // final File config = new File("patterns.xml");
        // if (config.exists()) {
        // model.fromXml(new Xml(FileUtils.read(config)).getRoot());
        // }

        // final StreamDefinitionModels streamDefinitionModels = new StreamDefinitionModels();
        // // TODO : all these should go in one file
        // final File streamsConfig = new File("streams.xml");
        // if (streamsConfig.exists()) {
        // XmlEntry root2 = new Xml(FileUtils.read(streamsConfig)).getRoot();
        // streamDefinitionModels.fromXml(root2);
        // }
        //
        wizardPanel.bind(controller);

        return wizardPanel;
    }

    @Override public void onNewLogEvent(LogEvent event) {

    }

    @Override public void onNewItem(StreamResultItem t) {
//        System.out.println(t);
    }

}
