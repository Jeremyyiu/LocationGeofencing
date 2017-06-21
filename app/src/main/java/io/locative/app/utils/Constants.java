package io.locative.app.utils;

public class Constants {

    public static String LOG = "io.locative.app";
    public static String API_ORIGIN = android.os.Build.MODEL;

    public enum TriggerType {
        ARRIVAL,
        DEPARTURE
    };

    public enum HttpMethod {
        POST,
        GET;
    };
}
