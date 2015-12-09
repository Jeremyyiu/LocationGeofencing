package io.locative.app;

import android.app.Application;
import android.content.Context;

import dagger.ObjectGraph;

public class LocativeApplication extends Application {

    private ObjectGraph mObjectGraph;

    @Override
    public void onCreate() {
        super.onCreate();

        // create ObjectGraph to provide all modules
        mObjectGraph = ObjectGraph.create(new LocativeApplicationModule(this));
        mObjectGraph.inject(this);

    }


    public static void inject(Context context) {
        ((LocativeApplication) context.getApplicationContext()).mObjectGraph.inject(context);
    }

    public static void inject(Object target, Context context) {
        ((LocativeApplication) context.getApplicationContext()).mObjectGraph.inject(target);
    }

}