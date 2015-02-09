package com.logginghub.logging.frontend.views.stack;

import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messages.StackTrace;
import com.logginghub.utils.WildcardOrRegexMatcher;
import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.observable.ObservableItemContainer;
import com.logginghub.utils.observable.ObservableListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.util.regex.PatternSyntaxException;

public class StackInstanceFilterView extends JPanel {

    private final JTextField threadFilter;
    private final JCheckBox threadFilterRegexCheckBox;

    private JTextField instanceFilter;
    private JCheckBox instanceFilterRegexCheckBox;

    private JTextField stackFilter;
    private JCheckBox stackFilterRegexCheckBox;

    //    private Stream<MutlipleThreadViewFilterPanel> filterChangedStream = new Stream<MutlipleThreadViewFilterPanel>();
    private WildcardOrRegexMatcher stackMatcher;
    private WildcardOrRegexMatcher instanceMatcher;
    private WildcardOrRegexMatcher threadMatcher;
    private boolean badFilters;

    public StackInstanceFilterView() {
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

        //        stackFilterRegexCheckBox.addActionListener(new ActionListener() {
        //            @Override public void actionPerformed(ActionEvent e) {
        //                updateFilters();
        //            }
        //        });
        //
        //        instanceFilterRegexCheckBox.addActionListener(new ActionListener() {
        //            @Override public void actionPerformed(ActionEvent e) {
        //                updateFilters();
        //            }
        //        });
        //
        //        threadFilterRegexCheckBox.addActionListener(new ActionListener() {
        //            @Override public void actionPerformed(ActionEvent e) {
        //                updateFilters();
        //            }
        //        });
        //
        //        instanceFilter.addKeyListener(new KeyListener() {
        //            @Override public void keyPressed(KeyEvent e) {}
        //
        //            @Override public void keyReleased(KeyEvent e) {
        //                updateFilters();
        //            }
        //
        //            @Override public void keyTyped(KeyEvent e) {
        //            }
        //        });
        //
        //        stackFilter.addKeyListener(new KeyListener() {
        //            @Override public void keyPressed(KeyEvent e) {}
        //
        //            @Override public void keyReleased(KeyEvent e) {
        //                updateFilters();
        //            }
        //
        //            @Override public void keyTyped(KeyEvent e) {
        //
        //            }
        //        });
        //
        //        threadFilter.addKeyListener(new KeyListener() {
        //            @Override public void keyPressed(KeyEvent e) {}
        //
        //            @Override public void keyReleased(KeyEvent e) {
        //                updateFilters();
        //            }
        //
        //            @Override public void keyTyped(KeyEvent e) {
        //            }
        //        });

    }

    public void bind(StackInstanceFilterModel model) {

        Binder2 binder = new Binder2();

        binder.bind(model.getInstanceFilter(), instanceFilter);
        binder.bind(model.getStackFilter(), stackFilter);
        binder.bind(model.getThreadFilter(), threadFilter);

        binder.bind(model.getInstanceFilterIsRegex(), instanceFilterRegexCheckBox);
        binder.bind(model.getStackFilterIsRegex(), stackFilterRegexCheckBox);
        binder.bind(model.getThreadFilterIsRegex(), threadFilterRegexCheckBox);

        model.addListener(new ObservableListener() {
            @Override public void onChanged(ObservableItemContainer observable, Object childPropertyThatChanged) {
                updateFilters();
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
            instanceMatcher = new WildcardOrRegexMatcher(instanceFilter.getText(), instanceFilterRegexCheckBox.isSelected());
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

        //        filterChangedStream.send(MutlipleThreadViewFilterPanel.this);
    }

    //    public Stream<MutlipleThreadViewFilterPanel> getFilterChangedStream() {
    //        return filterChangedStream;
    //    }

    public boolean includeColumn(StackTraceView.Column column) {
        return instanceMatcher.matches(column.name);
    }

    public boolean includeCell(StackTraceView.Cell cell) {
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

    public boolean passesFilter(StackTrace trace, StackSnapshot stackSnapshot) {

        boolean include = false;
        if (instanceMatcher.matches(stackSnapshot.getInstanceKey().buildKey())) {
            if (threadMatcher.matches(trace.getThreadName())) {
                if (stackMatcher.matches(trace.getThreadState()) || stackMatcher.matches(trace.getItems().toString())) {
                    include = true;
                }
            }
        }

        return include;
    }
}
