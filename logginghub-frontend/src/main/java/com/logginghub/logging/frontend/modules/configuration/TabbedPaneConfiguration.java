package com.logginghub.logging.frontend.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.frontend.modules.TabbedPaneModule;
import com.logginghub.utils.module.Configures;

@Configures(TabbedPaneModule.class)
@XmlAccessorType(XmlAccessType.FIELD)  public class TabbedPaneConfiguration extends AbstractContainerConfiguration {
    
    @XmlAttribute private String layout;

    public String getLayout() {
        return layout;
    }
    
    public void setLayout(String layout) {
        this.layout = layout;
    }
}
