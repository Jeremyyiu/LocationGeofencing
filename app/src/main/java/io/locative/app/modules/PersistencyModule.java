package io.locative.app.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.locative.app.LocativeApplication;
import io.locative.app.persistent.Storage;

import dagger.Module;
import dagger.Provides;

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

    @SuppressWarnings("unused")
    @Provides
    SharedPreferences providePreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
