package com.logginghub.logging.frontend.views.timetravel;

public interface NavigationListener {

    void onHomeSelected();
    void onDaySelected(int dayIndex, long startOfTheDay);
    void onRangeSelected(double startValue, double endValue);
    
}
