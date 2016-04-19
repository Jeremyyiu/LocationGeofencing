package io.locative.app;

import android.app.Application;

import dagger.ObjectGraph;

public class LocativeApplication extends Application {
  private ObjectGraph objectGraph;

  @Override public void onCreate() {
    super.onCreate();

    objectGraph = ObjectGraph.create(new LocativeApplicationModule(this));
  }

  public void inject(Object o) {
    objectGraph.inject(o);
  }
}