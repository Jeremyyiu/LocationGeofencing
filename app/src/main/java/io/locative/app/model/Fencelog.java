package io.locative.app.model;

import org.threeten.bp.LocalDateTime;

import java.util.Date;

public class Fencelog {
    public double longitude;
    public double latitude;
    public String locationId;
    public String httpUrl;
    public String httpMethod;
    public String httpResponseCode;
    public EventType eventType;
    public String fenceType;
    public String origin;
    public LocalDateTime createdAt;
}
