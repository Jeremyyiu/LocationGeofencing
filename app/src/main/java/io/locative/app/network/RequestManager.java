package io.locative.app.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import com.google.android.gms.location.Geofence;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import javax.inject.Inject;

import io.locative.app.model.EventType;
import io.locative.app.model.Fencelog;
import io.locative.app.model.Geofences;
import io.locative.app.notification.NotificationManager;
import io.locative.app.utils.Constants;
import io.locative.app.utils.Preferences;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestManager {

    @Inject
    SharedPreferences mPreferences;
    @Inject
    LocativeApiWrapper mLocativeNetworkingWrapper;
    @Inject
    Context mContext;
    @Inject
    NotificationManager mNotificationManager;

    private final OkHttpClient client = new OkHttpClient();

    public void dispatch(final Geofences.Geofence geofence, final EventType eventType) {
        final String httpMethod = fromMethod(mPreferences.getInt(Preferences.HTTP_METHOD, 0));
        final RequestBody body = new FormBody.Builder()
                .add("latitude", String.valueOf(geofence.latitude))
                .add("longitude", String.valueOf(geofence.longitude))
                .add("id", geofence.id)
                .add("device", Settings.Secure.getString(mContext.getContentResolver(),
                        Settings.Secure.ANDROID_ID))
                .add("trigger", eventToString(eventType))
                .add("timestamp", String.valueOf(new Timestamp(new Date().getTime())))
                .build();
        final Request request = new Request.Builder()
                .url(mPreferences.getString(Preferences.HTTP_URL, ""))
                .method(httpMethod, body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mPreferences.getBoolean(Preferences.NOTIFICATION_FAIL, false)) {
                    mNotificationManager.showNotification("Error when sending HTTP request for " + geofence.subtitle);
                }
                dispatchFencelog(geofence, eventType, httpMethod, 0, null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (mPreferences.getBoolean(Preferences.NOTIFICATION_SUCCESS, false)) {
                    mNotificationManager.showNotification("Success when sending HTTP request for " + geofence.subtitle);
                }
                dispatchFencelog(
                        geofence,
                        eventType,
                        httpMethod,
                        response.code(),
                        constrainedString(response.body().string())
                );
            }
        });
    }

    private String constrainedString(String in) {
        return in.length() > 256 ? in.substring(0, 255) : in;
    }

    private void dispatchFencelog(final Geofences.Geofence geofence,
                                  final EventType eventType,
                                  final String httpMethod,
                                  final int httpResponseCode,
                                  final String httpResponse) {
        String sessionId = mPreferences.getString(Preferences.SESSION_ID, null);
        if (sessionId != null && eventType != null) {
            Fencelog fencelog = new Fencelog();
            fencelog.locationId = geofence.id;
            fencelog.latitude = geofence.latitude;
            fencelog.longitude = geofence.longitude;
            fencelog.eventType = eventType;
            fencelog.origin = Build.MODEL;
            fencelog.httpMethod = httpMethod;
            fencelog.httpResponseCode = String.valueOf(httpResponseCode);
            fencelog.httpResponse = httpResponse;
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
