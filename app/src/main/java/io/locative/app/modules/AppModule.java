package io.locative.app.modules;

import android.content.Context;

import io.locative.app.LocativeApplication;
import io.locative.app.notification.NotificationManager;
import io.locative.app.service.TriggerManager;
import io.locative.app.utils.ResourceUtils;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private final LocativeApplication mApp;

    public AppModule(LocativeApplication application) {
        mApp = application;
    }


    @Provides
    Context getApplicationContext() {
        return mApp;
    }

    @Provides
    NotificationManager provideNotificationManager(Context context) {
        return new NotificationManager(context);
    }

    @Provides
    TriggerManager provideTriggerManager() {
        return new TriggerManager();
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
    }

    @Provides
    ResourceUtils provideResourceUtils() {
        return new ResourceUtils(mApp);
    }
}
