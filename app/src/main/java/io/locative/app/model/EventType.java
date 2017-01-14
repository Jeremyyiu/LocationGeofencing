package io.locative.app.model;

public enum EventType {

    ENTER("enter"),
    EXIT("exit");

    private final String mEventName;

    EventType(String apiName) {
        mEventName = apiName;
    }

    public String getEventName() {
        return mEventName;
    }

    public boolean isEnter() {
        return this == ENTER;
    }

    public boolean isExit() {
        return this == EXIT;
    }
}
