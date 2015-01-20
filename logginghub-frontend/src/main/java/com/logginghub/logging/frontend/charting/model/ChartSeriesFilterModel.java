package com.logginghub.logging.frontend.charting.model;

import java.util.HashSet;
import java.util.Set;

import com.logginghub.utils.StringUtils;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.observable.ObservablePropertyListener;

public class ChartSeriesFilterModel extends Observable {

    private ObservableInteger variableIndex = createIntProperty("variableIndex", -1);
    private ObservableProperty<Boolean> enabled = createBooleanProperty("enabled", true);
    private ObservableProperty<String> blacklist = createStringProperty("blacklist", "");
    private ObservableProperty<String> whitelist = createStringProperty("whitelist", "");

    private Set<String> blacklistValues = new HashSet<String>();
    private Set<String> whitelistValues = new HashSet<String>();

    public ChartSeriesFilterModel() {
        whitelist.addListener(new ObservablePropertyListener<String>() {
            @Override public void onPropertyChanged(String oldValue, String newValue) {
                parseList(newValue, whitelistValues);
            }            
        });
        
        blacklist.addListener(new ObservablePropertyListener<String>() {
            @Override public void onPropertyChanged(String oldValue, String newValue) {
                parseList(newValue, blacklistValues);
            }            
        });
    }
    
    private void parseList(String text, Set<String> list) {
        list.clear();
        if (StringUtils.isNotNullOrEmpty(text)) {
            String[] split = text.split(",|\\r?\\n");
            for (String string : split) {
                list.add(string.trim());
            }
        }
    }

    public ObservableInteger getVariableIndex() {
        return variableIndex;
    }

    public ObservableProperty<Boolean> getEnabled() {
        return enabled;
    }

    public ObservableProperty<String> getBlacklist() {
        return blacklist;
    }

    public ObservableProperty<String> getWhitelist() {
        return whitelist;
    }

    
    public Set<String> getWhitelistValues() {
        return whitelistValues;
    }
    
    public Set<String> getBlacklistValues() {
        return blacklistValues;
    }
}
