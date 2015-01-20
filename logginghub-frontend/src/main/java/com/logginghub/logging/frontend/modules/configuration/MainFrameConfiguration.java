package com.logginghub.logging.frontend.modules.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.logginghub.logging.frontend.modules.MainFrameModule;
import com.logginghub.utils.module.Configures;

@Configures(MainFrameModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class MainFrameConfiguration extends AbstractContainerConfiguration {
    
    @XmlElement private List<MenuBarConfiguration> menuBar = new ArrayList<MenuBarConfiguration>();
    @XmlElement private List<TabbedPaneConfiguration> tabbedPane = new ArrayList<TabbedPaneConfiguration>();
    @XmlAttribute private String name = "mainFrame";

    public List<MenuBarConfiguration> getMenuBars() {
        return menuBar;
    }
    
    public List<TabbedPaneConfiguration> getTabbedPanes() {
        return tabbedPane;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
