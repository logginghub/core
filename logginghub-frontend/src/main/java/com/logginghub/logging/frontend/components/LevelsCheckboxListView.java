package com.logginghub.logging.frontend.components;

import com.logginghub.logging.frontend.model.LevelNamesModel;
import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.OSUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.plaf.metal.MetalComboBoxIcon;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LevelsCheckboxListView extends JPanel {
    private final LevelNamesModel levelNamesModel;
    private JLabel xlabel;
    private Dimension preferredSize = new Dimension(150, 18);
    private JPopupMenu popupMenu;
    private boolean poppedUp;

    private static final Logger logger = Logger.getLoggerFor(LevelsCheckboxListView.class);

    private List<Row> rows = new ArrayList<Row>();
    private Map<Level, Row> rowsByLevel = new HashMap<Level, Row>();
    private LevelsCheckboxModel model;
    
    private Level selectedLevel;

    public static class Row extends JPanel {

        private JCheckBox checkBox = new JCheckBox();
        private JLabel label = new JLabel();

        public Row(String labelText) {
            setLayout(new MigLayout("insets 0, gap 0, fill", "[fill][fill, grow]", "[fill]"));

            label.setText(labelText);

            add(checkBox, "cell 0 0");
            add(label, "cell 1 0");
        }
    }

    public LevelsCheckboxListView(LevelNamesModel levelNamesModel) {
        this.levelNamesModel = levelNamesModel;
        if(OSUtils.isMac()) {
            setLayout(new MigLayout("fill, inset 0, gap 0", "[grow,fill][fill, 20px:20:20px]", "[grow,fill]"));
        }else{
            setLayout(new MigLayout("fill, inset 0, gap 0", "[grow,fill][fill, 18px:18:18px]", "[grow,fill]"));
        }

        xlabel = new JLabel("New label");
        xlabel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        add(xlabel, "cell 0 0");

        JButton dropdownButton = new JButton("");
        dropdownButton.setFocusable(false);
        dropdownButton.setRolloverEnabled(false);
        dropdownButton.setMargin(new Insets(0, 0, 0, 0));
        add(dropdownButton, "cell 1 0");

        popupMenu = new JPopupMenu();

        JPanel list = new JPanel();
        list.setLayout(new MigLayout("insets 0, gap 0, fill", "[fill,grow]", ""));

        Level[] levels = new Level[] { Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST };

        for (final Level level : levels) {
            String levelName = levelNamesModel.getLevelName(level.intValue());
            if(StringUtils.isNotNullOrEmpty(levelName)) {
                final Row row = new Row(levelName);

                row.label.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // logger.info("Clicked " + level);
                        handleClick(level);
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        for (Row otherRow : rows) {
                            removeHightlightFromRow(otherRow);
                        }

                        highlightRow(row);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        removeHightlightFromRow(row);
                    }

                });

                list.add(row, "wrap");
                rows.add(row);
                rowsByLevel.put(level, row);
            }
        }
        popupMenu.add(list);

        int allRows = 7;
        int heightForAllRows = 176;
        int height = (int) (heightForAllRows * (rows.size() / (double)allRows));

        popupMenu.setPreferredSize(new Dimension(150, height));

        dropdownButton.setIcon(new MetalComboBoxIcon());
        dropdownButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                changePopupState();
            }
        });

        addMouseListener(new MouseListener() {
            @Override public void mouseReleased(MouseEvent e) {}

            @Override public void mousePressed(MouseEvent e) {}

            @Override public void mouseExited(MouseEvent e) {}

            @Override public void mouseEntered(MouseEvent e) {}

            @Override public void mouseClicked(MouseEvent e) {
                changePopupState();
            }
        });
    }

    public void bind(final LevelsCheckboxModel model) {
        
        this.model = model;
        bindLevel(model.getSevereVisible(), Level.SEVERE);
        bindLevel(model.getWarningVisible(), Level.WARNING);
        bindLevel(model.getInfoVisible(), Level.INFO);
        bindLevel(model.getConfigVisible(), Level.CONFIG);
        bindLevel(model.getFineVisible(), Level.FINE);
        bindLevel(model.getFinerVisible(), Level.FINER);
        bindLevel(model.getFinestVisible(), Level.FINEST);
        
        model.getSelectedLevel().addListenerAndNotifyCurrent(new ObservablePropertyListener<Level>() {
            @Override public void onPropertyChanged(Level oldValue, final Level newValue) {
                selectLevel(newValue);
            }
        });
        
    }

    private void bindLevel(final ObservableProperty<Boolean> property, Level level) {
        Row row = rowsByLevel.get(level);
        if(row != null) {
            final JCheckBox checkBox = row.checkBox;
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    property.set(checkBox.isSelected());
                }
            });
            checkBox.setSelected(property.get());
        }
    }
    
    protected void handleClick(Level level) {
        logger.info("Level selected : {}", level);
        changePopupState();
        selectLevel(level);        
    }

    public void selectLevel(Level level) {
        selectedLevel = level;
        if(model != null) {
            model.getSelectedLevel().set(level);
        }
        
        updateSelectedLabel();
    }

    protected void changePopupState() {
        logger.info("Popup visible : {}", poppedUp);

        if (poppedUp) {
            logger.info("Hiding popup");
            popupMenu.setVisible(false);
            poppedUp = false;
        }
        else {
            resetBackgrounds();
            logger.info("Showing popup");
            poppedUp = true;
            popupMenu.show(this, 0, 24);
        }
    }

    private void resetBackgrounds() {
        for (Row row : rows) {

            if (row.label.getText().equals(getLevelName(selectedLevel))) {
                highlightRow(row);
            }
            else {
                removeHightlightFromRow(row);
            }
        }
    }

    private String getLevelName(Level level) {
        return levelNamesModel.getLevelName(level.intValue());
    }

    @Override public Dimension getPreferredSize() {
        return preferredSize;

    }

    private void highlightRow(final Row row) {                
        row.setBackground(ColourUtils.mildBlue);
        row.checkBox.setBackground(ColourUtils.mildBlue);
    }

    private void removeHightlightFromRow(final Row row) {
        row.setBackground(getBackground());
        row.checkBox.setBackground(getBackground());
    }
    
    public LevelsCheckboxModel getModel() {
        return model;
    }
    
    private void updateSelectedLabel() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                xlabel.setText(getLevelName(selectedLevel));
            }
        });
    }

    public Level getSelectedLevel() {
        return selectedLevel;
         
    }
}