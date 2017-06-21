package io.locative.app.modules;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.util.concurrent.TimeUnit;

import dagger.Module;
import io.locative.app.BuildConfig;
import okhttp3.OkHttpClient;

@Module
public class NetworkingModule {

    private OkHttpClient getClient() {
        OkHttpClient.Builder builder =  new OkHttpClient.Builder()
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES);

        if (BuildConfig.DEBUG) {
            builder.addNetworkInterceptor(new StethoInterceptor());
        }

        return builder.build();
    }
}
