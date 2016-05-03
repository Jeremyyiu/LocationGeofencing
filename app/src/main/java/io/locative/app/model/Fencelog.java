package io.locative.app.model;

import org.threeten.bp.LocalDateTime;

import java.util.Date;

/**
 * Created by mkida on 17/08/2014.
 */
public class Fencelog {
    /*
    NSDictionary *params = @{@"longitude": NumberOrZeroFloat(fencelog.longitude),
                             @"latitude": NumberOrZeroFloat(fencelog.latitude),
                             @"locationId": StringOrEmpty(fencelog.locationId),
                             @"httpUrl": StringOrEmpty(fencelog.httpUrl),
                             @"httpMethod": StringOrEmpty(fencelog.httpMethod),
                             @"httpResponseCode": StringOrEmpty(fencelog.httpResponseCode),
                             @"httpResponse": StringOrEmpty(fencelog.httpResponse),
                             @"eventType": StringOrEmpty(fencelog.eventType),
                             @"fenceType": StringOrEmpty(fencelog.fenceType),
                             @"origin": [self originString]
                             };
     */

    public float longitude;
    public float latitude;
    public String locationId;
    public String httpUrl;
    public String httpMethod;
    public String httpResponseCode;
    public String httpResponse;
    public EventType eventType;
    public String fenceType;
    public String origin;
    public LocalDateTime createdAt;
}
