package com.logginghub.logging.frontend.model;

import com.logginghub.logging.frontend.model.ObservableModel.FieldEnumeration;

public interface ObservableModelListener {
    void onFieldChanged(FieldEnumeration fe, Object value);
}
