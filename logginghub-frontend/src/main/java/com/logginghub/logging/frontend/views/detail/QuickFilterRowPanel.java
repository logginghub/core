package com.logginghub.logging.frontend.views.detail;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.components.LevelsCheckboxListView;
import com.logginghub.logging.frontend.components.QuickFilterHistoryController;
import com.logginghub.logging.frontend.components.QuickFilterHistoryTextField;
import com.logginghub.logging.frontend.model.QuickFilterModel;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Binder;
import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.observable.ObservablePropertyListener;

public class QuickFilterRowPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLoggerFor(QuickFilterRowPanel.class);

    private JButton andOrToggle = new JButton("and");
    private JCheckBox enabledCheckbox = new JCheckBox("Enabled");
    private LevelsCheckboxListView quickLevelFilterCombo;
    private QuickFilterHistoryTextField quickFilterTextField;
    private JRadioButton regexRadioButton;

    private String layoutConstraints = "gap 2, ins 2";

    private QuickFilterModel model;

    private Map<String, JCheckBox> levelCheckBoxesByLevelName = new HashMap<String, JCheckBox>();

//    private LevelsCheckboxModel levelsCheckboxModel;

    public QuickFilterRowPanel() {
        setLayout(new MigLayout(layoutConstraints, "[][][][grow,fill][]", "[grow,fill]"));

     /*   Level[] levels = new Level[] { Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL };
        JCheckBox[] checkBoxes = new JCheckBox[levels.length];
        for (int i = 0; i < levels.length; i++) {
            checkBoxes[i] = new JCheckBox(levels[i].getName(), true) {
                @Override protected void processMouseEvent(MouseEvent e) {
                    System.out.println(e);
                    // super.processMouseEvent(e);

                }

                @Override public void doClick() {
                    System.out.println("do click");

                }

                @Override public void doClick(int pressTime) {
                    System.out.println("do click");

                }
                
                
            };
            levelCheckBoxesByLevelName.put(levels[i].getName(), checkBoxes[i]);
        }*/

//        levelsCheckboxModel = new LevelsCheckboxModel();
        
        quickLevelFilterCombo = new LevelsCheckboxListView();
        quickLevelFilterCombo.setName("quickLevelFilterCombo");
//        quickLevelFilterCombo.setSelectedItem(Level.ALL);

        quickFilterTextField = new QuickFilterHistoryTextField();
        regexRadioButton = new JRadioButton("Regex");
        regexRadioButton.setName("regexRadioButton");
        regexRadioButton.setSelected(false);

        enabledCheckbox.setName("enabledCheckbox");

        Dimension minimumSize = new Dimension(60, 30);
        andOrToggle.setMinimumSize(minimumSize);
        andOrToggle.setMaximumSize(minimumSize);
        andOrToggle.setPreferredSize(minimumSize);

        add(enabledCheckbox);
        add(andOrToggle);
        add(quickLevelFilterCombo);
        add(quickFilterTextField);
        add(regexRadioButton, "alignx center");

    }

    public void bind(QuickFilterHistoryController quickFilterHistoryController,
                     final QuickFilterModel model,
                     final ObservableProperty<Boolean> isAndFilter) {
        this.model = model;

        quickFilterTextField.bind(quickFilterHistoryController);

        isAndFilter.addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override public void onPropertyChanged(Boolean oldValue, Boolean isAnd) {
                if (isAnd) {
                    andOrToggle.setText("and");
                }
                else {
                    andOrToggle.setText("or");
                }
            }
        });

        andOrToggle.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                isAndFilter.set(!isAndFilter.get().booleanValue());
            }
        });

        Binder.bind(model.getIsEnabled(), enabledCheckbox);
        Binder.bind(model.getFilterText(), quickFilterTextField);
        Binder.bind(model.getIsRegex(), regexRadioButton);

        // Bind the checkboxes by hand
        // Binder.bind(model.getLevelFilter(), quickLevelFilterCombo);

        quickLevelFilterCombo.bind(model.getLevelFilter().get());
        
//        quickLevelFilterCombo.setSelectedItem(model.getLevelFilter().get());

//        model.getLevelFilter().addListener(new ObservablePropertyListener<Level>() {
//            public void onPropertyChanged(Level oldValue, Level newValue) {
//                quickLevelFilterCombo.setSelectedItem(levelCheckBoxesByLevelName.get(newValue.getName()));
//            }
//        });

//        quickLevelFilterCombo.addItemListener(new ItemListener() {
//            @SuppressWarnings("unchecked") public void itemStateChanged(ItemEvent e) {
//                if (e.getStateChange() == ItemEvent.SELECTED) {
//                    JCheckBox box = (JCheckBox) quickLevelFilterCombo.getSelectedItem();
//                    model.getLevelFilter().set(Level.parse(box.getText()));
//                }
//            }
//        });
    }

    public QuickFilterModel getModel() {
        return model;
    }

    public QuickFilterHistoryTextField getQuickFilterTextField() {
        return quickFilterTextField;
    }

    public void setAndOrVisible(boolean isVisible) {
        if (!isVisible) {
            remove(enabledCheckbox);
            remove(andOrToggle);
            setLayout(new MigLayout(layoutConstraints, "[][grow,fill][]", "[grow,fill]"));
            revalidate();
            doLayout();
        }
        else {
            removeAll();
            setLayout(new MigLayout(layoutConstraints, "[][][][grow,fill][]", "[grow,fill]"));
            add(enabledCheckbox);
            add(andOrToggle);
            add(quickLevelFilterCombo);
            add(quickFilterTextField);
            add(regexRadioButton, "alignx center");
            revalidate();
            doLayout();
        }
    }

}
