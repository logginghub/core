package com.logginghub.logging.frontend.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.Metadata;

public class ObservableModel {

    private FactoryMap<FieldEnumeration, List<ObservableModelListener>> fieldListeners = new FactoryMap<ObservableModel.FieldEnumeration, List<ObservableModelListener>>() {
        private static final long serialVersionUID = 1L;
        protected List<ObservableModelListener> createEmptyValue(FieldEnumeration key) {
            return new CopyOnWriteArrayList<ObservableModelListener>();
        }
    };
    
    private List<ObservableModelListener> listeners = new CopyOnWriteArrayList<ObservableModelListener>();
    private Metadata metadata = new Metadata();

    public void addListener(ObservableModelListener listener) {
        listeners.add(listener);
    }
    
    public void addListener(FieldEnumeration field, ObservableModelListener listener) {
        fieldListeners.get(field).add(listener);
    }
    
    public void removeListener(FieldEnumeration field, ObservableModelListener listener) {
        fieldListeners.get(field).remove(listener);
    }

    public void removeListener(ObservableModelListener listener) {
        listeners.remove(listener);
    }

    public interface FieldEnumeration {};

    public Object getObject(FieldEnumeration fe) {
        return metadata.get(fe);
    }
    
    public String getString(FieldEnumeration fe) {
        return metadata.getString(fe);
    }
    
    @SuppressWarnings("unchecked") public <T> T get(FieldEnumeration fe) {
        return (T) metadata.get(fe);
    }

    public boolean getBoolean(FieldEnumeration fe) {
        return metadata.getBoolean(fe); 
    }

    @SuppressWarnings("unchecked") public <T extends Enum<?>> T getEnum(FieldEnumeration fe) {
        return (T) metadata.get(fe);
    }
    
    public int getInt(FieldEnumeration fe) {
        return metadata.getInt(fe);
    }
    
    public void set(FieldEnumeration fe, Object value) {
        metadata.put(fe, value);
        fireFieldChanged(fe, value);        
    }

    public void set(FieldEnumeration fe, int value) {
        metadata.put(fe, value);
        fireFieldChanged(fe, value);
    }
    
    private void fireFieldChanged(FieldEnumeration fe, Object value) {
        for (ObservableModelListener observableModelListener : listeners) {
            observableModelListener.onFieldChanged(fe, value);
        }
        
        List<ObservableModelListener> onlyIfExists = fieldListeners.getOnlyIfExists(fe);
        if(onlyIfExists != null){
            for (ObservableModelListener observableModelListener : onlyIfExists) {
                observableModelListener.onFieldChanged(fe, value);
            }
        }
    }
}
