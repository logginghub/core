package com.logginghub.logging.frontend.components;

import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservablePropertyListener;
import com.logginghub.utils.swing.TestFrame;

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class QuickFilterHistoryTextField extends JTextField {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLoggerFor(QuickFilterHistoryTextField.class);

    private QuickFilterPopupPanel popupPanel = new QuickFilterPopupPanel();
    private JDialog popup;
    private QuickFilterHistoryModel model;

    private Deque<UndoEntry> undoStack = new LinkedList<UndoEntry>();
    private Deque<UndoEntry> redoStack = new LinkedList<UndoEntry>();

    private QuickFilterHistoryController controller;

    private Border originalBorder;

    public QuickFilterHistoryTextField() {
        setName("quickFilterTestField");
        originalBorder = getBorder();

        addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}

            @Override public void keyReleased(KeyEvent e) {}

            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    logger.debug("Down pressed");
                    showCommandHistory();
                }
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    logger.debug("Escape pressed");
                    hideCommandHistory();
                }
                else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    logger.debug("Enter pressed");
                    saveCommand();
                    flashBorder();
                }
                else if (e.getKeyCode() == KeyEvent.VK_Z) {
                    if (e.isControlDown()) {
                        logger.debug("Undo pressed");
                        undo();
                    }
                }
                else if (e.getKeyCode() == KeyEvent.VK_Y) {
                    if (e.isControlDown()) {
                        logger.debug("Redo pressed");
                        redo();
                    }
                }
                else if (e.getKeyCode() == KeyEvent.VK_R) {
                    if (e.isControlDown()) {
                        logger.debug("Sort by time pressed");
                        sortByTime();
                    }
                }
                else if (e.getKeyCode() == KeyEvent.VK_U) {
                    if (e.isControlDown()) {
                        logger.debug("Sort by usage pressed");
                        sortByUsage();
                    }
                }
                else if (e.getKeyCode() == KeyEvent.VK_C) {
                    if (e.isControlDown()) {
                        logger.debug("Clear filter pressed");
                        clearFilter();
                    }
                }
                else {
                    logger.debug("Key pressed '{}' isAction {} isPrintable {}", e.getKeyChar(), e.isActionKey(), isPrintableChar(e.getKeyChar()));
                    if (isPrintableChar(e.getKeyChar()) || e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
                        String text = getText();
                        storeUndo(text);
                    }
                }
            }

        });
    }

    protected void clearFilter() {
        String text = getText();
        storeUndo(text);
        setText("");
    }

    protected void sortByTime() {
        popupPanel.sortByTime();
        showCommandHistory();
    }

    protected void sortByUsage() {
        popupPanel.sortByUsage();
        showCommandHistory();
    }

    protected void flashBorder() {
        PulsingBorder border = new PulsingBorder(Color.DARK_GRAY, 2);
        setBorder(border);
        pulse(border, originalBorder, Color.GREEN, 500);
    }

    public void pulse(final PulsingBorder border, final Border originalBorder, final Color toColour, long intervalMS) {

        Color lineColor = border.getLineColor();
        final Color start = new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue());

        final int updates = (int) (intervalMS / 10);

        final Timer timer = new Timer(true);

        timer.schedule(new TimerTask() {

            int currentTick = 0;

            // TODO : pulse up then pulse down
            // int mode = 0;

            @Override public void run() {
                border.setLineColor(interpolate(start, toColour, currentTick / (float) updates));
                repaint();
                currentTick++;

                if (currentTick == updates) {
                    timer.cancel();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            setBorder(originalBorder);
                        }
                    });
                }
            }
        }, 10, 10);

    }

    public Color interpolate(Color a, Color b, float factor) {
        int newRed = (int) (a.getRed() - (a.getRed() - b.getRed()) * factor);
        int newGreen = (int) (a.getGreen() - (a.getGreen() - b.getGreen()) * factor);
        int newBlue = (int) (a.getBlue() - (a.getBlue() - b.getBlue()) * factor);

        return new Color(newRed, newGreen, newBlue);
    }

    public boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) && c != KeyEvent.CHAR_UNDEFINED && block != null && block != Character.UnicodeBlock.SPECIALS;
    }

    private void storeUndo(String text) {
        logger.debug("Storing undo for current command '{}'", text);
        undoStack.push(new UndoEntry(text));
        if (undoStack.size() > 100) {
            undoStack.pollLast();
        }

        redoStack.clear();
    }

    protected void redo() {
        if (redoStack.size() > 0) {
            UndoEntry pop = redoStack.pop();
            undoStack.push(new UndoEntry(getText()));
            String commandValue = pop.getCommandValue();
            logger.debug("Restoring command to '{}', redo stack has {} items", commandValue, redoStack.size());
            setText(commandValue);
        }
    }

    protected void undo() {
        if (undoStack.size() > 0) {
            UndoEntry pop = undoStack.pop();
            redoStack.push(new UndoEntry(getText()));
            String commandValue = pop.getCommandValue();
            logger.debug("Restoring command to '{}', stack has {} items", commandValue, undoStack.size());
            setText(commandValue);
        }
    }

    protected void saveCommand() {

        if (model != null) {
            // TODO : this should be moved into a controller...
            String command = getText();

            boolean unique = true;
            ObservableList<QuickFilterHistoryEntryModel> entries = model.getEntries();
            for (QuickFilterHistoryEntryModel entry : entries) {
                if (entry.getCommand().get().equals(command)) {
                    unique = false;
                    break;
                }
            }

            if (unique) {
                model.getEntries().add(new QuickFilterHistoryEntryModel(command, true));
            }

        }
    }

    protected void hideCommandHistory() {
        if (popup != null && popup.isVisible()) {
            popup.setVisible(false);
        }
    }

    protected void showCommandHistory() {

        if (model != null) {
            if (popup == null) {
                popup = new JDialog();
                popup.setName("quickFilterPopupDialog");
                popup.setUndecorated(true);
                popup.getContentPane().add(popupPanel);
                popupPanel.bind(controller);

                popup.addKeyListener(new KeyListener() {
                    @Override public void keyTyped(KeyEvent e) {}

                    @Override public void keyReleased(KeyEvent e) {}

                    @Override public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                            logger.debug("Escape pressed");
                            hideCommandHistory();
                        }
                    }
                });

                controller.getSelectedEntry().addListener(new ObservablePropertyListener<QuickFilterHistoryEntryModel>() {
                    @Override public void onPropertyChanged(QuickFilterHistoryEntryModel oldValue, QuickFilterHistoryEntryModel newValue) {
                        if (newValue == null) {
                            logger.debug("Selection was cleared, hiding popup");
                        }
                        else {
                            logger.debug("{} has been selected, hiding popup", newValue);
                            storeUndo(getText());
                            setText(newValue.getCommand().get());
                        }
                        hideCommandHistory();
                    }
                });
            }

            setPopupSize();
            setPopupPosition();
            popup.setVisible(true);
        }
    }

    private void setPopupSize() {
        popup.setSize(new Dimension(getWidth(), 100));
    }

    private void setPopupPosition() {
        Point locationOnScreen = getLocationOnScreen();
        locationOnScreen.y += getHeight() - 1;
        popup.setLocation(locationOnScreen);
    }

    public void bind(QuickFilterHistoryController controller) {
        this.controller = controller;
        this.model = controller.getModel();
        model.getEntries().addListenerAndNotifyExisting(new ObservableListListener<QuickFilterHistoryEntryModel>() {
            @Override public void onRemoved(QuickFilterHistoryEntryModel t, int index) {
                popupPanel.removeEntry(t);
            }

            @Override public void onCleared() {}

            @Override public void onAdded(QuickFilterHistoryEntryModel t) {
                popupPanel.addEntry(t);
            }
        });
    }

    public QuickFilterHistoryModel getModel() {
        return model;
    }

    public static void main(String[] args) {
        QuickFilterHistoryTextField field = new QuickFilterHistoryTextField();

        QuickFilterHistoryModel model = new QuickFilterHistoryModel();
        model.getEntries().add(new QuickFilterHistoryEntryModel("Command 1"));
        model.getEntries().add(new QuickFilterHistoryEntryModel("Command 2"));
        model.getEntries().add(new QuickFilterHistoryEntryModel("Command 3"));

        field.bind(new QuickFilterHistoryController(model));

        TestFrame.show(field, 300, 100);
    }

}
