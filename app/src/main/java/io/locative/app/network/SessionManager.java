package io.locative.app.network;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.locative.app.utils.Preferences;

/**
 * Created by chris on 08.12.15.
 */

@Singleton
public class SessionManager {

    @Inject
    SharedPreferences mPrefs;


    public boolean hasSession() {
        return mPrefs.contains(Preferences.SESSION_ID);
    }

    @Nullable
    public String getSessionId() {
        return mPrefs.getString(Preferences.SESSION_ID, null);
    }

    public void setSessionId(@NonNull String sessionId) {
        // async
        mPrefs.edit().putString(Preferences.SESSION_ID, sessionId).apply();
    }

    public void clearSession() {
        // async
        mPrefs.edit().remove(Preferences.SESSION_ID).apply();
    }


}
