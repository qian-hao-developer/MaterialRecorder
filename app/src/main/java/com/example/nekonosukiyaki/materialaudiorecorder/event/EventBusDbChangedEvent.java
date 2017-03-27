package com.example.nekonosukiyaki.materialaudiorecorder.event;

/**
 * Created by nekonosukiyaki on 3/26/2017 AD.
 */

public class EventBusDbChangedEvent {
    public enum EventType {
        CREATE,
        DELETE
    }

    private EventType mEventType;

    public EventBusDbChangedEvent(EventType eventType) {
        mEventType = eventType;
    }

    public EventType getEventType() {
        return mEventType;
    }
}
