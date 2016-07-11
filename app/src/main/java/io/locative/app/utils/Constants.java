package io.locative.app.utils;

/**
 * Created by mkida on 3/08/2014.
 */
public class Constants {
    public static String LOG = "io.locative.app";

    public static String API_ENDPOINT = "https://my.locative.io";
    public static String API_ORIGIN = android.os.Build.MODEL;//"Android App";
    public static String TOS_URI = "https://my.locative.io/legal";
    public static String TWITTER_URI = "https://twitter.com/LocativeHQ";
    public static String FACEBOOK_URI = "https://www.facebook.com/LocativeHQ";
    public static String SUPPORT_URI = "https://my.locative.io/support";

    public enum TriggerType {
        ARRIVAL,
        DEPARTURE
    }

    ;

    public enum HttpMethod {
        POST,
        GET;
    }

    ;
}
