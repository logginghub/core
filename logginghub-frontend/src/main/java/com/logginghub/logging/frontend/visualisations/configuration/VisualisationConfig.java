package com.logginghub.logging.frontend.visualisations.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.logginghub.utils.JAXBConfiguration;

@XmlAccessorType(XmlAccessType.FIELD) @XmlRootElement public class VisualisationConfig {
    
    @XmlElement private VectorConfig screenSize = new VectorConfig(800, 600);
    @XmlElement private VectorConfig screenPosition = new VectorConfig(100, 100);
    
    @XmlElement List<BoxConfig> box = new ArrayList<BoxConfig>();
    @XmlElement List<EmitterConfig> emitter = new ArrayList<EmitterConfig>();
    @XmlElement List<EnvironmentConfiguration> environment= new ArrayList<EnvironmentConfiguration>();
   
    @XmlAttribute private boolean showGui = false;
    @XmlAttribute private boolean useAdditiveBlending = false;
    
    @XmlAttribute private String shape = "star";

    //public EmitterConfig getEmitter() {
      //  return emitter.get(0);
//    }
    
    public List<BoxConfig> getBoxes() {
        return box;
    }
    
    public List<EnvironmentConfiguration> getEnvironments() {
        return environment;
    }
    
    public List<EmitterConfig> getEmitters() {
        return emitter;
    }    
    
    
    public String getShape() {
        return shape;
    }
    
    public void setShape(String shape) {
        this.shape = shape;
    }
    
    public boolean useAdditiveBlending() {
        return useAdditiveBlending;
    }
    
    public void setUseAdditiveBlending(boolean useAdditiveBlending) {
        this.useAdditiveBlending = useAdditiveBlending;
    }

    public boolean getShowGui() {
        return showGui;
    }
    
    public void setShowGui(boolean showGui) {
        this.showGui = showGui;
    }
    
    public VectorConfig getScreenPosition() {
        return screenPosition;
    }
    
    public VectorConfig getScreenSize() {
        return screenSize;
    }
    
    public void setScreenPosition(VectorConfig screenPosition) {
        this.screenPosition = screenPosition;
    }
    
    public void setScreenSize(VectorConfig screenSize) {
        this.screenSize = screenSize;
    }
    
    
    public static VisualisationConfig loadConfiguration(String configurationPath) {
        VisualisationConfig configuration = JAXBConfiguration.loadConfiguration(VisualisationConfig.class, configurationPath);
        return configuration;
    }
}
