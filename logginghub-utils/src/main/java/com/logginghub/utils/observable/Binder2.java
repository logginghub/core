package com.logginghub.utils.observable;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.logginghub.utils.Convertor;
import com.logginghub.utils.MutableBoolean;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.swing.DoubleSpinner;

public class Binder2 {

    private static final Logger logger = Logger.getLoggerFor(Binder2.class);
    private List<Runnable> unbinders = new ArrayList<Runnable>();

    public void unbind() {
        for (Runnable runnable : unbinders) {
            runnable.run();
        }
        unbinders.clear();
    }

    public void bindObject(final ObservableProperty<Object> observable, final JComboBox comboBox) {
        final ObservablePropertyListener<Object> listener = new ObservablePropertyListener<Object>() {
            public void onPropertyChanged(Object oldValue, Object newValue) {
                comboBox.setSelectedItem(newValue);
            }
        };
        observable.addListener(listener);

        final ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object selectedItem = comboBox.getSelectedItem();
                observable.set(selectedItem);
            }
        };
        comboBox.addActionListener(actionListener);

        unbinders.add(new Runnable() {
            public void run() {
                observable.removeListener(listener);
                comboBox.removeActionListener(actionListener);
            }
        });
    }

    public <T> void bind(final ObservableProperty<T> observable, final JComboBox comboBox) {

        comboBox.setSelectedItem(observable.get());

        final ObservablePropertyListener<T> listener = new ObservablePropertyListener<T>() {
            public void onPropertyChanged(T oldValue, T newValue) {
                comboBox.setSelectedItem(newValue);
            }
        };
        observable.addListener(listener);

        final ItemListener aListener = new ItemListener() {
            @SuppressWarnings("unchecked") public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Object selectedItem = comboBox.getSelectedItem();
                    observable.set((T) selectedItem);
                }
            }
        };
        comboBox.addItemListener(aListener);

        Runnable unbinder = new Runnable() {
            public void run() {
                observable.removeListener(listener);
                comboBox.removeItemListener(aListener);
            }
        };

        unbinders.add(unbinder);
    }

    public void bind(final ObservableInteger value, final JSpinner spinner) {
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

        throw new RuntimeException("No unbind support");
    }

    public void bind(final ObservableLong value, final JSpinner spinner) {
        spinner.setValue(value.longValue());

        value.addListener(new ObservablePropertyListener<Long>() {
            public void onPropertyChanged(Long oldValue, Long newValue) {
                spinner.setValue(newValue);
            }
        });

        spinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Object spinnerValue = spinner.getValue();
                if (spinnerValue instanceof Long) {
                    Long long1 = (Long) spinnerValue;
                    value.set(long1);
                }
                else if (spinnerValue instanceof Integer) {
                    Integer integer = (Integer) spinnerValue;
                    value.set(integer);
                }
            }
        });

        throw new RuntimeException("No unbind support");
    }

    public void bind(final ObservableDouble value, final DoubleSpinner spinner) {
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

        throw new RuntimeException("No unbind support");
    }

    public void bind(final ObservableProperty<Integer> value, final JSpinner spinner) {

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

        throw new RuntimeException("No unbind support");
    }

    public void bindMultiline(ObservableProperty<String> property, final JLabel label) {
        property.addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            public void onPropertyChanged(String oldValue, String newValue) {
                label.setText("<html>" + newValue + "</html>");
            }
        });

        throw new RuntimeException("No unbind support");
    }

    public void bindAsDate(ObservableProperty<Long> property, final JLabel label) {
        property.addListenerAndNotifyCurrent(new ObservablePropertyListener<Long>() {
            public void onPropertyChanged(Long oldValue, Long newValue) {
                label.setText(Logger.toDateString(newValue).toString());
            }
        });

        throw new RuntimeException("No unbind support");
    }

    public <T> void bind(final AbstractObservableProperty<T> property, final JLabel label) {

        final ObservablePropertyListener<T> listener = new ObservablePropertyListener<T>() {
            public void onPropertyChanged(T oldValue, T newValue) {
                label.setText(newValue != null ? newValue.toString() : "<null>");
            }
        };

        property.addListenerAndNotifyCurrent(listener);

        unbinders.add(new Runnable() {
            public void run() {
                property.removeListener(listener);
            }
        });
    }

    public <T> void bind(AbstractObservableProperty<T> property, final JLabel label, final LabelFormatter<T> formatter) {
        property.addListenerAndNotifyCurrent(new ObservablePropertyListener<T>() {
            public void onPropertyChanged(T oldValue, T newValue) {
                label.setText(newValue != null ? newValue.toString() : "<null>");
                formatter.format(label, newValue);
            }
        });

        throw new RuntimeException("No unbind support");
    }

    public void bind(final ObservableProperty<String> property, final JTextArea textArea) {
        textArea.setText(property.asString());

        final MutableBoolean internalUpdate = new MutableBoolean(false);

        final ObservablePropertyListener<String> listener = new ObservablePropertyListener<String>() {
            public void onPropertyChanged(String oldValue, String newValue) {
                if (!internalUpdate.value) {
                    internalUpdate.value = true;
                    textArea.setText(newValue);
                    internalUpdate.value = false;
                }
            }
        };
        property.addListener(listener);

        final KeyListener keyListener = new KeyListener() {
            String lastText = textArea.getText();

            public void keyTyped(final KeyEvent e) {}

            public void keyReleased(final KeyEvent e) {
                final String newText = textArea.getText();
                if (!newText.equals(lastText)) {
                    internalUpdate.value = true;
                    property.set(textArea.getText());
                    internalUpdate.value = false;
                    lastText = newText;
                }
            }

            public void keyPressed(final KeyEvent e) {}
        };
        textArea.addKeyListener(keyListener);

        // Need to add a document listener for code based changes
        final DocumentListener documentListener = new DocumentListener() {
            public void removeUpdate(DocumentEvent e) {
                internalUpdate.value = true;
                String text = textArea.getText();
                logger.debug("Updating text area '{}'", text);
                property.set(text);
                internalUpdate.value = false;
            }

            public void insertUpdate(DocumentEvent e) {
                internalUpdate.value = true;
                String text = textArea.getText();
                logger.debug("Updating text area '{}'", text);
                property.set(text);
                internalUpdate.value = false;
            }

            public void changedUpdate(DocumentEvent e) {
                internalUpdate.value = true;
                String text = textArea.getText();
                logger.debug("Updating text area '{}'", text);
                property.set(text);
                internalUpdate.value = false;
            }
        };
        textArea.getDocument().addDocumentListener(documentListener);

        Runnable unbinder = new Runnable() {
            public void run() {
                textArea.removeKeyListener(keyListener);
                textArea.getDocument().removeDocumentListener(documentListener);
                property.removeListener(listener);
            }
        };

        unbinders.add(unbinder);
    }

    public void bind(final ObservableProperty<String> property, final JTextField textField) {
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

        unbinders.add(unbinder);
    }

    public void bind(final ObservableDouble property, final JTextField textField) {

        if (Double.isNaN(property.doubleValue())) {
            // Show nothing
        }
        else {
            textField.setText(property.asString());
        }

        final MutableBoolean internalUpdate = new MutableBoolean(false);

        final ObservablePropertyListener<Double> listener = new ObservablePropertyListener<Double>() {
            public void onPropertyChanged(Double oldValue, Double newValue) {
                if (!internalUpdate.value) {
                    internalUpdate.value = true;
                    textField.setText(Double.toString(newValue));
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
                    try {
                        property.set(Double.parseDouble(textField.getText()));
                    }
                    catch (NumberFormatException e2) {
                        // Ignore badly parsed
                    }
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

        unbinders.add(unbinder);
    }

    public void bind(final ObservableInteger property, final JTextField textField) {
        bind(property, textField, new Convertor<String, ObservableInteger>() {
            public String convert(ObservableInteger y) {
                return y.asString();
            }
        });
    }

    public void bind(final ObservableInteger property, final JTextField textField, Convertor<String, ObservableInteger> convertor) {
        textField.setText(convertor.convert(property));

        final MutableBoolean internalUpdate = new MutableBoolean(false);

        final ObservablePropertyListener<Integer> listener = new ObservablePropertyListener<Integer>() {
            public void onPropertyChanged(Integer oldValue, Integer newValue) {
                if (!internalUpdate.value) {
                    internalUpdate.value = true;
                    textField.setText(Integer.toString(newValue));
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
                    property.set(Integer.parseInt(textField.getText()));
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

        unbinders.add(unbinder);
    }

    public void bind(final ObservableProperty<Boolean> booleanProperty, final JCheckBox checkbox) {
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

    public void bindEnabledState(ObservableProperty<Boolean> booleanProperty, final Component component) {
        booleanProperty.addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                component.setEnabled(newValue);
            }
        });
    }

    public void bind(final ObservableProperty<Boolean> booleanProperty, final JRadioButton radioButton) {
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

    public void addUnbinder(Runnable runnable) {
        unbinders.add(runnable);
    }

    public void attachListener(final Observable t, final ObservableListener observableListener) {
        t.addListener(observableListener);
        unbinders.add(new Runnable() {
            public void run() {
                t.removeListener(observableListener);
            }
        });
    }

    public <T> void attachPropertyListener(final AbstractObservableProperty<T> t, final ObservablePropertyListener<T> observableListener) {
        t.addListener(observableListener);
        unbinders.add(new Runnable() {
            public void run() {
                t.removeListener(observableListener);
            }
        });
    }

    public <T> void attachPropertyListenerAndNotifyCurrent(final AbstractObservableProperty<T> t,
                                                           final ObservablePropertyListener<T> observableListener) {
        t.addListenerAndNotifyCurrent(observableListener);
        unbinders.add(new Runnable() {
            public void run() {
                t.removeListener(observableListener);
            }
        });
    }

    public void addAdjustmentListener(final JScrollBar scrollBar, final AdjustmentListener adjustmentListener) {
        unbinders.add(new Runnable() {
            public void run() {
                scrollBar.removeAdjustmentListener(adjustmentListener);
            }
        });
        scrollBar.addAdjustmentListener(adjustmentListener);
    }

    public void addListenerAndNotifyCurrent(final ObservableLong observableLong, final ObservablePropertyListener<Long> observablePropertyListener) {
        unbinders.add(new Runnable() {
            public void run() {
                observableLong.removeListener(observablePropertyListener);
            }
        });
        observableLong.addListenerAndNotifyCurrent(observablePropertyListener);
    }

}
