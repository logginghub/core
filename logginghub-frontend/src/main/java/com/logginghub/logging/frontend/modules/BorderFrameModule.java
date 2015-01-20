package com.logginghub.logging.frontend.modules;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.logginghub.logging.frontend.PathHelper;
import com.logginghub.logging.frontend.SmartJFrame;
import com.logginghub.logging.frontend.services.FrameService;
import com.logginghub.logging.frontend.services.LayoutService;
import com.logginghub.utils.DelayedAutosavingFileBasedMetadata;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.logging.Logger;

public class BorderFrameModule implements LayoutService, FrameService {

    private static final Logger logger = Logger.getLoggerFor(BorderFrameModule.class);
    private SmartJFrame frame;
    private String name;
    private boolean quitOnClose = false;
    private String iconResource = "/icons/LoggingHubLogo.png";

    public BorderFrameModule(String name) {
        File properties = PathHelper.getSettingsFile(name);

        logger.info("Loading/saving local properties : {}", properties.getAbsolutePath());
        DelayedAutosavingFileBasedMetadata metadata = new DelayedAutosavingFileBasedMetadata(properties, 1000);
        if (properties.exists()) {
            metadata.load();
        }
        else {
            createDefaultSettings(metadata);
        }

        frame = new SmartJFrame(name, metadata);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.setTitle(name);
        ImageIcon image = new ImageIcon(FileUtils.readAsBytes(ResourceUtils.openStream(iconResource)));
        frame.setIconImage(image.getImage());

        if (quitOnClose) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        
        frame.setVisible(true);
    }

    private static Metadata createDefaultSettings(Metadata data) {

        data.set("frame.state", 0);
        data.set("frame.extendedState", 0);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        data.set("frame.x", (int) (screenSize.width * 0.1));
        data.set("frame.y", (int) (screenSize.height * 0.1));
        data.set("frame.width", (int) (screenSize.width * 0.8));
        data.set("frame.height", (int) (screenSize.height * 0.8));

        return data;
    }

    @Override public void add(Component component, String layout) {
        logger.info("Adding component '{}' to '{}'", component, layout);
        if (component instanceof JMenuBar) {
            JMenuBar jMenuBar = (JMenuBar) component;
            frame.setJMenuBar(jMenuBar);
        }
        else {
            frame.getContentPane().add(component, layout);
        }
    }

    @Override public void dispose() {
        frame.dispose();
    }

    public void setIconResource(String iconResource) {
        this.iconResource = iconResource;
    }

    public String getIconResource() {
        return iconResource;
    }

}
