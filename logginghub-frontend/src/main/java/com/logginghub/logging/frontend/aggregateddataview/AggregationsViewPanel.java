package com.logginghub.logging.frontend.aggregateddataview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.exceptions.ConnectorException;
import com.logginghub.logging.frontend.services.PatternManagementService;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Result;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.swing.TestFrame;

public class AggregationsViewPanel extends JPanel implements Asynchronous, Destination<Aggregation> {

    private AggregationsViewTableModel model;
    private AggregationsViewTable table;

    private WorkerThread timer;

    public AggregationsViewPanel(PatternManagementService patternService) {
        model = new AggregationsViewTableModel(patternService);
        table = new AggregationsViewTable(model, patternService);
        
        setLayout(new MigLayout("", "[grow]", "[][grow]"));

        model.setAsync(false);
        
        JButton addButton = new JButton("Add new aggregation");
        add(addButton);
        addButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                Aggregation template = new Aggregation();
                template.setAggregationID(-1);
                model.addToBatch(template);
            }
        });
        
        JButton saveChanges = new JButton("Save changes");
        add(saveChanges);
        saveChanges.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                Aggregation template = new Aggregation();
                template.setAggregationID(-1);
                model.addToBatch(template);
            }
        });

        add(new JScrollPane(table), "cell 0 1,grow");
    }
    
    public AggregationsViewPanel() {
        // ONLY USED FOR VIEWING IN THE EDITOR
        this(null);
    }
    
    @Override public void start() {
        model.start();
        stop();
    }
    
    @Override public void stop() {
        model.stop();
    }
    
    @Override public void send(Aggregation t) {
        model.addToBatch(t);
    }

    public static void main(String[] args) throws ConnectorException {

        final SocketClient client = new SocketClient();
        client.addConnectionPoint(new InetSocketAddress(VLPorts.getSocketHubDefaultPort()));

        client.connect();

        PatternManagementService patternService = new PatternManagementService() {
            
            @Override public ObservableList<PatternModel> listPatterns() {
                
                return null;
                
            }
            
            @Override public List<String> getPatternNames() {
                
                List<String> names = new ArrayList<String>();
                Result<List<Pattern>> result = client.getPatternManagementAPI().getPatterns();
                if(result.isSuccessful()) {
                    List<Pattern> patterns = result.getValue();
                    for (Pattern pattern : patterns) {
                        names.add(pattern.getName());
                    }
                }
                return names;
                
            }
            
            @Override public Pattern getPatternByID(int patternID) {
                Pattern foundPattern = null;
                Result<List<Pattern>> result = client.getPatternManagementAPI().getPatterns();
                if(result.isSuccessful()) {
                    List<Pattern> patterns = result.getValue();
                    for (Pattern pattern : patterns) {
                        if(pattern.getPatternID() == patternID) {
                            foundPattern = pattern;
                            break;
                        }
                    }
                }
                return foundPattern;
                 
            }

            @Override public ObservableList<Pattern> getPatterns() {
                
                List<Pattern> patterns = null;
                Result<List<Pattern>> result = client.getPatternManagementAPI().getPatterns();
                if(result.isSuccessful()) {
                    patterns = result.getValue();                   
                }
                
                // TODO : this is nasty, its not properly observable, its just a clone
                ObservableList<Pattern> observableList = new ObservableList<Pattern>(patterns);
                return observableList;
                 
            }

            @Override public String getLabelName(int patternID, int labelIndex) {
                return null;
                 
            }

            @Override public String getPatternName(int patternID) {
                return null;
                 
            }
        };
        
        final AggregationsViewPanel panel = new AggregationsViewPanel(patternService);
        Result<List<Aggregation>> aggregations = client.getPatternManagementAPI().getAggregations();
        if(aggregations.isSuccessful()) {
            System.out.println("Aggregations reponse received : " + aggregations.getValue());
            List<Aggregation> value = aggregations.getValue();
            for (Aggregation aggregation : value) {
                panel.send(aggregation);
            }
        }
        
        panel.start();

        TestFrame.show(panel, 640, 480);

    }

}
