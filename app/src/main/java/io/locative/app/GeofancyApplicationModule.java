package io.locative.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by chris on 08.12.15.
 */

@Module(
        includes = {

        },
        injects = {
            GeofancyApplication.class
        },
        library = true
)
public class GeofancyApplicationModule {


    private final GeofancyApplication mApp;

    public GeofancyApplicationModule(GeofancyApplication application) {
        mApp = application;
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
    @Singleton
    Bus provideBus() {
        return new Bus();
    }


}
