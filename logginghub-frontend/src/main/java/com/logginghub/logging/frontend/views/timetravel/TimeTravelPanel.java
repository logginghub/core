package com.logginghub.logging.frontend.views.timetravel;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

public class TimeTravelPanel extends JPanel implements NavigationListener {

    private JComponent currentView;
//    private final LoggingRepository repositoryClient;

    /**
     * Create the panel.
     * @param repositoryClient 
     */
    public TimeTravelPanel(/*LoggingRepository repositoryClient*/) {
//        this.repositoryClient = repositoryClient;
        
        setLayout(new MigLayout("", "[grow][]", "[][grow][]"));

        JLabel lblHome = new JLabel("Home");
        lblHome.setToolTipText("Go back to the daily view");
        lblHome.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                onHomeSelected();
            }
        });
        add(lblHome, "cell 0 0");
        
        onHomeSelected();
        
        JButton btnNewButton = new JButton("Request data");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getEvents();
            }
        });
        add(btnNewButton, "flowx,cell 0 2");
        
        JButton btnNewButton_1 = new JButton("Cancel");
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO : use a wrapping dialog panel instead of this hack
                Window windowForComponent = SwingUtilities.windowForComponent(TimeTravelPanel.this);
                if(windowForComponent != null){
                    windowForComponent.dispose();
                }
            }
        });
        add(btnNewButton_1, "cell 0 2");
    }

    protected void getEvents() {
        
    }
    
    public void onHomeSelected() {
        
//        List<DailySummary> dailySummaries = repositoryClient.getDailySummaries(30);
        
        DailyHistoryViewChartPanel historyViewChartPanel = new DailyHistoryViewChartPanel();
//        historyViewChartPanel.setDailySummaries(dailySummaries);
        historyViewChartPanel.setNavigationListener(this);

        if (currentView != null) {
            remove(currentView);
        }

        add(historyViewChartPanel, "cell 0 1,alignx left,aligny top");
        revalidate();

        currentView = historyViewChartPanel;
    }

    public void onDaySelected(int dayIndex, long startOfTheDay) {
        
//        List<ChunkDetails> chunkDetails = repositoryClient.getDayBreakdown(startOfTheDay);
        
        HourlyHistoryViewChartPanel hourlyViewChartPanel = new HourlyHistoryViewChartPanel((String) null);
//        hourlyViewChartPanel.setChunkDetails(chunkDetails);
        hourlyViewChartPanel.setNavigationListener(this);

        if (currentView != null) {
            remove(currentView);
        }

        add(hourlyViewChartPanel, "cell 0 1,alignx left,aligny top");
        revalidate();

        currentView = hourlyViewChartPanel;
    }

    private void getDayBreakdown(long startOfTheDay) {
        // TODO Auto-generated method stub
        
    }

    public void onRangeSelected(double startValue, double endValue) {
        
    }

}
