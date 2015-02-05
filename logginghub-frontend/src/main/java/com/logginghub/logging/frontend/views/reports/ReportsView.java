package com.logginghub.logging.frontend.views.reports;

import com.logginghub.logging.messages.ReportDetails;
import com.logginghub.logging.messages.ReportExecuteResponse;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.observable.ObservableListListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by james on 04/02/15.
 */
public class ReportsView extends JPanel {
    private ReportsController controller;
    private ReportsModel model;

    private JPanel reportDetailsPanel;
    //    private JTable table;
    //    private ReportResultsTableModel reportResultsTableModel = new ReportResultsTableModel();

    private JPanel resultGridPanel;

    private int row = 0;

    public ReportsView() {
        setLayout(new MigLayout("fill", "[fill, grow]", "[fill, grow]"));

        reportDetailsPanel = new JPanel(new MigLayout("", "[fill]", "[fill][fill][fill][fill][fill][fill][fill][grow, fill]"));
        reportDetailsPanel.setBorder(BorderFactory.createTitledBorder("Available Reports"));

        resultGridPanel = new JPanel(new MigLayout("fill, gap 2", "[min!,fill][min!,fill][min!,fill][min!,fill][min!,fill][min!,fill][min!,fill][min!,fill][grow, fill]", "[min!, fill][grow, " +
                "fill]"));

        //        table = new JTable(reportResultsTableModel);
        //        int lines = 2;
        //        table.setRowHeight(table.getRowHeight() * lines);
        //
        //        table.setDefaultRenderer(String.class, new MultiLineCellRenderer());
        //        table.setDefaultRenderer(Object.class, new MultiLineCellRenderer());

        addHeaders();

        final JScrollPane scroller = new JScrollPane(resultGridPanel);
        scroller.getVerticalScrollBar().setUnitIncrement(100);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, reportDetailsPanel, scroller);

        //        add(reportDetailsPanel, "cell 0 0");
        //        add(scroller, "cell 1 0");
        add(splitPane, "cell 0 0");
    }

    private void addHeaders() {
        final JLabel name = new JLabel("Name");
        final JLabel command = new JLabel("Command");

        name.setFont(name.getFont().deriveFont(Font.BOLD));
        command.setFont(command.getFont().deriveFont(Font.BOLD));

        reportDetailsPanel.add(name);
        reportDetailsPanel.add(command);

        reportDetailsPanel.add(new JLabel(""), "wrap");
    }

    public void bind(final ReportsController controller) {
        this.controller = controller;
        this.model = controller.getModel();

        model.getResponses().addListenerAndNotifyCurrent(new ObservableListListener<ReportExecuteResponse>() {
            @Override public void onAdded(final ReportExecuteResponse reportExecuteResponse) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {

                        resultGridPanel.add(newJLabel(reportExecuteResponse.getInstanceKey().getEnvironment()), StringUtils.format("cell 0 {}", row));
                        resultGridPanel.add(newJLabel(reportExecuteResponse.getInstanceKey().getHost()), StringUtils.format("cell 1 {}", row));
                        resultGridPanel.add(newJLabel(reportExecuteResponse.getInstanceKey().getAddress()), StringUtils.format("cell 2 {}", row));
                        resultGridPanel.add(newJLabel(reportExecuteResponse.getInstanceKey().getInstanceType()), StringUtils.format("cell 3 {}", row));
                        resultGridPanel.add(newJLabel(reportExecuteResponse.getInstanceKey().getInstanceIdentifier()), StringUtils.format("cell 4 {}", row));
                        resultGridPanel.add(newJLabel("" + reportExecuteResponse.getInstanceKey().getPid()), StringUtils.format("cell 5 {}", row));
                        resultGridPanel.add(newJLabel(reportExecuteResponse.getResult().getState().toString()), StringUtils.format("cell 6 {}", row));

                        if (reportExecuteResponse.getResult().isSuccessful()) {
                            resultGridPanel.add(newJLabel("" + reportExecuteResponse.getResult().getValue().getReturnCode()), StringUtils.format("cell 7 {}", row));
                            final JTextArea text = new JTextArea(reportExecuteResponse.getResult().getValue().getResult());
                            text.setLineWrap(true);
                            resultGridPanel.add(text, StringUtils.format("cell 8 {}", row));
                        } else {
                            resultGridPanel.add(newJLabel(""), StringUtils.format("cell 7 {}", row));
                            resultGridPanel.add(newJLabel(""), StringUtils.format("cell 8 {}", row));
                        }

                        row++;
                        resultGridPanel.doLayout();
                    }
                });
            }

            @Override public void onRemoved(ReportExecuteResponse reportExecuteResponse, int index) {

            }

            @Override public void onCleared() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        resultGridPanel.removeAll();
                        addResultHeaders();
                    }
                });
                row = 0;
            }
        });

        model.getReportDetails().addListenerAndNotifyCurrent(new ObservableListListener<ReportDetails>() {
            @Override public void onAdded(final ReportDetails reportDetails) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        reportDetailsPanel.add(new JLabel(reportDetails.getName()));
                        reportDetailsPanel.add(new JLabel(reportDetails.getCommand()));

                        JButton button = new JButton("Request");
                        button.addActionListener(new ActionListener() {
                            @Override public void actionPerformed(ActionEvent e) {
                                controller.requestReport(reportDetails.getName());
                            }
                        });

                        reportDetailsPanel.add(button, "wrap");
                        reportDetailsPanel.doLayout();
                    }
                });
            }

            @Override public void onRemoved(ReportDetails reportDetails, int index) {

            }

            @Override public void onCleared() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        reportDetailsPanel.removeAll();
                        addHeaders();
                    }
                });
            }
        });
    }

    private void addResultHeaders() {
        row = 0;
        resultGridPanel.add(newJLabel2("Environment"), StringUtils.format("cell 0 {}", row));
        resultGridPanel.add(newJLabel2("Host"), StringUtils.format("cell 1 {}", row));
        resultGridPanel.add(newJLabel2("Address"), StringUtils.format("cell 2 {}", row));
        resultGridPanel.add(newJLabel2("Type"), StringUtils.format("cell 3 {}", row));
        resultGridPanel.add(newJLabel2("Instance"), StringUtils.format("cell 4 {}", row));
        resultGridPanel.add(newJLabel2("Port"), StringUtils.format("cell 5 {}", row));
        resultGridPanel.add(newJLabel2("Result"), StringUtils.format("cell 6 {}", row));
        resultGridPanel.add(newJLabel2("Exit code"), StringUtils.format("cell 7 {}", row));
        resultGridPanel.add(newJLabel2("Output"), StringUtils.format("cell 8 {}", row));
        row++;
    }

    private Component newJLabel(String s) {
        JLabel label = new JLabel(s);
        label.setVerticalAlignment(SwingConstants.TOP);
        label.setOpaque(true);
        label.setBackground(Color.white);
        //        label.setBorder(BorderFactory.createLineBorder(Color.black));
        return label;
    }

    private Component newJLabel2(String s) {
        JLabel label = new JLabel(s);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.lightGray);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

}

