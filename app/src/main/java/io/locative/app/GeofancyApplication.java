package io.locative.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import dagger.ObjectGraph;
import io.locative.app.network.GeofancyNetworkingWrapper;

public class GeofancyApplication extends Application {

    private static final String APP_PREFS = "geofancy";
    private static final String PREF_SESSION_ID = "sessionId";

    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        super.onCreate();

        // create ObjectGraph to provide all modules
        mObjectGraph = ObjectGraph.create(new GeofancyApplicationModule(this));
        mObjectGraph.inject(this);

    }


    public static void inject(Context context) {
        ((GeofancyApplication) context.getApplicationContext()).mObjectGraph.inject(context);
    }

    public static void inject(Object target, Context context) {
        ((GeofancyApplication) context.getApplicationContext()).mObjectGraph.inject(target);
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