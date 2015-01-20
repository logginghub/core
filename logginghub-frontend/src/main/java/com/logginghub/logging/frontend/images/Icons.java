package com.logginghub.logging.frontend.images;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import com.logginghub.logging.frontend.Utils;

public class Icons {
    public enum IconIdentifier {
        Pause,
        Add,
//        Remove,
        GreenBall,
        Filter,
        Stop,
        MenuItem,
        File,
        ViewDetailed,
        ViewSummary,
        Magnify,
        NewWindow,
        Utitilites,
        Play,
        Clear,
        Clock,
        Locked,
        Unlocked,
        Delete,
        AddCircle,
        AddCircleSmall,
        LoggingHubLogo;
    }

    private static Map<IconIdentifier, ImageIcon> iconCache = new HashMap<IconIdentifier, ImageIcon>();
    private static Map<String, ImageIcon> dynamicCache = new HashMap<String, ImageIcon>();

    public static ImageIcon get(IconIdentifier id) {
        ImageIcon imageIcon = iconCache.get(id.name());
        if (imageIcon == null) {
            imageIcon = Utils.createImageIcon("/icons/" + id.name() + ".png", id.name());
            iconCache.put(id, imageIcon);
        }

        return imageIcon;
    }

    
    
    public static ImageIcon load(String imagePath) {
        ImageIcon imageIcon = dynamicCache.get(imagePath);
        if (imageIcon == null) {
            imageIcon = Utils.createImageIcon(imagePath, imagePath);
            dynamicCache.put(imagePath, imageIcon);
        }

        return imageIcon;
    }

}
