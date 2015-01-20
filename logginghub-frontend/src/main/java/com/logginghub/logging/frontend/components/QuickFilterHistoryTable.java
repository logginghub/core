package com.logginghub.logging.frontend.components;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JTable;

public class QuickFilterHistoryTable extends JTable {
    private static final long serialVersionUID = 1L;
    private QuickFilterHistoryTableModel model;

    public QuickFilterHistoryTable(QuickFilterHistoryTableModel model) {
        super(model);
        this.model = model;
        
        getColumnModel().getColumn(1).setMaxWidth(20);
        
        
        addMouseListener(new MouseListener() {
            
            @Override public void mouseReleased(MouseEvent e) {}
            
            @Override public void mousePressed(MouseEvent e) {}
            
            @Override public void mouseExited(MouseEvent e) {}
            
            @Override public void mouseEntered(MouseEvent e) {}
            
            @Override public void mouseClicked(MouseEvent e) {}
        });
        
        addMouseMotionListener(new MouseMotionListener() {
            @Override public void mouseMoved(MouseEvent e) {
                int columnAtPoint = columnAtPoint(e.getPoint());
                if(columnAtPoint == 1) {
                    setCursor (Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }else{
                    setCursor (Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
            @Override public void mouseDragged(MouseEvent e) {}
        });
    }

    public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }
}
