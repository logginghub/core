package com.logginghub.logging.frontend.brainscan;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class BrainScanViewPanel extends JPanel {
    
    private TreeMapComponent treeMapComponent;
    private StrobeRequestPanel strobeRequestPanel;
    private MutlipleThreadViewPanel mutlipleThreadViewPanel;
//    private JScrollPane scrollPane;
    private ThreadGroupingEditorPanel threadGroupingEditorPanel;

    public BrainScanViewPanel() {
        setLayout(new MigLayout("", "[grow,fill]", "[grow,fill]"));
        
//        scrollPane = new JScrollPane();
        
//        threadGroupingEditorPanel = new ThreadGroupingEditorPanel();        
//        add(threadGroupingEditorPanel, "cell 0 1");
        
        mutlipleThreadViewPanel = new MutlipleThreadViewPanel();
        add(mutlipleThreadViewPanel, "cell 0 0,grow");
//        scrollPane.setViewportView(mutlipleThreadViewPanel);
        
//        strobeRequestPanel = new StrobeRequestPanel();
//        add(strobeRequestPanel, "cell 0 2");
        
//        treeMapComponent = new TreeMapComponent();
//        add(treeMapComponent, "cell 0 3, grow");
    }

    // TODO : get rid of this, its only so it can be repainted - repaints should happen when the model changes!
    public TreeMapComponent getTreeMapComponent() {
        return treeMapComponent;
    }

    public void bind(final BrainScanController controller) {
//        treeMapComponent.bind(controller);
//        threadGroupingEditorPanel.bind(controller.getThreadGroupingModel());
        
//        strobeRequestPanel.getRequestStream().addDestination(new Destination<StackStrobeRequest>() {
//            @Override public void send(StackStrobeRequest t) {
//                controller.sendStrobeRequest(t);
//            }
//        });
        
        mutlipleThreadViewPanel.bind(controller, controller.getThreadViewModelsList());
        
    }

}
