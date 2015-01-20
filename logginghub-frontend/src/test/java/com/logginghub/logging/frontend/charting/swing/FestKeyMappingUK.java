package com.logginghub.logging.frontend.charting.swing;

import static java.awt.event.InputEvent.SHIFT_MASK;
import static java.awt.event.KeyEvent.VK_2;
import static java.util.Collections.unmodifiableList;
import static org.fest.swing.keystroke.KeyStrokeMapping.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fest.swing.keystroke.KeyStrokeMapping;
import org.fest.swing.keystroke.KeyStrokeMappingProvider;

public class FestKeyMappingUK implements KeyStrokeMappingProvider {

    public Collection<KeyStrokeMapping> keyStrokeMappings() {
        return SingletonHolder.instance;
    }

    // Thread-safe, lazy-loading singleton.
    private static class SingletonHolder {
        static List<KeyStrokeMapping> instance = createMappings();
    }

    private static List<KeyStrokeMapping> createMappings() {
        List<KeyStrokeMapping> mappings = new ArrayList<KeyStrokeMapping>(100);
        mappings.add(mapping('"', VK_2, SHIFT_MASK));
        return unmodifiableList(mappings);
    }
}
