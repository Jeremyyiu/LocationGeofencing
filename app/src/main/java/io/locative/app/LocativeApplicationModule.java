package io.locative.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.JsonParser;
import com.squareup.otto.Bus;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.locative.app.network.SessionManager;
import io.locative.app.notification.NotificationManager;
import io.locative.app.persistent.Storage;

@Module
public class LocativeApplicationModule {


    private final LocativeApplication mApp;

    public LocativeApplicationModule(LocativeApplication application) {
        mApp = application;
    }

    @SuppressWarnings("unused")
    @Provides
    @Singleton
    JsonParser getJsonParser() {
        return new JsonParser();
    }

    @SuppressWarnings("unused")
    @Provides
    Context getApplicationContext() {
        return mApp;
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

    @SuppressWarnings("unused")
    @Provides
    NotificationManager provideNotificationManager(Context context) {
        return new NotificationManager(context);
    }

    @SuppressWarnings("unused")
    @Provides
    Storage provideStorage(Context context) {
        return new Storage(context);
    }

    @SuppressWarnings("unused")
    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
    }


}
