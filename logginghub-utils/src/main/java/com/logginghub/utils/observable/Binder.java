package com.logginghub.utils.observable;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.logginghub.utils.MutableBoolean;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.swing.DoubleSpinner;

public class Binder {

    public static void bindObject(final ObservableProperty<Object> observable, final JComboBox comboBox) {
        observable.addListener(new ObservablePropertyListener<Object>() {
            public void onPropertyChanged(Object oldValue, Object newValue) {
                comboBox.setSelectedItem(newValue);
            }
        });

        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object selectedItem = comboBox.getSelectedItem();
                observable.set(selectedItem);
            }
        });
    }

    public static <T> void bind(final ObservableProperty<T> observable, final JComboBox comboBox) {

        comboBox.setSelectedItem(observable.get());

        observable.addListener(new ObservablePropertyListener<T>() {
            public void onPropertyChanged(T oldValue, T newValue) {
                comboBox.setSelectedItem(newValue);
            }
        });

        comboBox.addItemListener(new ItemListener() {
            @SuppressWarnings("unchecked") public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Object selectedItem = comboBox.getSelectedItem();
                    observable.set((T) selectedItem);
                }
            }
        });
    }

    public static void bind(final ObservableInteger value, final JSpinner spinner) {
        spinner.setValue(value.intValue());

        value.addListener(new ObservablePropertyListener<Integer>() {
            public void onPropertyChanged(Integer oldValue, Integer newValue) {
                spinner.setValue(newValue);
            }
        });

        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int newValue = (Integer) spinner.getValue();
                value.set(newValue);
            }
        });
    }
    
    public static void bind(final ObservableLong value, final JSpinner spinner) {
        spinner.setValue(value.longValue());

        value.addListener(new ObservablePropertyListener<Long>() {
            public void onPropertyChanged(Long oldValue, Long newValue) {
                spinner.setValue(newValue);
            }
        });

        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Object spinnerValue = spinner.getValue();
                if(spinnerValue instanceof Long) {
                    Long long1 = (Long) spinnerValue;
                    value.set(long1);
                }else if(spinnerValue instanceof Integer) {
                    Integer integer = (Integer) spinnerValue;
                    value.set(integer);
                }
            }
        });
    }
    
    public static void bind(final ObservableDouble value, final DoubleSpinner spinner) {
        spinner.setValue(value.doubleValue());

        value.addListener(new ObservablePropertyListener<Double>() {
            public void onPropertyChanged(Double oldValue, Double newValue) {
                spinner.setValue(newValue);
            }
        });

        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                double newValue = (Double) spinner.getValue();
                value.set(newValue);
            }
        });
    }

    public static void bind(final ObservableProperty<Integer> value, final JSpinner spinner) {

        spinner.setValue(value.asInt());

        value.addListener(new ObservablePropertyListener<Integer>() {
            public void onPropertyChanged(Integer oldValue, Integer newValue) {
                spinner.setValue(newValue);
            }
        });

        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int newValue = (Integer) spinner.getValue();
                value.set(newValue);
            }
        });
    }

    public static void bindMultiline(ObservableProperty<String> property, final JLabel label) {
        property.addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            public void onPropertyChanged(String oldValue, String newValue) {
                label.setText("<html>" + newValue + "</html>");
            }
        });
    }

    public static void bindAsDate(ObservableProperty<Long> property, final JLabel label) {
        property.addListenerAndNotifyCurrent(new ObservablePropertyListener<Long>() {
            public void onPropertyChanged(Long oldValue, Long newValue) {
                label.setText(Logger.toDateString(newValue).toString());
            }
        });
    }

    public static <T> void bind(AbstractObservableProperty<T> property, final JLabel label) {
        property.addListenerAndNotifyCurrent(new ObservablePropertyListener<T>() {
            public void onPropertyChanged(T oldValue, T newValue) {
                label.setText(newValue != null ? newValue.toString() : "<null>");
            }
        });
    }

    public static <T> void bind(AbstractObservableProperty<T> property, final JLabel label, final LabelFormatter<T> formatter) {
        property.addListenerAndNotifyCurrent(new ObservablePropertyListener<T>() {
            public void onPropertyChanged(T oldValue, T newValue) {
                label.setText(newValue != null ? newValue.toString() : "<null>");
                formatter.format(label, newValue);
            }
        });
    }

    public static Runnable bind(final ObservableProperty<String> property, final JTextField textField) {
        textField.setText(property.asString());

        final MutableBoolean internalUpdate = new MutableBoolean(false);

        final ObservablePropertyListener<String> listener = new ObservablePropertyListener<String>() {
            public void onPropertyChanged(String oldValue, String newValue) {
                if (!internalUpdate.value) {
                    internalUpdate.value = true;
                    textField.setText(newValue);
                    internalUpdate.value = false;
                }
            }
        };
        property.addListener(listener);

        final KeyListener keyListener = new KeyListener() {
            String lastText = textField.getText();

            public void keyTyped(final KeyEvent e) {}

            public void keyReleased(final KeyEvent e) {
                final String newText = textField.getText();
                if (!newText.equals(lastText)) {
                    internalUpdate.value = true;
                    property.set(textField.getText());
                    internalUpdate.value = false;
                    lastText = newText;
                }
            }

            public void keyPressed(final KeyEvent e) {}
        };
        textField.addKeyListener(keyListener);
        
        Runnable unbinder = new Runnable() {            
            public void run() {
                textField.removeKeyListener(keyListener);
                property.removeListener(listener);
            }
        };
        
        return unbinder;

    }

    public static void bind(final ObservableProperty<Boolean> booleanProperty, final JCheckBox checkbox) {
        checkbox.setSelected(booleanProperty.asBoolean());

        checkbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                booleanProperty.set(checkbox.isSelected());
            }
        });

        booleanProperty.addListener(new ObservablePropertyListener<Boolean>() {
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                checkbox.setSelected(newValue);
            }
        });
    }

    public static void bindEnabledState(ObservableProperty<Boolean> booleanProperty, final Component component) {
        booleanProperty.addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                component.setEnabled(newValue);
            }
        });
    }

    public static void bind(final ObservableProperty<Boolean> booleanProperty, final JRadioButton radioButton) {
        radioButton.setSelected(booleanProperty.asBoolean());

        radioButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                booleanProperty.set(radioButton.isSelected());
            }
        });

        booleanProperty.addListener(new ObservablePropertyListener<Boolean>() {
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                radioButton.setSelected(newValue);
            }
        });
    }

}
