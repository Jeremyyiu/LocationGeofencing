package io.locative.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.JsonParser;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.locative.app.network.LocativeNetworkModule;
import io.locative.app.notification.NotificationManager;
import io.locative.app.view.UIModule;

@Module(
        includes = {
                LocativeNetworkModule.class,
                UIModule.class
        },
        injects = {
                LocativeApplication.class
        },
        library = true
)
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
    @Singleton
    public Context getApplicationContext() {
        return mApp;
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
    @Singleton
    Bus provideBus() {
        return new Bus();
    }


}
