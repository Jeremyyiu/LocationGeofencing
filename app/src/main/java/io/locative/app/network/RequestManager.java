package io.locative.app.network;

import android.content.SharedPreferences;

import javax.inject.Inject;

import io.locative.app.model.Geofences;
import io.locative.app.utils.Constants;
import io.locative.app.utils.Preferences;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RequestManager {

    @Inject
    SharedPreferences mPreferences;

    private final OkHttpClient client = new OkHttpClient();

    public void perform (Geofences.Geofence geofence, int transition, Callback callback) {
        final RequestBody body = new FormBody.Builder()
                .add("latitude", String.valueOf(geofence.latitude))
                .add("longitude", String.valueOf(geofence.longitude))
                .add("id", geofence.id)
                .add("device", "")
                .add("trigger", fromTransition(transition))
                .add("timestamp", null)
                .build();
        final Request request = new Request.Builder()
                .url(mPreferences.getString(Preferences.HTTP_URL, ""))
                .method(fromMethod(mPreferences.getInt(Preferences.HTTP_METHOD, 0)), body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    private String fromMethod (int method) {
        return method == Constants.HttpMethod.POST.ordinal() ? "POST" : "GET";
    }

    private String fromTransition (int transition) {
        return transition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER ? "enter" : "exit";
    }
}
