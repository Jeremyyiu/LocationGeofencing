package io.locative.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.locative.app.network.GeofancyNetworking;

public class GeofancyApplication extends Application {

    private static final String APP_PREFS = "geofancy";
    private static final String PREF_SESSION_ID = "sessionId";

    private GeofancyNetworking networking;

    @Override
    public void onCreate() {
        super.onCreate();
        this.networking = new GeofancyNetworking();
    }

    public GeofancyNetworking getNetworking() {
        return networking;
    }

    public boolean hasSession() {
        return getPrefs().contains(PREF_SESSION_ID);
    }

    @Nullable
    public String getSessionId() {
        return getPrefs().getString(PREF_SESSION_ID, null);
    }

    public void setSessionId(@NonNull String sessionId) {
        // async
        getPrefs().edit().putString(PREF_SESSION_ID, sessionId).apply();
    }

    public void clearSession() {
        // async
        getPrefs().edit().remove(PREF_SESSION_ID).apply();
    }

    private SharedPreferences getPrefs() {
        return getSharedPreferences(APP_PREFS, MODE_PRIVATE);
    }
}