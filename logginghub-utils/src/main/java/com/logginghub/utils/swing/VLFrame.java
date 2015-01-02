package com.logginghub.utils.swing;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.logginghub.utils.DelayedAction;
import com.logginghub.utils.FileBasedMetadata;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.logging.Logger;

/**
 * Utility JFrame with enhancements for VL apps
 * 
 * @author James
 * 
 */
public class VLFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // private Metadata dynamicSettings;

    private FileBasedMetadata metadata;

    private DelayedAction action = new DelayedAction(500, TimeUnit.MILLISECONDS);

    private final String name;
    private final String property;

    private static final Logger logger = Logger.getLoggerFor(VLFrame.class);

    // public VLFrame(Metadata settings) {
    // this.dynamicSettings = settings;
    // initialise();
    // }

    public VLFrame(String name, String iconResource) {
        this.name = name;
        this.property = name + ".";
        setTitle(name);
        setIcon(iconResource);
        initialise();
    }

    static public Image loadImage(String name) {
        URL url = ResourceUtils.openURL(name);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image image = tk.getImage(url);
        return image;
    }

    public void setIcon(String path) {
        setIconImage(loadImage(path));
    }

    private void initialise() {

        File homeDirectory = FileUtils.getHomeDirectory();
        File vlDirectory = new File(homeDirectory, ".vertexlabs");
        File frameSettings = new File(vlDirectory, "vlFrameSettings");
        File thisWindowFile = new File(frameSettings, name + ".properties");
        frameSettings.mkdirs();

        metadata = new FileBasedMetadata(thisWindowFile);

        if (thisWindowFile.exists()) {
            logger.debug("Loading frame metadata from '{}'", thisWindowFile.getAbsolutePath());
            metadata.load();
        }
        else {
            logger.debug("No metadata found at '{}', using defaults", thisWindowFile.getAbsolutePath());
        }

        int border = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        setState(metadata.getInt(property + "frame.state", JFrame.NORMAL));
        setExtendedState(metadata.getInt(property + "frame.extendedState", JFrame.NORMAL));
        setLocation(metadata.getInt(property + "frame.x", 10), this.metadata.getInt(property + "frame.y", 10));
        setSize(metadata.getInt(property + "frame.width", screenSize.width - border - border), metadata.getInt(property + "frame.height", screenSize.height - border - border));

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final Runnable saver = new Runnable() {
            public void run() {
                logger.debug("Saving window details {}", metadata);
                metadata.save();
            }
        };

        addComponentListener(new ComponentListener() {

            public void componentHidden(ComponentEvent e) {

            }

            public void componentMoved(ComponentEvent e) {
                Point location = getLocation();

                if (getExtendedState() == 6) {
                    // This is maximmised, dont save the size
                    logger.trace("Window moved via maximise to {}, not saving", location);
                }
                else {
                    logger.trace("Window moved to {}", location);
                    metadata.put(property + "frame.x", location.x);
                    metadata.put(property + "frame.y", location.y);
                    action.execute(saver);
                }
            }

            public void componentResized(ComponentEvent e) {
                Dimension size = getSize();
                logger.trace("Window resized to {}", size);
                if (getExtendedState() == 6) {
                    // This is maximmised, dont save the size
                    logger.trace("Window resized via maximise to {}, not saving", size);
                }
                else {
                    logger.trace("Window resized to {}", size);
                    metadata.put(property + "frame.width", size.width);
                    metadata.put(property + "frame.height", size.height);
                    action.execute(saver);
                }
            }

            public void componentShown(ComponentEvent e) {

            }
        });

        addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent e) {
                logger.trace("Window state changed to {} extended {}", getState(), getExtendedState());
                metadata.put(property + "frame.extendedState", getExtendedState());
                metadata.put(property + "frame.state", getState());
                action.execute(saver);
            }
        });

    }

    public static void main(String[] args) {
        Logger.setLevel(Logger.trace, FileBasedMetadata.class, VLFrame.class);

        VLFrame frame = new VLFrame("Test VL Frame", "D:\\Development\\Resources\\Icons\\crystal_project\\32x32\\apps\\agt_games.png");
        frame.add(new JLabel("This is a test label"));
        frame.setVisible(true);
    }

}
