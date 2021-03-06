package io.locative.app;

import android.app.Application;

import io.locative.app.modules.AppModule;
import io.locative.app.modules.PersistencyModule;
import io.locative.app.notification.NotificationManager;
import io.locative.app.service.ReceiveTransitionsIntentService;
import io.locative.app.service.TransitionService;
import io.locative.app.service.TriggerManager;

public class LocativeApplication extends Application {

    private static LocativeApplication mInstance;
    private LocativeComponent mComponent;

    @Override public void onCreate() {
        super.onCreate();
        mInstance = this;

        mComponent = DaggerLocativeComponent.builder()
                .appModule(new AppModule(this))
                .persistencyModule(new PersistencyModule(this))
                .build();
    }

    public void inject(ReceiveTransitionsIntentService object) {
        mComponent.inject(object);
    }

    public void inject(TriggerManager object) {
        mComponent.inject(object);
    }

    public void inject(TransitionService object) {
        mComponent.inject(object);
    }

    public void inject(NotificationManager object) {
        mComponent.inject(object);
    }

    public LocativeComponent getComponent() {
        return mComponent;
    }

    public static LocativeApplication getApplication() {
        return mInstance;
    }
}