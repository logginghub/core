package com.logginghub.logging.frontend.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import com.logginghub.logging.frontend.services.MenuService;
import com.logginghub.utils.TimeProvider;

public class TimeControllerModule {

    private TimeProvider timeProvider;

    public TimeControllerModule(MenuService menuService, TimeProvider timeProvider) {
        
        this.timeProvider = timeProvider;
        
        menuService.addMenuItem("Edit", "Time controller", new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                showTimePopout();
            }
        });
    }

    protected void showTimePopout() {
        JFrame timeFrame = new JFrame();
        TimeControllerView view = new TimeControllerView();
        view.setTimeProvider(timeProvider);
        view.start();
        timeFrame.getContentPane().add(view);
        timeFrame.setSize(600,400);
        timeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        timeFrame.setVisible(true);
    }

}
