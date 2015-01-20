package com.logginghub.logging.frontend.components;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import com.logginghub.utils.logging.Logger;

public class QuickFilterPopupPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private QuickFilterHistoryTableModel model = new QuickFilterHistoryTableModel();
    private QuickFilterHistoryTable quickFilterHistoryTable;

    private static final Logger logger = Logger.getLoggerFor(QuickFilterPopupPanel.class);
    private QuickFilterHistoryController controller;

    public QuickFilterPopupPanel() {
        setName("quickFilterPopupPanel");
        setLayout(new MigLayout("", "[grow]", "[grow]"));
        setBorder(BorderFactory.createLineBorder(Color.black));

        quickFilterHistoryTable = new QuickFilterHistoryTable(model);
        quickFilterHistoryTable.setName("quickFilterHistoryTable");

        quickFilterHistoryTable.addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}

            @Override public void keyReleased(KeyEvent e) {}

            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int selectedRow = quickFilterHistoryTable.getSelectedRow();
                    logger.debug("Enter pressed on table, row {} selected", selectedRow);
                    controller.selectItem(model.getEntryAtRow(selectedRow));
                }
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    controller.clearSelection();
                }
            }
        });

        quickFilterHistoryTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int selectedRow = quickFilterHistoryTable.getSelectedRow();
                int columnAtPoint = quickFilterHistoryTable.columnAtPoint(e.getPoint());
                QuickFilterHistoryEntryModel entryAtRow = model.getEntryAtRow(selectedRow);
                if (columnAtPoint == 1) {
                    logger.debug("Mouse clicked on table, row {} on the delete column", selectedRow);

                    int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete '" + entryAtRow.getCommand().get() + "'", "Double checking", JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.YES_OPTION) {
                        controller.deleteItem(entryAtRow);
                    }
                }
                else {
                    logger.debug("Mouse clicked on table, row {} selected", selectedRow);
                    controller.selectItem(entryAtRow);
                }
            }
        });

        addFocusListener(new FocusListener() {
            @Override public void focusLost(FocusEvent e) {
                logger.debug("Panel focus lost");
                controller.clearSelection();
            }

            @Override public void focusGained(FocusEvent e) {
                logger.debug("Panel focus gained");
            }
        });

        quickFilterHistoryTable.addFocusListener(new FocusListener() {
            @Override public void focusLost(FocusEvent e) {
                logger.debug("Table focus lost");
                controller.clearSelection();
            }

            @Override public void focusGained(FocusEvent e) {
                logger.debug("Table focus gained");
            }
        });

        JScrollPane scrollPane = new JScrollPane(quickFilterHistoryTable);
        quickFilterHistoryTable.setTableHeader(null);
        add(scrollPane, "cell 0 0,grow");
    }

    public void bind(QuickFilterHistoryController controller) {
        this.controller = controller;
    }

    public void addEntry(QuickFilterHistoryEntryModel t) {
        model.addEntry(t);
    }

    public void removeEntry(QuickFilterHistoryEntryModel t) {
        model.removeEntry(t);
    }

    public void sortByUsage() {
        model.sortByUsage();
    }

    public void sortByTime() {
        model.sortByTime();
    }

}
