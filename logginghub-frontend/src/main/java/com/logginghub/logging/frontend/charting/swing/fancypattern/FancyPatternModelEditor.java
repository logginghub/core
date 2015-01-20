package com.logginghub.logging.frontend.charting.swing.fancypattern;

import java.awt.Color;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.charting.swing.Event;
import com.logginghub.logging.frontend.charting.swing.EventHandler;
import com.logginghub.logging.frontend.charting.swing.fancypattern.FancyPatternElement.ElementState;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.observable.ObservablePropertyListener;
import com.logginghub.utils.swing.TestFrame;

public class FancyPatternModelEditor extends JPanel {

    private static final Logger logger = Logger.getLoggerFor(FancyPatternModelEditor.class);

    private FancyPatternEditorControl fancyPatternEditor = new FancyPatternEditorControl();
    private JTextArea pattern = new JTextArea();
    private JTextField patternNameTextField;

    private ObservableProperty<String> patternToEdit = new ObservableProperty<String>("");
    private Binder2 binder;

    public FancyPatternModelEditor() {
        setName("FancyPatternModelEditor");
        setLayout(new MigLayout("fill, ins 2", "[grow,fill]", "[grow,fill][grow,fill][]"));
        fancyPatternEditor.setBorder(new TitledBorder(null, "Select variable elements:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(fancyPatternEditor, "cell 0 0");

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
                                         "Once you are happy, choose a name and replace the group names with something useful:",
                                         TitledBorder.LEADING,
                                         TitledBorder.TOP,
                                         null,
                                         new Color(0, 0, 0)));
        add(panel, "cell 0 1,grow");
        panel.setLayout(new MigLayout("", "[][grow,fill]", "[][grow,fill]"));

        JLabel lblNewLabel_1 = new JLabel("Pattern name");
        panel.add(lblNewLabel_1, "cell 0 0,alignx trailing");

        patternNameTextField = new JTextField();
        patternNameTextField.setName("Pattern name");
        
        panel.add(patternNameTextField, "cell 1 0,growx");
        patternNameTextField.setColumns(10);

        JLabel lblNewLabel = new JLabel("Pattern ");
        panel.add(lblNewLabel, "cell 0 1");
        panel.add(pattern, "cell 1 1,alignx left,growy");

        pattern.setName("Pattern");
        pattern.setEditable(true);
        pattern.setLineWrap(true);

        pattern.setText("This is where something will go");

        fancyPatternEditor.getChangedEvent().addHandler(new EventHandler() {
            @Override public void onEvent(Event event) {

                StringBuilder builder = new StringBuilder();
                List<FancyPatternElement> elements = fancyPatternEditor.getElements();

                int currentGroup = -1;
                boolean inGroup = false;
                boolean numericGroup = false;

                for (FancyPatternElement element : elements) {

                    if (element.getElementState() == ElementState.raw) {

                        if (inGroup) {
                            // Group has ended
                            if (numericGroup) {
                                builder.append("}");
                            }
                            else {
                                builder.append("]");
                            }
                            inGroup = false;
                            currentGroup = -1;
                        }

                        builder.append(element.getText());

                    }
                    else {

                        int group = element.getGroup();

                        if (inGroup) {
                            if (currentGroup == -1 || currentGroup == group) {
                                // First group or still in the same group
                            }
                            else {
                                // Have switched to another group
                                if (numericGroup) {
                                    builder.append("}");
                                }
                                else {
                                    builder.append("]");
                                }
                                inGroup = false;
                            }
                        }

                        if (!inGroup) {
                            // New group
                            numericGroup = element.getElementState() == ElementState.numeric;
                            if (numericGroup) {
                                builder.append("{");
                            }
                            else {
                                builder.append("[");
                            }

                            builder.append("group").append(group);
                            currentGroup = group;
                            inGroup = true;
                        }
                    }
                }

                if (inGroup) {
                    if (numericGroup) {
                        builder.append("}");
                    }
                    else {
                        builder.append("]");
                    }
                }

                pattern.setText(builder.toString());
            }
        });
    }

    public FancyPatternEditorControl getFancyPatternEditor() {
        return fancyPatternEditor;
    }
    
    public void setRawEventText(String rawText) {
        fancyPatternEditor.setRawText(rawText);
    }

    public void bind(final PatternModel patternModel) {

        unbind();
        
        binder = new Binder2();

        final ObservablePropertyListener<String> fancyPatternUpdator = new ObservablePropertyListener<String>() {
            @Override public void onPropertyChanged(String oldValue, String newValue) {
                fancyPatternEditor.setEditedText(newValue);
            }
        };

        patternToEdit.addListenerAndNotifyCurrent(fancyPatternUpdator);
        // patternModel.getPattern().addListenerAndNotifyCurrent(fancyPatternUpdator);

        binder.bind(patternModel.getName(), patternNameTextField);
        binder.bind(patternModel.getPattern(), pattern);
        binder.addUnbinder(new Runnable() {
            public void run() {
                patternModel.getPattern().removeListener(fancyPatternUpdator);
            }
        });

    }

    public ObservableProperty<String> getPatternToEdit() {
        return patternToEdit;
    }

    public static void main(String[] args) {

        TestFrame.show(new FancyPatternModelEditor());
    }

    public boolean isEdited() {
        return false;

    }

    public void unbind() {
        if (binder != null) {
            binder.unbind();
            binder  = null;
        }
        
        pattern.setText("");
        patternNameTextField.setText("");
        fancyPatternEditor.setRawText("");
        
    }

    public void setEdited(boolean b) {}

    public void setCaretAtStart() {}

}
