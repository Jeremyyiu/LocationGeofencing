package io.locative.app.network;

import android.content.SharedPreferences;

import java.io.IOException;

import javax.inject.Inject;

import io.locative.app.model.Geofences;
import io.locative.app.utils.Constants;
import io.locative.app.utils.Preferences;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class RequestManager {

    @Inject
    SharedPreferences mPreferences;

    private final OkHttpClient client = new OkHttpClient();

    public void perform (Geofences.Geofence geofence, int transition, Callback callback) {
        final RequestBody body = new RequestBody() {
            @Override
            public MediaType contentType() {
                return null;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {

            }
        };
        final Request request = new Request.Builder()
                .url(mPreferences.getString(Preferences.HTTP_URL, ""))
                .method(fromMethod(mPreferences.getInt(Preferences.HTTP_METHOD, 0)), body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    private String fromMethod (int method) {
        return method == Constants.HttpMethod.POST.ordinal() ? "POST" : "GET";
    }
}
