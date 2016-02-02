package com.logginghub.logging.frontend.views.logeventdetail;

import com.logginghub.logging.filters.TimeFieldFilter;
import com.logginghub.logging.frontend.components.LevelsCheckboxListView;
import com.logginghub.logging.frontend.components.QuickFilterHistoryController;
import com.logginghub.logging.frontend.components.QuickFilterHistoryTextField;
import com.logginghub.logging.frontend.images.Icons;
import com.logginghub.logging.frontend.images.Icons.IconIdentifier;
import com.logginghub.logging.frontend.model.CustomDateFilterModel;
import com.logginghub.logging.frontend.model.CustomQuickFilterModel;
import com.logginghub.logging.frontend.model.LevelNamesModel;
import com.logginghub.logging.frontend.model.QuickFilterModel;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Binder;
import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class QuickFilterRowPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLoggerFor(QuickFilterRowPanel.class);
    private final JLabel clearFilters;

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
    private QuickFilterHistoryController controller;

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

        clearFilters = new JLabel(Icons.get(IconIdentifier.Delete));
        clearFilters.setToolTipText("Clear the filter values");
        clearFilters.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clearAllFilters();
            }
        });

        add(enabledCheckbox);
        add(andOrToggle);
        add(quickLevelFilterCombo);
        add(quickFilterTextField);
        add(customFiltersPanel);

        add(regexRadioButton, "alignx center");
        add(clearFilters);

    }

    private void clearAllFilters() {
        logger.info("Clearing filters");

        // TODO : this should be done in a controller
        model.getFilterText().set("");

        for (CustomDateFilterModel customDateFilter : model.getCustomDateFilters()) {
            customDateFilter.getValue().set(TimeFieldFilter.ACCEPT_ALL);
        }

        for (CustomQuickFilterModel customQuickFilterModel : model.getCustomFilters()) {
            customQuickFilterModel.getValue().set("");
        }
    }

    public void bind(QuickFilterHistoryController controller, final QuickFilterModel model, final ObservableProperty<Boolean> isAndFilter) {
        this.model = model;
        this.controller = controller;
        quickFilterTextField.bind(controller);

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

        model.getCustomFilters().addListenerAndNotifyCurrent(new ObservableListListener<CustomQuickFilterModel>() {
            @Override
            public void onAdded(CustomQuickFilterModel customQuickFilterModel) {
                addCustomFilter(customQuickFilterModel);
            }

            @Override
            public void onRemoved(CustomQuickFilterModel customQuickFilterModel, int index) {

            }

            @Override
            public void onCleared() {

            }
        });

        model.getCustomDateFilters().addListenerAndNotifyCurrent(new ObservableListListener<CustomDateFilterModel>() {
            @Override
            public void onAdded(CustomDateFilterModel customDateFilterModel) {
                addCustomDateFilter(customDateFilterModel);
            }

            @Override
            public void onRemoved(CustomDateFilterModel customDateFilterModel, int index) {

            }

            @Override
            public void onCleared() {

            }
        });

        // Bind to the show regex option
        controller.getEnvironmentModel().getShowRegexOptionOnQuickFilters().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                remove(regexRadioButton);
            }
        });

    }

    private void addCustomFilter(final CustomQuickFilterModel customQuickFilterModel) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                JComponent target;
                Binder2 binder = new Binder2();

                JLabel label = new JLabel(customQuickFilterModel.getLabel().get());
                if (customQuickFilterModel.getChoices().isEmpty()) {
                    JTextField filter = new JTextField();
                    target = filter;
                    binder.bind(customQuickFilterModel.getValue(), filter);
                } else {

                    DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
                    comboModel.addElement("");
                    for (String choice : customQuickFilterModel.getChoices()) {
                        comboModel.addElement(choice);
                    }
                    JComboBox comboBox = new JComboBox(comboModel);
                    target = comboBox;
                    binder.bind(customQuickFilterModel.getValue(), comboBox);
                }

                Dimension dimension = new Dimension(customQuickFilterModel.getWidth().get(), 16);
                target.setPreferredSize(dimension);
                target.setMinimumSize(dimension);

                customFiltersPanel.add(label);
                customFiltersPanel.add(target);

                customFiltersPanel.doLayout();
            }
        });

    }

    private void addCustomDateFilter(final CustomDateFilterModel customDateFilterModel) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JLabel label = new JLabel(customDateFilterModel.getLabel().get());

                final UtilDateModel model = new UtilDateModel();

                Calendar calendar = new GregorianCalendar();
                TimeUtils.clearHoursMinutesSecondsMillis(calendar);

                model.setValue(calendar.getTime());
                model.setSelected(true);
                JDatePanelImpl datePanel = new JDatePanelImpl(model);
                JDatePickerImpl filter = new JDatePickerImpl(datePanel, null);

                // Default to clear
                model.setValue(null);

                // TODO : process default value if there is one in the model

                final ObservablePropertyListener<Long> listener = new ObservablePropertyListener<Long>() {
                    @Override
                    public void onPropertyChanged(Long oldValue, Long newValue) {

                        if (newValue == TimeFieldFilter.ACCEPT_ALL) {
                            logger.info("Setting calendar value to NULL due to change in the model");
                            model.setValue(null);
                        } else {
                            logger.info("Setting calendar value to {} due to change in the model", newValue);
                            //                            Calendar calendar = new GregorianCalendar();
                            //                            calendar.setTimeInMillis(newValue);
                            //                            int year = calendar.get(Calendar.YEAR);
                            //                            int month = calendar.get(Calendar.MONTH);
                            //                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            //                            model.setDate(year, month, day);
                            model.setValue(new Date(newValue));
                        }
                    }
                };

                filter.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Date value = model.getValue();
                        logger.info("Action performed on hte date filter : {} {} {} {}", model.getDay(), model.getMonth(), model.getYear(), value);
                        if (value == null) {
                            // This is the "cleared" state - turn off the filter
                            logger.info("Setting date filter model value to TimeFieldFilter.ACCEPT_ALL due to change in the gui");
                            customDateFilterModel.getValue().set(TimeFieldFilter.ACCEPT_ALL);
                        } else {
                            // Apply
                            long time = value.getTime();
                            logger.info("Setting date filter model value to {} due to change in the gui", time);
                            customDateFilterModel.getValue().set(time);
                        }
                    }
                });

                customDateFilterModel.getValue().addListenerAndNotifyCurrent(listener);

                Binder2 binder = new Binder2();
                binder.addUnbinder(new Runnable() {
                    @Override
                    public void run() {
                        customDateFilterModel.getValue().removeListener(listener);
                    }
                });

                setDateFilterSizes(filter, customDateFilterModel.getWidth().get(), 16);


                customFiltersPanel.add(label);
                customFiltersPanel.add(filter);
                customFiltersPanel.doLayout();
            }

        });

    }

    private void setDateFilterSizes(JDatePickerImpl filter, int width, int height) {

        int buttonWidth = 20;

        // The default height is too small really, so add some fudge
        int adjustedHeight = height + 4;

        // jshaw - this is a bit of a hack based on knowing the order of the sub-components in the date control source code
        Dimension entireDimension = new Dimension(width, adjustedHeight);
        Dimension buttonDimension = new Dimension(buttonWidth, adjustedHeight);
        Dimension textDimension = new Dimension(width - buttonWidth - 1, adjustedHeight);


        filter.setPreferredSize(entireDimension);
        filter.setMinimumSize(entireDimension);
        filter.setSize(entireDimension);
        filter.setMaximumSize(entireDimension);

        filter.getComponent(0).setPreferredSize(textDimension);
        filter.getComponent(0).setMinimumSize(textDimension);
        filter.getComponent(0).setSize(textDimension);
        filter.getComponent(0).setMaximumSize(textDimension);

        filter.getComponent(1).setPreferredSize(buttonDimension);
        filter.getComponent(1).setMinimumSize(buttonDimension);
        filter.getComponent(1).setSize(buttonDimension);
        filter.getComponent(1).setMaximumSize(buttonDimension);

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
