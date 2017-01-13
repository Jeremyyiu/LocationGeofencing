package io.locative.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.jakewharton.threetenabp.AndroidThreeTen;

import io.fabric.sdk.android.Fabric;
import io.locative.app.network.LocativeApiWrapper;
import io.locative.app.network.LocativeNetworkModule;
import io.locative.app.network.ReceiveTransitionsIntentService;

public class LocativeApplication extends Application {

    private static LocativeApplication mInstance;
    private LocativeComponent mComponent;

    @Override public void onCreate() {
        super.onCreate();
        mInstance = this;

        if (BuildConfig.USE_CRASHLYTICS) {
            Fabric.with(this, new Crashlytics());
        }

        mComponent = DaggerLocativeComponent.builder()
                .locativeApplicationModule(new LocativeApplicationModule(this))
                .locativeNetworkModule(new LocativeNetworkModule())
                .build();
        AndroidThreeTen.init(this);
    }

    public void inject(LocativeApiWrapper object) {
        mComponent.inject(object);
    }

    public void inject(ReceiveTransitionsIntentService object) {
        mComponent.inject(object);
    }

    public LocativeComponent getComponent() {
        return mComponent;
    }

    public static LocativeApplication getApplication() {
        return mInstance;
    }
}