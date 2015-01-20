package com.logginghub.logging.frontend.services;

import java.awt.event.ActionListener;

public interface MenuService {
    void addMenuItem(String topLevel, String secondLevel, ActionListener action);
}
