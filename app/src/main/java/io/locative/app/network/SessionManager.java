package io.locative.app.network;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.locative.app.LocativeComponent;
import io.locative.app.model.Account;
import io.locative.app.utils.Preferences;

@Singleton
public class SessionManager {

    @Inject
    SharedPreferences mPrefs;

    public SessionManager(LocativeComponent component) {
        component.inject(this);
    }

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
        mPrefs.edit().remove(Preferences.ACCOUNT).apply();
    }

    @Nullable
    public Account getAccount() {
        String accountJson = mPrefs.getString(Preferences.ACCOUNT, null);
        if (accountJson != null) {
            return new Account().fromJsonRepresentation(accountJson);
        }
        return null;
    }

    public void setAccount(@NonNull Account account) {
        mPrefs.edit().putString(Preferences.ACCOUNT, account.jsonRepresentation()).apply();
    }

    public void clearAccount() {
        mPrefs.edit().remove(Preferences.ACCOUNT).apply();
    }
}
