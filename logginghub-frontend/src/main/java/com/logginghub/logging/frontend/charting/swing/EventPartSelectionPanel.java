package com.logginghub.logging.frontend.charting.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.charting.model.StreamElementSelectionModel;

public class EventPartSelectionPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    class ListTransferHandler extends TransferHandler {

        /**
         * Perform the actual data import.
         */
        public boolean importData(TransferHandler.TransferSupport info) {
            String data = null;

            // If we can't handle the import, bail now.
            if (!canImport(info)) {
                return false;
            }

            JList list = (JList) info.getComponent();
            DefaultListModel model = (DefaultListModel) list.getModel();
            // Fetch the data -- bail if this fails
            try {
                data = (String) info.getTransferable().getTransferData(DataFlavor.stringFlavor);
            }
            catch (UnsupportedFlavorException ufe) {
                System.out.println("importData: unsupported data flavor");
                return false;
            }
            catch (IOException ioe) {
                System.out.println("importData: I/O exception");
                return false;
            }

            String[] split = data.split("::");
            JCheckBox checkBox = new JCheckBox(split[0]);
            checkBox.setSelected(Boolean.parseBoolean(split[1]));
            checkBox.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    processChange();
                }
            });

            if (info.isDrop()) { // This is a drop
                JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
                int index = dl.getIndex();
                if (dl.isInsert()) {
                    model.add(index, checkBox);
                    return true;
                }
                else {
                    model.set(index, checkBox);
                    return true;
                }
            }
            else { // This is a paste
                int index = list.getSelectedIndex();
                // if there is a valid selection,
                // insert data after the selection
                if (index >= 0) {
                    model.add(list.getSelectedIndex() + 1, checkBox);
                    // else append to the end of the list
                }
                else {
                    model.addElement(checkBox);
                }
                return true;
            }
        }

        /**
         * Bundle up the data for export.
         */
        protected Transferable createTransferable(JComponent c) {
            JList list = (JList) c;
            int index = list.getSelectedIndex();
            JCheckBox value = (JCheckBox) list.getSelectedValue();
            return new StringSelection(value.getText() + "::" + value.isSelected());
        }

        /**
         * The list handles both copy and move actions.
         */
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        /**
         * When the export is complete, remove the old list entry if the action was a move.
         */
        protected void exportDone(JComponent c, Transferable data, int action) {
            if (action != MOVE) {
                return;
            }
            JList list = (JList) c;
            DefaultListModel model = (DefaultListModel) list.getModel();
            int index = list.getSelectedIndex();
            model.remove(index);
        }

        /**
         * We only support importing strings.
         */
        public boolean canImport(TransferHandler.TransferSupport support) {
            // we only import Strings
            if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return false;
            }
            return true;
        }
    }

    private DefaultListModel list1Model = new DefaultListModel();
    private StreamElementSelectionModel model;

    public EventPartSelectionPanel() {
        setLayout(new MigLayout("fill", "[grow, fill]", "[fill]"));

        addCheckbox("Source Host", list1Model);
        addCheckbox("Source Application", list1Model);
        addCheckbox("Source IP", list1Model);        
        // TODO : other things

        CheckBoxList list = new CheckBoxList();
        list.setModel(list1Model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp1 = new JScrollPane(list);
        list.setDragEnabled(true);
        TransferHandler lh = new ListTransferHandler();
        list.setTransferHandler(lh);
        list.setDropMode(DropMode.INSERT);

        add(sp1);
    }

    private void addCheckbox(String string, DefaultListModel list1Model) {
        JCheckBox checkBox = new JCheckBox(string);
        checkBox.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                processChange();
            }
        });

        list1Model.addElement(checkBox);
    }

    public void bind(StreamElementSelectionModel model) {
        this.model = model;
    }

    protected void processChange() {

        model.getItems().clear();
        int size = list1Model.getSize();
        for (int i = 0; i < size; i++) {
            JCheckBox checkBox = (JCheckBox) list1Model.get(i);
            if (checkBox.isSelected()) {
                model.getItems().add(checkBox.getText());
            }
        }

    }

}
