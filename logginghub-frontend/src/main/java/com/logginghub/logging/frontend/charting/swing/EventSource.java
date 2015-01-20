package com.logginghub.logging.frontend.charting.swing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JLabel;

public class EventSource {

    private String name;
    private List<EventHandler> handlers = null;

    public EventSource(String name) {
        this.name = name;
    }

    public void bindClick(JLabel label) {
        label.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                fireEvent(new Event(EventSource.this));
            }
        });
    }

    public synchronized void addHandler(EventHandler eventHandler) {
        if (handlers == null) {
            handlers = new CopyOnWriteArrayList<EventHandler>();
        }

        handlers.add(eventHandler);
    }

    public synchronized void removeHandler(EventHandler eventHandler) {
        if (handlers != null) {
            handlers.remove(eventHandler);
        }
    }

    public String getName() {
        return name;
    }

    public void fireEvent(Object payload) {
        if (handlers != null) {
            for (EventHandler eventHandler : handlers) {
                eventHandler.onEvent(new Event(EventSource.this, payload));
            }
        }
    }

    public void trigger() {
        fireEvent(null);
    }

}
