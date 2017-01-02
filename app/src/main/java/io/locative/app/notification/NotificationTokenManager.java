package io.locative.app.notification;

import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.gson.JsonObject;

import io.locative.app.network.FcmPayloadBuilder;
import io.locative.app.network.LocativeApiService;
import io.locative.app.network.LocativeConnect;
import io.locative.app.utils.Constants;
import io.locative.app.utils.Preferences;
import io.locative.app.utils.StringConverter;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NotificationTokenManager extends FirebaseInstanceIdService {
    private final LocativeConnect connect = new LocativeConnect();

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(Constants.LOG, "Received new FCM token:" + token);
        connect.updateSession(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Preferences.SESSION_ID, null), token, false);
    }
}
