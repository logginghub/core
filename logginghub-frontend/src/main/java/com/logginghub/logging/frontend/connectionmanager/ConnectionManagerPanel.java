package com.logginghub.logging.frontend.connectionmanager;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import com.logginghub.logging.frontend.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.configuration.HubConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;

public class ConnectionManagerPanel extends JPanel {
    private JTextField hostOrIP;
    private JTextField port;
    private JList presetsList;
    private JList customList;
    private JList hubsForEnvironmentList;
    private ConnectionManagerListener listener;

    public ConnectionManagerPanel() {
        setLayout(new MigLayout("", "[grow,fill][grow,fill][grow,fill]", "[grow][grow]"));

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Preset environments", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(panel, "cell 0 0,grow");
        panel.setLayout(new MigLayout("", "[grow,fill]", "[grow,fill]"));

        JScrollPane scrollPane = new JScrollPane();
        panel.add(scrollPane, "cell 0 0,grow");

        presetsList = new JList();
        presetsList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() >= 2) {
                    openEnvironment((EnvironmentListItem) presetsList.getSelectedValue());
                }
            }
        });
        presetsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent lse) {
                selectEnvironment((EnvironmentListItem) presetsList.getSelectedValue());
            }
        });
        scrollPane.setViewportView(presetsList);

        JPanel panel_3 = new JPanel();
        panel_3.setBorder(new TitledBorder(null, "Hub connection details", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(panel_3, "cell 1 0,grow");
        panel_3.setLayout(new MigLayout("", "[][grow]", "[][]"));

        JLabel lblNewLabel = new JLabel("Host or IP");
        panel_3.add(lblNewLabel, "cell 0 0,alignx trailing");

        hostOrIP = new JTextField();
        panel_3.add(hostOrIP, "cell 1 0,growx");
        hostOrIP.setColumns(10);

        JLabel lblPort = new JLabel("Port");
        panel_3.add(lblPort, "cell 0 1,alignx trailing");

        port = new JTextField();
        port.setText("58770");
        port.setColumns(10);
        panel_3.add(port, "cell 1 1,growx");

        JPanel panel_4 = new JPanel();
        panel_4.setBorder(new TitledBorder(null, "Hubs for environment", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(panel_4, "cell 2 0,grow");
        panel_4.setLayout(new MigLayout("", "[grow,fill]", "[grow,fill]"));

        JScrollPane scrollPane_2 = new JScrollPane();
        panel_4.add(scrollPane_2, "cell 0 0,alignx left,aligny top");

        hubsForEnvironmentList = new JList();
        scrollPane_2.setViewportView(hubsForEnvironmentList);

        JPanel panel_1 = new JPanel();
        panel_1.setBorder(new TitledBorder(null, "Custom Environments", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(panel_1, "cell 0 1,grow");
        panel_1.setLayout(new MigLayout("", "[grow,fill]", "[grow,fill]"));

        JScrollPane scrollPane_1 = new JScrollPane();
        panel_1.add(scrollPane_1, "cell 0 0,alignx left,aligny top");

        customList = new JList();
        scrollPane_1.setViewportView(customList);

        JPanel panel_2 = new JPanel();
        add(panel_2, "cell 1 1,grow");
        panel_2.setLayout(new MigLayout("", "[89px]", "[23px][][]"));

        JButton btnNewButton = new JButton("Open");
        panel_2.add(btnNewButton, "cell 0 0,alignx left,aligny top");

        JButton btnNewButton_1 = new JButton("Create");
        panel_2.add(btnNewButton_1, "cell 0 1");

        JButton btnNewButton_2 = new JButton("Delete");
        panel_2.add(btnNewButton_2, "cell 0 2");
    }

    public void setListener(ConnectionManagerListener listener) {
        this.listener = listener;
    }

    protected void openEnvironment(EnvironmentListItem selectedValue) {
        listener.onOpenEnvironment(selectedValue.getConfiguration());
    }

    protected void selectEnvironment(EnvironmentListItem selectedValue) {
        DefaultListModel model = new DefaultListModel();
        EnvironmentConfiguration configuration = selectedValue.getConfiguration();
        List<HubConfiguration> hubs = configuration.getHubs();
        for (HubConfiguration hubConfiguration : hubs) {
            model.addElement(new HubListItem(hubConfiguration));
        }
        hubsForEnvironmentList.setModel(model);
    }

    public void populate(LoggingFrontendConfiguration configuration) {

        DefaultListModel model = new DefaultListModel();
        List<EnvironmentConfiguration> environments = configuration.getEnvironments();
        for (EnvironmentConfiguration environmentConfiguration : environments) {
            model.addElement(new EnvironmentListItem(environmentConfiguration));
        }

        presetsList.setModel(model);
    }

    class EnvironmentListItem {
        EnvironmentConfiguration configuration;

        public EnvironmentListItem(EnvironmentConfiguration configuration) {
            this.configuration = configuration;
        }

        public EnvironmentConfiguration getConfiguration() {
            return configuration;
        }

        @Override public String toString() {
            return configuration.getName();

        }
    }

    class HubListItem {
        HubConfiguration configuration;

        public HubListItem(HubConfiguration configuration) {
            this.configuration = configuration;
        }

        public HubConfiguration getConfiguration() {
            return configuration;
        }

        @Override public String toString() {
            return String.format("%s (%s:%s)", configuration.getName(), configuration.getHost(), configuration.getPort());

        }
    }
}
