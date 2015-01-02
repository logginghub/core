package com.logginghub.utils.observable;

import javax.swing.JLabel;

public interface LabelFormatter<T> {
    void format(JLabel label, T newValue);
}
