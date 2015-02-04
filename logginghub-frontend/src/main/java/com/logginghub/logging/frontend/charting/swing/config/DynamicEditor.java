package com.logginghub.logging.frontend.charting.swing.config;

import com.logginghub.swingutils.MigPanel;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.Is;
import com.logginghub.utils.observable.AbstractObservableProperty;
import com.logginghub.utils.observable.Binder;
import com.logginghub.utils.observable.ObservableDouble;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.swing.DoubleSpinner;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;

public class DynamicEditor {

    public static JPanel createEditor(Object observable) {
        MigPanel panel = new MigPanel("fill", "[shrink][grow, fill]", "");
        try {
            populateDynamicEditor(panel, observable);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return panel;
    }

    public static void populateDynamicEditor(JPanel panel, Object observable) throws Exception {

        Field[] fields = observable.getClass().getDeclaredFields();
        for (Field field : fields) {

            Class<?> type = field.getType();
            if (AbstractObservableProperty.class.isAssignableFrom(type)) {
                field.setAccessible(true);
                AbstractObservableProperty<?> p = (AbstractObservableProperty<?>) field.get(observable);

                Class<?> objectClass;

                if (p instanceof ObservableInteger) {
                    objectClass = Integer.class;
                }
                else if (p instanceof ObservableDouble) {
                    objectClass = Double.class;
                }
                else {
                    Object object = p.get();

                    if (object == null) {
                        throw new FormattedRuntimeException("Field '{}' of class '{}' was an observable property, but get() return null - so we have no clue what kind of class it is. Please make sure it has a default value!",
                                                            field.getName(),
                                                            observable.getClass().getName());

                    }
                    else {
                        objectClass = object.getClass();
                    }
                }

                JLabel label = new JLabel(field.getName());

                JComponent editor;
                if (objectClass == Boolean.class) {
                    ObservableProperty<Boolean> property = (ObservableProperty<Boolean>) p;
                    JCheckBox checkEditor = new JCheckBox();
                    Binder.bind(property, checkEditor);
                    editor = checkEditor;

                }
                else if (objectClass == Integer.class) {
                    ObservableInteger property = (ObservableInteger) p;
                    JSpinner checkEditor = new JSpinner();
                    Binder.bind(property, checkEditor);
                    editor = checkEditor;
                }
                else if (objectClass == Long.class) {
                    ObservableLong property = (ObservableLong) p;
                    JSpinner checkEditor = new JSpinner();
                    Binder.bind(property, checkEditor);
                    editor = checkEditor;
                }
                else if (objectClass == String.class) {
                    ObservableProperty<String> property = (ObservableProperty<String>) p;
                    JTextField textField = new JTextField(property.get());
                    Binder.bind(property, textField);
                    editor = textField;
                }
                else if (objectClass == Double.class) {
                    ObservableDouble property = (ObservableDouble) p;
                    DoubleSpinner doubleSpinner = new DoubleSpinner();
                    Binder.bind(property, doubleSpinner);
                    editor = doubleSpinner;
                }
                else {
                    throw new FormattedRuntimeException("Field '{}' of class '{}' has object class '{}' - we dont know how to bind that :(",
                                                        field.getName(),
                                                        observable.getClass().getName(),
                                                        objectClass.getName());

                }

                label.setLabelFor(editor);
                panel.add(label);
                panel.add(editor, "wrap");

            }
            else if (ObservableList.class.isAssignableFrom(type)) {
                field.setAccessible(true);
                ObservableList<?> list = (ObservableList<?>) field.get(observable);
                buildListEditor(field.getName(), panel, list);
            }
        }
    }

    private static void buildListEditor(String name, JPanel panel, final ObservableList list) {
        final Class<?> contentClass = list.getContentClass();
        Is.notNull(contentClass, "You must set the context class on an ObservableList for it to be dynamically editable");

        JPanel subPanel = new MigPanel("fill", "", "");

        final DefaultListModel listModel = new DefaultListModel();

        list.addListenerAndNotifyCurrent(new ObservableListListener<Object>() {

            @Override public void onAdded(Object t) {
                listModel.addElement(t);
            }

            @Override public void onRemoved(Object t, int index) {
                listModel.removeElement(t);
            }

            @Override public void onCleared() {

            }
        });

        final JList jlist = new JList(listModel);
        JScrollPane jScrollPane = new JScrollPane(jlist);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                final JDialog dialog = new JDialog();

                MigPanel panel = new MigPanel("fill", "[shrink][grow,fill]", "[]");

                try {
                    final Object newInstance = contentClass.newInstance();
                    populateDynamicEditor(panel, newInstance);
                    dialog.add(panel);

                    JButton okButton = new JButton("OK");
                    okButton.addActionListener(new ActionListener() {
                        @Override public void actionPerformed(ActionEvent e) {
                            list.add(newInstance);
                            dialog.dispose();
                        }
                    });

                    JButton cancelButton = new JButton("Cancel");
                    cancelButton.addActionListener(new ActionListener() {
                        @Override public void actionPerformed(ActionEvent e) {
                            dialog.dispose();
                        }
                    });

                    panel.add(okButton);
                    panel.add(cancelButton);

                    dialog.getRootPane().setDefaultButton(okButton);
                    dialog.pack();
                    dialog.setResizable(false);
                    dialog.setAlwaysOnTop(true);
                    dialog.setLocationRelativeTo(panel);
                    dialog.setModal(true);
                    dialog.setVisible(true);
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                Object selectedValue = jlist.getSelectedValue();
                list.remove(selectedValue);
            }
        });

        subPanel.add(addButton, "cell 0 0");
        subPanel.add(removeButton, "cell 0 0, wrap");
        subPanel.add(jScrollPane);

        panel.add(new JLabel(name));
        panel.add(subPanel);
    }
}
