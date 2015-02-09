package com.logginghub.logging.frontend.views.reports;

import com.logginghub.logging.messages.InstanceKey;
import com.logginghub.utils.WildcardOrRegexMatcher;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.observable.ObservablePropertyListener;

import java.util.regex.PatternSyntaxException;

/**
 * Created by james on 04/02/15.
 */
public class InstanceFilterModel extends Observable {

    private ObservableProperty<String> instanceFilter = createStringProperty("instanceFilter", "*.*.*.*");
    private ObservableProperty<Boolean> instanceFilterIsRegex = createBooleanProperty("instanceFilterIsRegex", false);
    private ObservableProperty<Boolean> instanceFilterIsOK = createBooleanProperty("instanceFilterIsOK", false);

    private ObservableProperty<String> pidFilter = createStringProperty("pidFilter", "*");
    private ObservableProperty<Boolean> pidFilterIsRegex = createBooleanProperty("pidFilterIsRegex", false);
    private ObservableProperty<Boolean> pidFilterIsOK = createBooleanProperty("pidFilterIsOK", false);

    public ObservableProperty<Boolean> getInstanceFilterIsRegex() {
        return instanceFilterIsRegex;
    }

    public ObservableProperty<String> getInstanceFilter() {
        return instanceFilter;
    }

    public ObservableProperty<Boolean> getInstanceFilterIsOK() {return instanceFilterIsOK;}

    public ObservableProperty<Boolean> getPidFilterIsOK() {return pidFilterIsOK;}

    public ObservableProperty<Boolean> getPidFilterIsRegex() {return pidFilterIsRegex;}

    public ObservableProperty<String> getPidFilter() {return pidFilter;}

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Non-observable stuff - can't decide whether this should be in the model, the view or the controller!
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    private WildcardOrRegexMatcher instanceMatcher;
    private WildcardOrRegexMatcher pidMatcher;

    public InstanceFilterModel() {
        instanceFilter.addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override public void onPropertyChanged(String oldValue, String newValue) {
                updateInstanceFilter();
            }
        });

        instanceFilterIsRegex.addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                updateInstanceFilter();
            }
        });

        pidFilter.addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override public void onPropertyChanged(String oldValue, String newValue) {
                updatePIDFilter();
            }
        });

        pidFilterIsRegex.addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                updatePIDFilter();
            }
        });
    }

    public boolean passes(InstanceKey instanceKey) {
        boolean passes = true;
        passes &= instanceMatcher.matches(instanceKey.buildKey());
        if(passes) {
            passes &= pidMatcher.matches(Integer.toString(instanceKey.getPid()));
        }
        return passes;
    }

    private void updatePIDFilter() {
        try {
            pidMatcher = new WildcardOrRegexMatcher(pidFilter.get(), pidFilterIsRegex.get());
            pidFilterIsOK.set(true);
        } catch (PatternSyntaxException pse) {
            pidFilterIsOK.set(false);
        }
    }

    private void updateInstanceFilter() {
        try {
            instanceMatcher = new WildcardOrRegexMatcher(instanceFilter.get(), instanceFilterIsRegex.get());
            instanceFilterIsOK.set(true);
        } catch (PatternSyntaxException pse) {
            instanceFilterIsOK.set(false);
        }
    }
}
