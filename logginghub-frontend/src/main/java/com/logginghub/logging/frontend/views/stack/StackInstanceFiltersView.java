package com.logginghub.logging.frontend.views.stack;

import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messages.StackTrace;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class StackInstanceFiltersView extends JPanel {

    private static final Logger logger = Logger.getLoggerFor(StackInstanceFiltersView.class);

    private final JButton addFilterButton;
    private final JButton andOrButton;

    private StackInstanceFiltersModel model;

    private List<MutlipleThreadViewFilterPanel> filterPanels = new ArrayList<MutlipleThreadViewFilterPanel>();

    private final JPanel filterPane;

    public StackInstanceFiltersView() {
        setLayout(new MigLayout("insets 1", "[grow,fill]", "[top][grow,fill]"));

        filterPane = new JPanel();
        filterPane.setLayout(new MigLayout("", "[grow,fill]", "[grow, fill]"));

        //        addFilter();
        add(filterPane, "top");

        JPanel buttonPane = new JPanel(new MigLayout());

        addFilterButton = new JButton("Add another filter");
        buttonPane.add(addFilterButton, "cell 4 0");

        andOrButton = new JButton("AND");
        buttonPane.add(andOrButton, "cell 5 0");
        andOrButton.setVisible(false);

        filterPane.add(buttonPane, "cell 0 1");
    }

    public void bind(final StackInstanceFiltersModel model) {
        this.model = model;

        Binder2 binder = new Binder2();

        binder.bind(model.getFilters(), new ObservableListListener<StackInstanceFilterModel>() {
            @Override public void onAdded(StackInstanceFilterModel stackInstanceFilterModel) {
                addFilterPanel(stackInstanceFilterModel);

                if(model.getFilters().size() > 1) {
                    andOrButton.setVisible(true);
                }
            }

            @Override public void onRemoved(StackInstanceFilterModel stackInstanceFilterModel, int index) {
                if(model.getFilters().size() < 2) {
                    andOrButton.setVisible(false);
                }
            }

            @Override public void onCleared() {
                andOrButton.setVisible(false);
            }
        });

        binder.bind(model.getAndMatch(), new ObservablePropertyListener<Boolean>() {
            @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    andOrButton.setText("AND");
                } else {
                    andOrButton.setText("OR");
                }
            }
        });

        andOrButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean andMatch = model.getAndMatch().get();
                andMatch = !andMatch;
                model.getAndMatch().set(andMatch);
            }
        });

        addFilterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.getFilters().add(new StackInstanceFilterModel());
                filterPane.invalidate();
                filterPane.revalidate();
                filterPane.doLayout();
            }
        });
    }

    private void addFilterPanel(StackInstanceFilterModel stackInstanceFilterModel) {
        MutlipleThreadViewFilterPanel filterPanel = new MutlipleThreadViewFilterPanel();
        filterPanel.bind(stackInstanceFilterModel);
        filterPanels.add(filterPanel);
        filterPane.add(filterPanel, "cell 0 0");
    }

    public List<MutlipleThreadViewFilterPanel> getFilterPanels() {
        return filterPanels;
    }

    public boolean passesFilter(StackTrace trace, StackSnapshot stackSnapshot) {

        boolean andMatch = model.getAndMatch().get();

        boolean passes = andMatch;

        for (MutlipleThreadViewFilterPanel filterPanel : filterPanels) {
            if (andMatch) {
                passes &= filterPanel.passesFilter(trace, stackSnapshot);
            }else{
                passes |= filterPanel.passesFilter(trace, stackSnapshot);
            }
        }

        return passes;

    }
}
