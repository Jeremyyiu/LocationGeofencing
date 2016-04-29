package io.locative.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import dagger.ObjectGraph;
import io.fabric.sdk.android.Fabric;

public class LocativeApplication extends Application {
  private ObjectGraph objectGraph;

  @Override public void onCreate() {
    super.onCreate();
    if (BuildConfig.USE_CRASHLYTICS) {
      Fabric.with(this, new Crashlytics());
    }

    objectGraph = ObjectGraph.create(new LocativeApplicationModule(this));
  }

  public void inject(Object o) {
    objectGraph.inject(o);
  }
}