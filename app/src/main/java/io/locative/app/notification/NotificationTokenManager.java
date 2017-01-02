package io.locative.app.notification;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.List;

import io.locative.app.model.Fencelog;
import io.locative.app.model.Geofences;
import io.locative.app.model.Notification;
import io.locative.app.network.LocativeApiService;
import io.locative.app.network.SessionUpdatePayload;
import io.locative.app.utils.Constants;
import io.locative.app.utils.Preferences;
import io.locative.app.utils.StringConverter;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NotificationTokenManager extends FirebaseInstanceIdService {
    private final LocativeApiService apiService = new RestAdapter.Builder()
            .setEndpoint(Constants.API_ENDPOINT)
            .setConverter(new StringConverter())
            .build()
            .create(LocativeApiService.class);

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("Locative", "Received new FCM token:" + token);
        SessionUpdatePayload payload = new SessionUpdatePayload(token);
        apiService.updateSession(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Preferences.SESSION_ID, null), payload, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                // TODO: Implement success
            }

            @Override
            public void failure(RetrofitError error) {
                // TODO: Implement error handling
            }
        });
    }
}
