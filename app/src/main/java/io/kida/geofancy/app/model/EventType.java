package io.kida.geofancy.app.model;


public enum EventType {

    ENTER("enter"),
    EXIT("exit");

    public final String apiName;

    EventType(String apiName){
        this.apiName = apiName;
    }
}
