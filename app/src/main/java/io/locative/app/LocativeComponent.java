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

@Singleton
@Component(modules = {LocativeApplicationModule.class, LocativeNetworkModule.class, LocativeApiWrapper.class})
public interface LocativeComponent {
    void inject(LocativeApiWrapper object);
    void inject(ReceiveTransitionsIntentService object);
}
