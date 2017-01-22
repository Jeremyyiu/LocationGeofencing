package io.locative.app.modules;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.jakewharton.retrofit.Ok3Client;

import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import io.locative.app.BuildConfig;
import io.locative.app.network.LocativeApiService;
import io.locative.app.utils.Constants;
import io.locative.app.utils.StringConverter;
import okhttp3.OkHttpClient;
import retrofit.RestAdapter;

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

    @SuppressWarnings("unused")
    @Provides
    LocativeApiService provideDownloadService() {
        return new RestAdapter.Builder()
                .setEndpoint(Constants.API_ENDPOINT)
                .setConverter(new StringConverter())
                .setClient(new Ok3Client(getClient()))
                .build()
                .create(LocativeApiService.class);
    }
}
