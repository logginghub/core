package com.logginghub.logging.frontend.model;

import com.logginghub.logging.filters.CompositeAndFilter;
import com.logginghub.logging.filters.FilterFactory;
import com.logginghub.logging.filters.LevelCheckFilter;
import com.logginghub.logging.filters.LogEventLevelFilter;
import com.logginghub.logging.filters.MultiValueOrFieldFilter;
import com.logginghub.logging.filters.MultipleEventContainsFilter;
import com.logginghub.logging.filters.SwitchingAndOrFilter;
import com.logginghub.logging.filters.TimeFieldFilter;
import com.logginghub.logging.frontend.components.LevelsCheckboxModel;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.observable.ObservablePropertyListener;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class QuickFilterController {

    private final FilterFactory filterFactory;
    private Map<QuickFilterModel, FilterWrapper> filterWrappersByQuickFilterModel = new HashMap<QuickFilterModel, FilterWrapper>();
    private SwitchingAndOrFilter filter = new SwitchingAndOrFilter();
    private EnvironmentModel environmentModel;
    private ObservableList<QuickFilterModel> quickFilterModels;
    private ObservableProperty<Boolean> isAndFilter = new ObservableProperty<Boolean>(true);

    public QuickFilterController(final EnvironmentModel environmentModel) {

        this.environmentModel = environmentModel;
        this.filterFactory = new FilterFactory(environmentModel.isFilterCaseSensitive(), environmentModel.isFilterUnicode());

        isAndFilter.addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                filter.setApplyAndLogic(newValue);
                fireRefilter();
            }
        });

        quickFilterModels = environmentModel.getQuickFilterModels();
        quickFilterModels.addListenerAndNotifyCurrent(new ObservableListListener<QuickFilterModel>() {

            @Override
            public void onAdded(QuickFilterModel t) {
                bindQuickFilter(t);
            }

            @Override
            public void onRemoved(QuickFilterModel t, int index) {
                FilterWrapper filterWrapper = filterWrappersByQuickFilterModel.remove(t);
                filter.removeFilter(filterWrapper.compositeFilter);
                fireRefilter();
            }

            @Override
            public void onCleared() {
            }
        });
    }

    private void fireRefilter() {
        environmentModel.getFilterUpdateCount().increment(1);
    }

    private void bindQuickFilter(QuickFilterModel t) {
        final FilterWrapper wrapper = new FilterWrapper();
        wrapper.messageFilter = new MultipleEventContainsFilter("", false, filterFactory);
        wrapper.severityFilter = new LogEventLevelFilter(0);
        wrapper.specificLevelFilter = new LevelCheckFilter();

        wrapper.compositeFilter = new CompositeAndFilter(wrapper.messageFilter, wrapper.severityFilter, wrapper.specificLevelFilter);

        LevelsCheckboxModel levelsCheckboxModel = t.getLevelFilter().get();
        bind(levelsCheckboxModel, wrapper.specificLevelFilter);

        // Bind changes to the filter text to the actual filter, and then notify for a re-filter
        t.getFilterText().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override
            public void onPropertyChanged(String oldValue, String newValue) {
                wrapper.messageFilter.setEventContainsString(newValue);
                fireRefilter();
            }
        });

        // If any custom filters get added, wire these up in a similar way
        bindCustomFilters(t.getCustomFilters(), wrapper);
        bindCustomDateFilters(t.getCustomDateFilters(), wrapper);

        t.getIsRegex().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                wrapper.messageFilter.setUseRegex(newValue);
                fireRefilter();
            }
        });

        levelsCheckboxModel.getSelectedLevel().addListenerAndNotifyCurrent(new ObservablePropertyListener<Level>() {
            @Override
            public void onPropertyChanged(Level oldValue, Level newValue) {
                wrapper.severityFilter.setLevel(newValue.intValue());
                fireRefilter();
            }
        });

        t.getIsEnabled().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    filter.addFilter(wrapper.compositeFilter);

                } else {
                    filter.removeFilter(wrapper.compositeFilter);
                }

                fireRefilter();
            }
        });

        filterWrappersByQuickFilterModel.put(t, wrapper);
        fireRefilter();
    }

    protected void bind(LevelsCheckboxModel levelsCheckboxModel, final LevelCheckFilter specificLevelFilter) {
        // jshaw - today I will mostly be hating Matt Scott for making me implement this.
        levelsCheckboxModel.getSevereVisible().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                specificLevelFilter.setSevereAllowed(newValue);
            }
        });

        levelsCheckboxModel.getWarningVisible().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                specificLevelFilter.setWarningAllowed(newValue);
            }
        });

        levelsCheckboxModel.getInfoVisible().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                specificLevelFilter.setInfoAllowed(newValue);
            }
        });

        levelsCheckboxModel.getConfigVisible().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                specificLevelFilter.setConfigAllowed(newValue);
            }
        });

        levelsCheckboxModel.getFineVisible().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                specificLevelFilter.setFineAllowed(newValue);
            }
        });

        levelsCheckboxModel.getFinerVisible().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                specificLevelFilter.setFinerAllowed(newValue);
            }
        });

        levelsCheckboxModel.getFinestVisible().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                specificLevelFilter.setFinestAllowed(newValue);
            }
        });

    }

    private void bindCustomFilters(ObservableList<CustomQuickFilterModel> customFilters, final FilterWrapper wrapper) {
        customFilters.addListenerAndNotifyCurrent(new ObservableListListener<CustomQuickFilterModel>() {
            @Override
            public void onAdded(CustomQuickFilterModel customQuickFilterModel) {
                MultiValueOrFieldFilter fieldFilter;
                MultiValueOrFieldFilter.Field field;

                MultiValueOrFieldFilter.Type type = MultiValueOrFieldFilter.Type.valueOf(StringUtils.capitalise(customQuickFilterModel.getType().get()));
                String value = customQuickFilterModel.getValue().get();
                String fieldName = customQuickFilterModel.getField().get();

                try {
                    field = MultiValueOrFieldFilter.Field.valueOf(StringUtils.capitalise(fieldName));
                    fieldFilter = new MultiValueOrFieldFilter(field, type, value, false);
                } catch (IllegalArgumentException e) {
                    fieldFilter = new MultiValueOrFieldFilter(fieldName, type, value, false);
                }

                final MultiValueOrFieldFilter finalFilter = fieldFilter;

                wrapper.compositeFilter.addFilter(fieldFilter);

                customQuickFilterModel.getValue().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
                    @Override
                    public void onPropertyChanged(String oldValue, String newValue) {
                        finalFilter.setValue(newValue);
                        fireRefilter();
                    }
                });

            }

            @Override
            public void onRemoved(CustomQuickFilterModel customQuickFilterModel, int index) {

            }

            @Override
            public void onCleared() {

            }

        });
    }

    private void bindCustomDateFilters(ObservableList<CustomDateFilterModel> customDateFilters, final FilterWrapper wrapper) {
        customDateFilters.addListenerAndNotifyCurrent(new ObservableListListener<CustomDateFilterModel>() {
            @Override
            public void onAdded(CustomDateFilterModel customDateFilterModel) {
                TimeFieldFilter fieldFilter;
                TimeFieldFilter.Field field;

                TimeFieldFilter.Type type = TimeFieldFilter.Type.valueOf(StringUtils.capitalise(customDateFilterModel.getType().get()));
                long value = customDateFilterModel.getValue().get();
                String fieldName = customDateFilterModel.getField().get();

                try {
                    field = TimeFieldFilter.Field.valueOf(StringUtils.capitalise(fieldName));
                    fieldFilter = new TimeFieldFilter(field, type, value);
                } catch (IllegalArgumentException e) {
                    fieldFilter = new TimeFieldFilter(fieldName, type, value);
                }

                final TimeFieldFilter finalFilter = fieldFilter;

                wrapper.compositeFilter.addFilter(fieldFilter);

                customDateFilterModel.getValue().addListenerAndNotifyCurrent(new ObservablePropertyListener<Long>() {
                    @Override
                    public void onPropertyChanged(Long oldValue, Long newValue) {
                        finalFilter.setValue(newValue);
                        fireRefilter();
                    }
                });
            }

            @Override
            public void onRemoved(CustomDateFilterModel customQuickFilterModel, int index) {

            }

            @Override
            public void onCleared() {

            }

        });
    }

    public SwitchingAndOrFilter getFilter() {
        return filter;
    }

    public ObservableProperty<Boolean> getIsAndFilter() {
        return isAndFilter;
    }

    class FilterWrapper {
        public LogEventLevelFilter severityFilter;
        public MultipleEventContainsFilter messageFilter;
        public CompositeAndFilter compositeFilter;
        public LevelCheckFilter specificLevelFilter;
    }

}
