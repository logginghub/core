package com.logginghub.logging.frontend.views.reports;

import com.logginghub.logging.messages.ReportDetails;
import com.logginghub.logging.messages.ReportExecuteResponse;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.DelayedAction;
import com.logginghub.utils.HTMLBuilder2;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.observable.ObservableItemContainer;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservableListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by james on 04/02/15.
 */
public class ReportsView extends JPanel {
    private final JButton copyCSVButton;
    private final JButton copyHTMLButton;
    private ReportsController controller;
    private ReportsModel model;

    private JPanel reportsView;
    private JPanel resultGridPanel;

    private int row = 0;
    private DelayedAction filterDelay = new DelayedAction(50, TimeUnit.MILLISECONDS);
    private InstanceFiltersView instanceFiltersView = new InstanceFiltersView();
    private InstanceFiltersModel instanceFiltersModel = new InstanceFiltersModel();

    private List<ReportExecuteResponse> allResults = new ArrayList<ReportExecuteResponse>();
    private List<ReportExecuteResponse> visibleResults = new ArrayList<ReportExecuteResponse>();
    private final JCheckBox trimWhitespace;

    public ReportsView() {
        setLayout(new MigLayout("fill", "[fill][fill, grow]", "[min!, fill][fill, grow][min!][min!]"));

        reportsView = new JPanel(new MigLayout("", "[fill]", "[fill][fill][fill][fill][fill][fill][fill][grow, fill]"));

        resultGridPanel = new JPanel(new MigLayout("fill, gap 2",
                "[min!,fill][min!,fill][min!,fill][min!,fill][min!,fill][min!,fill][min!,fill][min!,fill][grow, fill]",
                "[min!, fill][grow, " + "fill]"));

        instanceFiltersModel.getFilters().add(new InstanceFilterModel());
        instanceFiltersView.bind(instanceFiltersModel);
        instanceFiltersView.setBorder(BorderFactory.createTitledBorder("Instance Filters"));

        addHeaders();

        final JScrollPane resultsScroller = new JScrollPane(resultGridPanel);
        resultsScroller.getVerticalScrollBar().setUnitIncrement(100);
        resultsScroller.setBorder(BorderFactory.createTitledBorder("Results"));

        final JScrollPane reportsScroller = new JScrollPane(reportsView, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        reportsScroller.setBorder(BorderFactory.createTitledBorder("Available Reports"));

        add(instanceFiltersView, "cell 0 0, spanx 2");
        add(reportsScroller, "cell 0 1");
        add(resultsScroller, "cell 1 1, spany 3");

        trimWhitespace = new JCheckBox("Trim whitespace");
        add(trimWhitespace, "cell 0 2,spanx 2");

        copyCSVButton = new JButton("Copy CSV to clipboard");
        copyCSVButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copyCSVToClipboard();
            }
        });
        add(copyCSVButton, "cell 0 3");

        copyHTMLButton = new JButton("Copy HTML to clipboard");
        copyHTMLButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copyHTMLToClipboard();
            }
        });
        add(copyHTMLButton, "cell 0 3");
    }

    private void addHeaders() {
        final JLabel name = new JLabel("Name");
        final JLabel command = new JLabel("Command");

        name.setFont(name.getFont().deriveFont(Font.BOLD));
        command.setFont(command.getFont().deriveFont(Font.BOLD));

        reportsView.add(name);
        reportsView.add(command);

        reportsView.add(new JLabel(""), "wrap");
    }

    public void bind(final ReportsController controller) {
        this.controller = controller;
        this.model = controller.getModel();

        bindToResults();
        bindToReports(controller);

        Binder2 binder = new Binder2();
        binder.bind(model.getTrimWhitespsace(), trimWhitespace);

        instanceFiltersModel.addListener(new ObservableListener() {
            @Override public void onChanged(ObservableItemContainer observable, Object childPropertyThatChanged) {
                filterDelay.execute(new Runnable() {
                    @Override public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override public void run() {
                                updateFilters();
                            }
                        });
                    }
                });
            }
        });
    }

    private void updateFilters() {

        visibleResults.clear();

        for (ReportExecuteResponse result : allResults) {
            if (instanceFiltersModel.passesFilter(result.getInstanceKey())) {
                visibleResults.add(result);
            }
        }

        updateResultsView();
    }

    private void bindToReports(final ReportsController controller) {
        model.getReportDetails().addListenerAndNotifyCurrent(new ObservableListListener<ReportDetails>() {
            @Override public void onAdded(final ReportDetails reportDetails) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        reportsView.add(new JLabel(reportDetails.getName()));
                        reportsView.add(new JLabel(reportDetails.getCommand()));

                        JButton button = new JButton("Request");
                        button.addActionListener(new ActionListener() {
                            @Override public void actionPerformed(ActionEvent e) {
                                resetResults();
                                controller.requestReport(reportDetails.getName());
                            }
                        });

                        reportsView.add(button, "wrap");

                        // Need to re-layout everything in case the reports column needs to grow
                        invalidate();
                        revalidate();
                        doLayout();
                        repaint();
                    }
                });
            }

            @Override public void onRemoved(ReportDetails reportDetails, int index) {

            }

            @Override public void onCleared() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        reportsView.removeAll();
                        addHeaders();
                    }
                });
            }
        });
    }

    private void resetResults() {
        allResults.clear();
        visibleResults.clear();
        clearResultsView();
    }

    private void bindToResults() {
        model.getResponses().addListenerAndNotifyCurrent(new ObservableListListener<ReportExecuteResponse>() {
            @Override public void onAdded(final ReportExecuteResponse reportExecuteResponse) {
                allResults.add(reportExecuteResponse);

                if (instanceFiltersModel.passesFilter(reportExecuteResponse.getInstanceKey())) {

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            visibleResults.add(reportExecuteResponse);

                            Collections.sort(visibleResults, new Comparator<ReportExecuteResponse>() {
                                @Override public int compare(ReportExecuteResponse o1, ReportExecuteResponse o2) {
                                    return CompareUtils.start().add(o1.getInstanceKey().getEnvironment(), o2.getInstanceKey().getEnvironment()).add(o1.getInstanceKey().getHost(),
                                            o2.getInstanceKey().getHost()).add(o1.getInstanceKey().getInstanceType(), o2.getInstanceKey().getInstanceType()).add(o1.getInstanceKey()
                                                                                                                                                                   .getInstanceIdentifier(),
                                            o2.getInstanceKey().getInstanceIdentifier()).compare();
                                }
                            });
                            updateResultsView();
                        }
                    });
                }
            }

            @Override public void onRemoved(ReportExecuteResponse reportExecuteResponse, int index) {

            }

            @Override public void onCleared() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        clearResultsView();
                    }
                });
                row = 0;
            }
        });
    }

    private void clearResultsView() {
        resultGridPanel.removeAll();
        addResultHeaders();
        resultGridPanel.invalidate();
        resultGridPanel.revalidate();
        resultGridPanel.doLayout();
        resultGridPanel.repaint();
    }

    private void updateResultsView() {

        clearResultsView();

        for (ReportExecuteResponse visibleResult : visibleResults) {
            addResult(visibleResult);
        }

    }

    private void addResult(ReportExecuteResponse reportExecuteResponse) {
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

            final JTextArea text = new JTextArea(reportExecuteResponse.getResult().getExternalReason());
            text.setLineWrap(true);
            resultGridPanel.add(text, StringUtils.format("cell 8 {}", row));
        }

        row++;
        resultGridPanel.doLayout();
    }

    private void addResultHeaders() {
        row = 0;
        resultGridPanel.add(newJLabel2("Environment"), StringUtils.format("cell 0 {}", row));
        resultGridPanel.add(newJLabel2("Host"), StringUtils.format("cell 1 {}", row));
        resultGridPanel.add(newJLabel2("Address"), StringUtils.format("cell 2 {}", row));
        resultGridPanel.add(newJLabel2("Type"), StringUtils.format("cell 3 {}", row));
        resultGridPanel.add(newJLabel2("Instance"), StringUtils.format("cell 4 {}", row));
        resultGridPanel.add(newJLabel2("PID"), StringUtils.format("cell 5 {}", row));
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

    private void copyCSVToClipboard() {
        StringUtils.StringUtilsBuilder builder = new StringUtils.StringUtilsBuilder();

        // Write the header rows
        builder.appendLine("Environment,Host,Address,Type,Instance,PID,Result,Exit code,Output");

        for (ReportExecuteResponse visibleResult : visibleResults) {
            builder.append(visibleResult.getInstanceKey().getEnvironment()).append(",");
            builder.append(visibleResult.getInstanceKey().getHost()).append(",");
            builder.append(visibleResult.getInstanceKey().getAddress()).append(",");
            builder.append(visibleResult.getInstanceKey().getInstanceType()).append(",");
            builder.append(visibleResult.getInstanceKey().getInstanceIdentifier()).append(",");
            builder.append(visibleResult.getInstanceKey().getPid()).append(",");
            builder.append(visibleResult.getResult().getState()).append(",");

            if (visibleResult.getResult().isSuccessful()) {
                builder.append(visibleResult.getResult().getValue().getReturnCode()).append(",\"");
                builder.append(visibleResult.getResult().getValue().getResult()).append("\"");
            } else {
                builder.append(",");
                builder.append(visibleResult.getResult().getExternalReason());
            }

            builder.append("\n");
        }

        StringSelection stringSelection = new StringSelection(builder.toString());
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
    }

    private void copyHTMLToClipboard() {

        HTMLBuilder2 builder = new HTMLBuilder2();

        HTMLBuilder2.TableElement table = builder.getBody().table();

        HTMLBuilder2.RowElement header1 = table.row();

        // Write the header rows
        header1.cells("Environment","Host","Address","Type","Instance","PID","Result","Exit code","Output");

        for (ReportExecuteResponse visibleResult : visibleResults) {

            HTMLBuilder2.RowElement dataRow = table.row();

            dataRow.cell(visibleResult.getInstanceKey().getEnvironment());
            dataRow.cell(visibleResult.getInstanceKey().getHost());
            dataRow.cell(visibleResult.getInstanceKey().getAddress());
            dataRow.cell(visibleResult.getInstanceKey().getInstanceType());
            dataRow.cell(visibleResult.getInstanceKey().getInstanceIdentifier());
            dataRow.cell(visibleResult.getInstanceKey().getPid());
            dataRow.cell(visibleResult.getResult().getState());

            if (visibleResult.getResult().isSuccessful()) {
                dataRow.cell(visibleResult.getResult().getValue().getReturnCode());
                dataRow.cell(visibleResult.getResult().getValue().getResult());
            } else {
                dataRow.cell("");
                dataRow.cell(visibleResult.getResult().getExternalReason());
            }

        }

        StringSelection stringSelection = new StringSelection(builder.toString());
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);

    }
}


