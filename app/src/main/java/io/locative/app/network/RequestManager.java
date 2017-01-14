package io.locative.app.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.inject.Inject;

import io.locative.app.R;
import io.locative.app.model.EventType;
import io.locative.app.model.Fencelog;
import io.locative.app.model.Geofences;
import io.locative.app.notification.NotificationManager;
import io.locative.app.utils.Constants;
import io.locative.app.utils.Preferences;
import io.locative.app.utils.UrlEncoder;
import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class RequestManager {

    @Inject
    SharedPreferences mPreferences;
    @Inject
    LocativeApiWrapper mLocativeNetworkingWrapper;
    @Inject
    Context mContext;
    @Inject
    NotificationManager mNotificationManager;

    @Inject
    RequestManager() {
    }

    private final OkHttpClient.Builder mClientBuilder = new OkHttpClient.Builder();

    private String relevantUrl(final Geofences.Geofence geofence, final EventType eventType) {
        if (eventType == EventType.ENTER) {
            if (geofence.enterUrl != null && geofence.enterUrl.length() > 0) {
                return geofence.enterUrl;
            }
        }
        if (eventType == EventType.EXIT) {
            if (geofence.exitUrl != null && geofence.exitUrl.length() > 0) {
                return geofence.exitUrl;
            }
        }
        return mPreferences.getString(Preferences.HTTP_URL, "");
    }

    private int relevantMethod(final Geofences.Geofence geofence, final EventType eventType) {
        if (eventType == EventType.ENTER) {
            if (geofence.enterUrl != null && geofence.enterUrl.length() > 0) {
                return geofence.enterMethod;
            }
        }
        if (eventType == EventType.EXIT) {
            if (geofence.exitUrl != null && geofence.exitUrl.length() > 0) {
                return geofence.exitMethod;
            }
        }
        return mPreferences.getInt(Preferences.HTTP_METHOD, 0);
    }

    private String urlIncludingQuery(final Geofences.Geofence geofence, EventType eventType) {
        final String url = relevantUrl(geofence, eventType);
        final int method = relevantMethod(geofence, eventType);
        if (method == 0) { // POST
            return url;
        }

        // GET
        return url
                .concat(url.contains("?") ? "&" : "?")
                .concat(
                        "latitude=" + UrlEncoder.encode(Double.toString(geofence.latitude))
                                + "&longitude=" + UrlEncoder.encode(Double.toString(geofence.longitude))
                                + "&id=" + UrlEncoder.encode(geofence.getRelevantId())
                                + "&device=" + UrlEncoder.encode(Settings.Secure.getString(mContext.getContentResolver(),
                                Settings.Secure.ANDROID_ID))
                                + "&device_type=" + UrlEncoder.encode("Android")
                                + "&device_model=" + UrlEncoder.encode(Build.MODEL)
                                + "&trigger=" + UrlEncoder.encode(eventToString(eventType))
                                + "&timestamp=" + UrlEncoder.encode(String.valueOf(new Timestamp(new Date().getTime())))
                );
    }

    public void dispatch(final Geofences.Geofence geofence, final EventType eventType) {

        final String httpUsername = geofence.hasAuthentication() ?
                geofence.httpUsername :
                mPreferences.getString(Preferences.HTTP_USERNAME, "");

        final String httpPassword = geofence.hasAuthentication() ?
                geofence.httpPassword :
                mPreferences.getString(Preferences.HTTP_PASSWORD, "");

        if (httpUsername.length() > 0 && httpPassword.length() > 0) {
            mClientBuilder.authenticator(new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    final String basicAuth = Credentials.basic(httpUsername, httpPassword);
                    return response.request().newBuilder().header("Authorization", basicAuth).build();
                }
            });
        }

        OkHttpClient mClient = mClientBuilder.build();
        final int method = relevantMethod(geofence, eventType);
        final RequestBody body = new FormBody.Builder()
                .add("latitude", String.valueOf(geofence.latitude))
                .add("longitude", String.valueOf(geofence.longitude))
                .add("id", geofence.getRelevantId())
                .add("device", Settings.Secure.getString(mContext.getContentResolver(),
                        Settings.Secure.ANDROID_ID))
                .add("device_type", "Android")
                .add("device_model", Build.MODEL)
                .add("trigger", eventToString(eventType))
                .add("timestamp", String.valueOf(new Timestamp(new Date().getTime())))
                .build();
        Request request;
        try {
            request = new Request.Builder()
                    .url(urlIncludingQuery(geofence, eventType))
                    .method(fromMethod(method), method == 0 ? body : null)
                    .build();
        } catch (Exception ex) {
            conditionallyShowFailureNotification(geofence, mContext.getString(R.string.trigger_error_invalid_url_notification));
            return;
        }
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                conditionallyShowFailureNotification(geofence, mContext.getString(R.string.error_http_request, mContext.getString((eventType == EventType.ENTER ? R.string.entering : R.string.exiting))));
                dispatchFencelog(geofence, eventType, relevantUrl(geofence, eventType), fromMethod(method), 0);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                conditionallyShowSuccessNotification(geofence, eventType, response);
                dispatchFencelog(
                        geofence,
                        eventType,
                        relevantUrl(geofence, eventType),
                        fromMethod(method),
                        response.code()
                );
            }
        });
    }

    private void conditionallyShowFailureNotification(final Geofences.Geofence geofence, final String message) {
        if (!mPreferences.getBoolean(Preferences.NOTIFICATION_FAIL, false)) {
            return;
        }
        mNotificationManager.showNotification(geofence.getRelevantId(), message);
    }

    private void conditionallyShowSuccessNotification(final Geofences.Geofence geofence, final EventType eventType, final Response response) {
        if (!mPreferences.getBoolean(Preferences.NOTIFICATION_SUCCESS, false)) {
            return;
        }
        mNotificationManager.showNotification(
                geofence.getRelevantId(),
                mContext.getString(
                        R.string.success_http_request,
                        response.isSuccessful() ? mContext.getString(R.string.success) : mContext.getString(R.string.error),
                        eventType.isEnter() ? mContext.getString(R.string.entering) : mContext.getString(R.string.exiting),
                        response.code()
                )
        );
    }

    void dispatchFencelog(final Geofences.Geofence geofence,
                          final EventType eventType,
                          final String httpUrl,
                          final String httpMethod,
                          final int httpResponseCode) {
        String sessionId = mPreferences.getString(Preferences.SESSION_ID, null);
        if (sessionId != null && eventType != null) {
            Fencelog fencelog = new Fencelog();
            fencelog.locationId = geofence.getRelevantId();
            fencelog.latitude = geofence.latitude;
            fencelog.longitude = geofence.longitude;
            fencelog.eventType = eventType;
            fencelog.origin = Build.MODEL;
            fencelog.httpUrl = httpUrl;
            fencelog.httpMethod = httpMethod;
            fencelog.httpResponseCode = String.valueOf(httpResponseCode);
            mLocativeNetworkingWrapper.doDispatchFencelog(sessionId, fencelog, null);
        }
    }

    private String fromMethod(int method) {
        return method == Constants.HttpMethod.POST.ordinal() ? "POST" : "GET";
    }

    private String eventToString(EventType eventType) {
        return eventType == EventType.ENTER ? "enter" : "exit";
    }

}
