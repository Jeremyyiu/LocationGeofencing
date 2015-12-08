package io.locative.app.network;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by chris on 08.12.15.
 */

@Singleton
public class SessionManager {

    private static final String PREF_SESSION_ID = "sessionId";

    @Inject
    SharedPreferences mPrefs;


    public boolean hasSession() {
        return mPrefs.contains(PREF_SESSION_ID);
    }

    @Nullable
    public String getSessionId() {
        return mPrefs.getString(PREF_SESSION_ID, null);
    }

    public void setSessionId(@NonNull String sessionId) {
        // async
        mPrefs.edit().putString(PREF_SESSION_ID, sessionId).apply();
    }

    public void clearSession() {
        // async
        mPrefs.edit().remove(PREF_SESSION_ID).apply();
    }


}
