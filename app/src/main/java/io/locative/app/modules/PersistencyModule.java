package io.locative.app.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.locative.app.LocativeApplication;
import io.locative.app.network.SessionManager;
import io.locative.app.persistent.Storage;

@Module
public class PersistencyModule {

    private LocativeApplication mApp;

    public PersistencyModule(LocativeApplication app) {
        mApp = app;
    }

    @SuppressWarnings("unused")
    @Provides
    Storage provideStorage(Context context) {
        return new Storage(context);
    }

    @Provides
    @Singleton
    SessionManager provideSessionManager() {
        return new SessionManager(mApp.getComponent());
    }

    @SuppressWarnings("unused")
    @Provides
    SharedPreferences providePreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
