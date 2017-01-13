package io.locative.app;

import android.app.Activity;

import com.google.gson.JsonParser;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Provides;
import io.locative.app.network.LocativeApiService;
import io.locative.app.network.LocativeApiWrapper;
import io.locative.app.network.LocativeNetworkModule;
import io.locative.app.network.ReceiveTransitionsIntentService;
import io.locative.app.network.SessionManager;
import io.locative.app.view.AddEditGeofenceActivity;
import io.locative.app.view.BaseActivity;
import io.locative.app.view.GeofencesActivity;
import io.locative.app.view.SettingsActivity;

@Singleton
@Component(modules = {LocativeApplicationModule.class, LocativeNetworkModule.class})
public interface LocativeComponent {
    void inject(LocativeApiWrapper object);
    void inject(ReceiveTransitionsIntentService object);
    void inject(GeofencesActivity object);
    void inject(SettingsActivity object);
    void inject(BaseActivity object);
    void inject(AddEditGeofenceActivity object);
    void inject(SessionManager object);
}
