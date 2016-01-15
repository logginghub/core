package com.logginghub.utils.swing;

import com.logginghub.utils.Out;
import com.logginghub.utils.StringUtils;

import java.awt.*;

/**
 * Created by james on 14/01/2016.
 */
public class Utils {

    public static void dumpContainer(Container panel, int indent) {

        String indentString = StringUtils.repeat("   ", indent);

        int children = panel.getComponentCount();
        for (int i = 0; i < children; i++) {
            Component component = panel.getComponent(i);
            Out.out("{}{-50}'{50}' visible={6} size={10} preferred={10}",
                    indentString,
                    component.getClass().getSimpleName(),
                    component.getName(),
                    component.isVisible(),
                    format(component.getSize()),
                    format(component.getPreferredSize()));
            if (component instanceof Container) {
                dumpContainer((Container) component, indent++);
            }
        }
    }

    private static String format(Dimension preferredSize) {
        return StringUtils.format("{}x{}", preferredSize.getWidth(), preferredSize.getHeight());
    }

}
