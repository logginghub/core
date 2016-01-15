package com.logginghub.logging.frontend.views.logeventdetail;

import com.logginghub.logging.frontend.components.LevelsCheckboxListView;
import com.logginghub.logging.frontend.components.QuickFilterHistoryController;
import com.logginghub.logging.frontend.components.QuickFilterHistoryTextField;
import com.logginghub.logging.frontend.model.CustomQuickFilterModel;
import com.logginghub.logging.frontend.model.LevelNamesModel;
import com.logginghub.logging.frontend.model.ObservableListListener;
import com.logginghub.logging.frontend.model.QuickFilterModel;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Binder;
import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class QuickFilterRowPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLoggerFor(QuickFilterRowPanel.class);

    private MigLayout customFiltersLayout = new MigLayout("gap 2, ins 2", "[grow, fill]", "[grow, fill]");
    private JPanel customFiltersPanel = new JPanel(customFiltersLayout);
    private JButton andOrToggle = new JButton("and");
    private JCheckBox enabledCheckbox = new JCheckBox("Enabled");
    private LevelsCheckboxListView quickLevelFilterCombo;
    private QuickFilterHistoryTextField quickFilterTextField;

    private List<QuickFilterHistoryTextField> customFilters = new ArrayList<QuickFilterHistoryTextField>();

    private JRadioButton regexRadioButton;

    private String layoutConstraints = "gap 2, ins 2";

    private QuickFilterModel model;

    public QuickFilterRowPanel(LevelNamesModel levelNamesModel) {
        setLayout(new MigLayout(layoutConstraints, "[][][][grow,fill][grow, fill][]", "[grow,fill]"));

        quickLevelFilterCombo = new LevelsCheckboxListView(levelNamesModel);
        quickLevelFilterCombo.setName("quickLevelFilterCombo");

        quickFilterTextField = new QuickFilterHistoryTextField();
        regexRadioButton = new JRadioButton("Regex");
        regexRadioButton.setName("regexRadioButton");
        regexRadioButton.setSelected(false);

        enabledCheckbox.setName("enabledCheckbox");

        Dimension toggleSize = new Dimension(60, 30);
        andOrToggle.setMinimumSize(toggleSize);
        andOrToggle.setMaximumSize(toggleSize);
        andOrToggle.setPreferredSize(toggleSize);

        add(enabledCheckbox);
        add(andOrToggle);
        add(quickLevelFilterCombo);
        add(quickFilterTextField);
        add(customFiltersPanel);
        add(regexRadioButton, "alignx center");

    }

    public void bind(QuickFilterHistoryController quickFilterHistoryController,
                     final QuickFilterModel model,
                     final ObservableProperty<Boolean> isAndFilter) {
        this.model = model;

        quickFilterTextField.bind(quickFilterHistoryController);

        isAndFilter.addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean isAnd) {
                if (isAnd) {
                    andOrToggle.setText("and");
                } else {
                    andOrToggle.setText("or");
                }
            }
        });

        andOrToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isAndFilter.set(!isAndFilter.get().booleanValue());
            }
        });

        Binder.bind(model.getIsEnabled(), enabledCheckbox);
        Binder.bind(model.getFilterText(), quickFilterTextField);
        Binder.bind(model.getIsRegex(), regexRadioButton);

        quickLevelFilterCombo.bind(model.getLevelFilter().get());

        model.getCustomFilters().addListListenerAndNotifyExisting(new ObservableListListener<CustomQuickFilterModel>() {
            @Override
            public void onItemAdded(CustomQuickFilterModel customQuickFilterModel) {
                addCustomFilter(customQuickFilterModel);
            }

            @Override
            public void onItemRemoved(CustomQuickFilterModel customQuickFilterModel) {

            }
        });

    }

    private void addCustomFilter(final CustomQuickFilterModel customQuickFilterModel) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JLabel label = new JLabel(customQuickFilterModel.getLabel().get());

                JTextField filter = new JTextField();

                Binder2 binder = new Binder2();
                binder.bind(customQuickFilterModel.getValue(), filter);

                Dimension dimension = new Dimension(customQuickFilterModel.getWidth().get(), 16);
                filter.setPreferredSize(dimension);
                filter.setMinimumSize(dimension);

                customFiltersPanel.add(label);
                customFiltersPanel.add(filter);

                customFiltersPanel.doLayout();
            }
        });

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
            setLayout(new MigLayout(layoutConstraints, "[][grow,fill][fill][]", "[grow,fill]"));
            revalidate();
            doLayout();
        } else {
            removeAll();
            setLayout(new MigLayout(layoutConstraints, "[][][][grow,fill][fill][]", "[grow,fill]"));
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
