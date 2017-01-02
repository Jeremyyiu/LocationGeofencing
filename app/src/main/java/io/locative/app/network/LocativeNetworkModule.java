package io.locative.app.network;

import dagger.Module;
import dagger.Provides;
import io.locative.app.utils.Constants;
import io.locative.app.utils.StringConverter;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;

@Module(library = true, complete = false,

        injects = {
                LocativeApiWrapper.class,
                ReceiveTransitionsIntentService.class,
        }

)
public class LocativeNetworkModule {

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
