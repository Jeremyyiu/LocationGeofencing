package io.locative.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.jakewharton.threetenabp.AndroidThreeTen;

import dagger.ObjectGraph;
import io.fabric.sdk.android.Fabric;

public class LocativeApplication extends Application {
    private ObjectGraph mObjectGraph;
    private static LocativeApplication mInstance;

  @Override public void onCreate() {
        super.onCreate();
        mInstance = this;

        if (BuildConfig.USE_CRASHLYTICS) {
            Fabric.with(this, new Crashlytics());
        }

        mObjectGraph = ObjectGraph.create(new LocativeApplicationModule(this));
        AndroidThreeTen.init(this);
  }

    public void inject(Object o) {
    mObjectGraph.inject(o);
  }

    public static LocativeApplication getApplication() {
        return mInstance;
    }
}