package io.locative.app.modules;

import dagger.Module;
import dagger.Provides;
import io.locative.app.network.LocativeApiService;
import io.locative.app.utils.Constants;
import io.locative.app.utils.StringConverter;
import retrofit.RestAdapter;

@Module
public class NetworkingModule {

    @SuppressWarnings("unused")
    @Provides
    public LocativeApiService provideDownloadService() {
        return new RestAdapter.Builder()
                .setEndpoint(Constants.API_ENDPOINT)
                .setConverter(new StringConverter())
                .build()
                .create(LocativeApiService.class);
    }
}
