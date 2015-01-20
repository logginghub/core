package com.logginghub.logging.frontend.modules;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.logging.frontend.services.PatternManagementService;
import com.logginghub.logging.frontend.services.PatternisedEventService;
import com.logginghub.logging.frontend.visualisations.LibGDXSpritesView;
import com.logginghub.logging.frontend.visualisations.ParticleSettings;
import com.logginghub.logging.frontend.visualisations.PatternisedEventParticalSettingsSource;
import com.logginghub.logging.frontend.visualisations.RawEventParticalSettingsSource;
import com.logginghub.logging.frontend.visualisations.VisualisationProfile;
import com.logginghub.logging.frontend.visualisations.VisualiserModel;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.Debug;
import com.logginghub.utils.Destination;
import com.logginghub.utils.module.Inject;

public class VisualisationViewModule {

    private String layout;
    private LayoutService layoutService;
    private EnvironmentMessagingService messagingService;
    private PatternisedEventService patterniserService;
    private JPanel panel;

    private String filterPattern = null;

    private LibGDXSpritesView view;
    private LogEventListener rawEventListener;
    private Destination<ParticleSettings> particleSourceConnector;
    private PatternisedEventParticalSettingsSource patternisedParticleSource;
    private RawEventParticalSettingsSource rawEventParticleSource;
    private PatternManagementService patternManagement;

    public VisualisationViewModule() {
        panel = new JPanel(new MigLayout("", "", ""));
    }

    @Inject public void setPatterniserService(PatternisedEventService patterniserService, PatternManagementService patternManagement) {
        this.patterniserService = patterniserService;
        this.patternManagement = patternManagement;
    }

    @Inject public void setLayoutService(LayoutService layoutService) {
        this.layoutService = layoutService;
    }

    @Inject public void setMessagingService(EnvironmentMessagingService messagingService) {
        this.messagingService = messagingService;
    }

    public void initialise() {

        layoutService.add(panel, layout);

        final ViewDetails viewDetails = new ViewDetails(0, 0);
        final VisualiserModel model = new VisualiserModel(200000, viewDetails);

        final VisualisationProfile profile = new VisualisationProfile(model, viewDetails);

        JComboBox comboBox = createProfileCombo(model, profile);
        JComboBox sourceBox = createSourceCombo(model, profile);

        profile.setupFountain();
        panel.add(comboBox, "cell 0 0");
        panel.add(sourceBox, "cell 1 0, wrap");

        // Choose a source - either raw events, or patternised events
        // Choose a particle settings source -
        // Attach the partical settings source up to an EntitySource which has been configured
        // The EntitySource is responsible for actually handling the particles

        rawEventParticleSource = new RawEventParticalSettingsSource();
        patternisedParticleSource = new PatternisedEventParticalSettingsSource();

        rawEventListener = new LogEventListener() {
            @Override public void onNewLogEvent(LogEvent event) {
                rawEventParticleSource.send(event);
            }
        };

        messagingService.addLogEventListener(rawEventListener);

        particleSourceConnector = new Destination<ParticleSettings>() {
            @Override public void send(ParticleSettings t) {
                model.spawn(t);
            }
        };

        rawEventParticleSource.addDestination(particleSourceConnector);
        patternisedParticleSource.addDestination(particleSourceConnector);

        // VisualisationConfig config = new VisualisationConfig();
        // FireworksController controller = new FireworksController(model, config);

        
        view = new LibGDXSpritesView(model, viewDetails);

        panel.addComponentListener(new ComponentListener() {
            private LwjglAWTCanvas canvas;

            @Override public void componentShown(ComponentEvent e) {
                Debug.out("Panel shown : {}", e);
            }

            @Override public void componentResized(ComponentEvent e) {
                Debug.out("Panel resized : {}", e);
                
                if(canvas != null) {
                    panel.remove(canvas.getCanvas());
                    canvas.exit();
                    canvas = null;
                }
                
                // TODO : looks like there is a bug on linux that is drawing over Swing/AWT components
                canvas = new LwjglAWTCanvas(view, false);
                canvas.getCanvas().setSize(panel.getWidth(), panel.getHeight());
//                canvas.getCanvas().setLocation(200, 50);
                panel.add(canvas.getCanvas(), "cell 0 1, span 2");
                
                viewDetails.width = panel.getWidth();
                viewDetails.height = panel.getHeight();
            }

            @Override public void componentMoved(ComponentEvent e) {
                Debug.out("Panel moved : {}", e);
            }

            @Override public void componentHidden(ComponentEvent e) {
                Debug.out("Panel hidden : {}", e);
            }
        });

//        SwingUtilities.invokeLater(new Runnable() {
//            @Override public void run() {
//                LwjglAWTCanvas canvas = new LwjglAWTCanvas(view, false);
//                canvas.getCanvas().setSize(800, 600);
//                panel.add(canvas.getCanvas(), "cell 0 1, span 2");
//            }
//        });
    }

    private JComboBox createSourceCombo(final VisualiserModel model, final VisualisationProfile profile) {
        final DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
        comboModel.addElement("Event levels");

        List<String> patternNames = patternManagement.getPatternNames();
        for (String string : patternNames) {
            comboModel.addElement(string);
        }

        JComboBox comboBox = new JComboBox(comboModel);
        comboBox.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int patternID = Integer.parseInt(comboModel.getSelectedItem().toString());

                    // TODO : work out how to put this back in post patternID refactor
//                    if (sourceName.equals("Event levels")) {
//                        messagingService.addLogEventListener(rawEventListener);
//                        patterniserService.removePatternisedEventListener(patternisedParticleSource);
//                    }
//                    else {
                        messagingService.removeLogEventListener(rawEventListener);
                        patternisedParticleSource.setPatternID(patternID);
                        patterniserService.addPatternisedEventListener(patternisedParticleSource);
//                    }

                    model.reset();
                    view.updateShape();
                }
            }
        });
        return comboBox;

    }

    private JComboBox createProfileCombo(final VisualiserModel model, final VisualisationProfile profile) {
        final DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
        comboModel.addElement("Fountain");
        comboModel.addElement("Electromagnetic");
        comboModel.addElement("Catherine Wheel Offset");
        comboModel.addElement("Catherine Wheel");
        comboModel.addElement("Starfield");
        comboModel.addElement("Edge");
        JComboBox comboBox = new JComboBox(comboModel);
        comboBox.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String visualisationName = comboModel.getSelectedItem().toString();

                    profile.reset();

                    if (visualisationName.equals("Catherine Wheel Offset")) {
                        profile.setupRotateOffsetCentre();
                    }
                    else if (visualisationName.equals("Catherine Wheel")) {
                        profile.setupRotateCentre();
                    }
                    else if (visualisationName.equals("Starfield")) {
                        profile.setupStarField();
                    }
                    else if (visualisationName.equals("Fountain")) {
                        profile.setupFountain();
                    }
                    else if (visualisationName.equals("Electromagnetic")) {
                        profile.setupEmEdge();
                    }
                    else if (visualisationName.equals("Edge")) {
                        profile.setupEdge();
                    }

                    model.reset();
                    view.updateShape();
                }
            }
        });
        return comboBox;
    }

    public void start() {

    }

}
