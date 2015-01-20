package com.logginghub.logging.frontend.modules.configuration;

import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.frontend.modules.MenuBarModule;
import com.logginghub.utils.module.Configures;

@Configures(MenuBarModule.class)
public class MenuBarConfiguration {

    @XmlAttribute boolean fileMenu = true;
    
    public boolean isFileMenu() {
        return fileMenu;
    }

}
