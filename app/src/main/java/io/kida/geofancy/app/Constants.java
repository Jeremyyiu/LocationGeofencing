package io.kida.geofancy.app;

import java.util.Currency;

/**
 * Created by mkida on 3/08/2014.
 */
public class Constants {
    public static String LOG = "io.kida.geofancy.app";

    public enum TriggerType {
        ARRIVAL,
        DEPARTURE
    };

    public enum HttpMethod {
        POST,
        GET;
    };
}
