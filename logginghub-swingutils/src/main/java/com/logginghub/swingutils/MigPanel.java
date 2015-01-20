package com.logginghub.swingutils;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class MigPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public MigPanel() {
        super();
        setLayout(new MigLayout());
    }

    public MigPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        setLayout(new MigLayout());
    }

    public MigPanel(String layoutConstraints, String columnConstraints, String rowConstraints) {
        setLayout(new MigLayout(layoutConstraints, columnConstraints, rowConstraints));
    }
}
