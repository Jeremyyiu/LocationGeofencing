package io.locative.app.modules;

import android.content.Context;

import com.google.gson.JsonParser;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.locative.app.LocativeApplication;
import io.locative.app.service.TriggerManager;
import io.locative.app.notification.NotificationManager;

@Module
public class AppModule {

    private final LocativeApplication mApp;

    public AppModule(LocativeApplication application) {
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

    @SuppressWarnings("unused")
    @Provides
    NotificationManager provideNotificationManager(Context context) {
        return new NotificationManager(context);
    }

    @SuppressWarnings("unused")
    @Provides
    TriggerManager provideTriggerManager() {
        return new TriggerManager();
    }

    @SuppressWarnings("unused")
    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
    }
}
