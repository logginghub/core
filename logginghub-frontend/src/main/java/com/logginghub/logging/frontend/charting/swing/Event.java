package com.logginghub.logging.frontend.charting.swing;

public class Event {

    private EventSource source;
    private Object payload;

    public Event(EventSource eventSource) {
        source = eventSource;
    }

    public Event(EventSource eventSource, Object payload) {
        this(eventSource);
        setPayload(payload);
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
    
    @SuppressWarnings("unchecked") public <T> T getPayload() {
        return (T)payload;
    }

    public EventSource getSource() {
        return source;
    }
}
