package com.logginghub.logging.frontend.brainscan;

import com.logginghub.utils.Stream;
import com.logginghub.utils.WildcardOrRegexMatcher;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.PatternSyntaxException;

public class MutlipleThreadViewFilterPanel extends JPanel {

    private final JTextField threadFilter;
    private final JCheckBox threadFilterRegexCheckBox;

    private JTextField instanceFilter;
    private JCheckBox instanceFilterRegexCheckBox;

    private JTextField stackFilter;
    private JCheckBox stackFilterRegexCheckBox;

    private Stream<MutlipleThreadViewFilterPanel> filterChangedStream = new Stream<MutlipleThreadViewFilterPanel>();
    private WildcardOrRegexMatcher stackMatcher;
    private WildcardOrRegexMatcher instanceMatcher;
    private WildcardOrRegexMatcher threadMatcher;
    private boolean badFilters;

    public MutlipleThreadViewFilterPanel() {
        setLayout(new MigLayout("", "[][grow,fill][]", "[][][fill]"));

        instanceFilter = new JTextField("*.*.*.*");
        add(new JLabel("Instance Filter"), "cell 0 0");
        add(instanceFilter, "cell 1 0, grow");
        instanceFilterRegexCheckBox = new JCheckBox("Regex", false);
        add(instanceFilterRegexCheckBox, "cell 2 0");

        stackFilter = new JTextField("*");
        add(new JLabel("Stack Filter"), "cell 0 1");
        add(stackFilter, "cell 1 1, grow");
        stackFilterRegexCheckBox = new JCheckBox("Regex", false);
        add(stackFilterRegexCheckBox, "cell 2 1");

        threadFilter = new JTextField("*");
        add(new JLabel("Thread Filter"), "cell 0 2");
        add(threadFilter, "cell 1 2, grow");
        threadFilterRegexCheckBox = new JCheckBox("Regex", false);
        add(threadFilterRegexCheckBox, "cell 2 2");

        stackFilterRegexCheckBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                updateFilters();
            }
        });

        instanceFilterRegexCheckBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                updateFilters();
            }
        });

        threadFilterRegexCheckBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                updateFilters();
            }
        });

        instanceFilter.addKeyListener(new KeyListener() {
            @Override public void keyPressed(KeyEvent e) {}

            @Override public void keyReleased(KeyEvent e) {
                updateFilters();
            }

            @Override public void keyTyped(KeyEvent e) {
            }
        });

        stackFilter.addKeyListener(new KeyListener() {
            @Override public void keyPressed(KeyEvent e) {}

            @Override public void keyReleased(KeyEvent e) {
                updateFilters();
            }

            @Override public void keyTyped(KeyEvent e) {

            }
        });

        threadFilter.addKeyListener(new KeyListener() {
            @Override public void keyPressed(KeyEvent e) {}

            @Override public void keyReleased(KeyEvent e) {
                updateFilters();
            }

            @Override public void keyTyped(KeyEvent e) {
            }
        });

        updateFilters();
    }

    private void updateFilters() {

        stackMatcher = null;
        instanceMatcher = null;
        threadMatcher = null;
        badFilters = false;

        try {
            instanceMatcher = new WildcardOrRegexMatcher(instanceFilter.getText(),
                    instanceFilterRegexCheckBox.isSelected());
            instanceFilter.setBackground(Color.white);
        } catch (PatternSyntaxException pse) {
            instanceFilter.setBackground(Color.red);
            badFilters = true;
        }

        try {
            stackMatcher = new WildcardOrRegexMatcher(stackFilter.getText(), stackFilterRegexCheckBox.isSelected());
            stackFilter.setBackground(Color.white);
        } catch (PatternSyntaxException pse) {
            stackFilter.setBackground(Color.red);
            badFilters = true;
        }

        try {
            threadMatcher = new WildcardOrRegexMatcher(threadFilter.getText(), threadFilterRegexCheckBox.isSelected());
            threadFilter.setBackground(Color.white);
        } catch (PatternSyntaxException pse) {
            threadFilter.setBackground(Color.red);
            badFilters = true;
        }

        filterChangedStream.send(MutlipleThreadViewFilterPanel.this);
    }

    public Stream<MutlipleThreadViewFilterPanel> getFilterChangedStream() {
        return filterChangedStream;
    }

    public boolean includeColumn(MutlipleThreadViewPanel.Column column) {
        return instanceMatcher.matches(column.name);
    }

    public boolean includeCell(MutlipleThreadViewPanel.Cell cell) {
        boolean include = false;
        if (threadMatcher.matches(cell.name)) {
            if (stackMatcher.matches(cell.name) ||
                    stackMatcher.matches(cell.panel.getModel().getState().get()) ||
                    stackMatcher.matches(cell.panel.getModel().getStack().get())) {
                include = true;
            }
        }

        return include;
    }

}
