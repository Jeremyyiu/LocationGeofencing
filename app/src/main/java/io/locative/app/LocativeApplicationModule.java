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
import io.locative.app.network.LocativeNetworkModule;
import io.locative.app.network.SessionManager;
import io.locative.app.notification.NotificationManager;
import io.locative.app.notification.NotificationTokenManager;
import io.locative.app.persistent.Storage;
import io.locative.app.view.UIModule;

@Module
public class LocativeApplicationModule {


    private final LocativeApplication mApp;

    public LocativeApplicationModule(LocativeApplication application) {
        mApp = application;
    }

    @SuppressWarnings("unused")
    @Provides
    @Singleton
    public JsonParser getJsonParser() {
        return new JsonParser();
    }

    @SuppressWarnings("unused")
    @Provides
    public Context getApplicationContext() {
        return mApp;
    }

    @Provides
    @Singleton
    public SessionManager provideSessionManager() {
        return new SessionManager();
    }

    @SuppressWarnings("unused")
    @Provides
    public SharedPreferences providePreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @SuppressWarnings("unused")
    @Provides
    public NotificationManager provideNotificationManager(Context context) {
        return new NotificationManager(context);
    }

    @SuppressWarnings("unused")
    @Provides
    public Storage provideStorage(Context context) {
        return new Storage(context);
    }

    @SuppressWarnings("unused")
    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
    }


}
