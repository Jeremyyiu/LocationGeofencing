package io.locative.app.network;

import dagger.Module;
import dagger.Provides;
import io.locative.app.utils.Constants;
import io.locative.app.utils.StringConverter;
import retrofit.RestAdapter;

/**
 * Created by chris on 08.12.15.
 */

@Module(library = true, complete = false,

        injects = {
                GeofancyNetworkingWrapper.class,
                ReceiveTransitionsIntentService.class
        }

)
public class GeofancyNetworkModule {

    @SuppressWarnings("unused")
    @Provides
    public GeofancyNetworkService provideDownloadService() {
        return new RestAdapter.Builder()
                .setEndpoint(Constants.API_ENDPOINT)
                .setConverter(new StringConverter())
                .build()
                .create(GeofancyNetworkService.class);
    }


}
