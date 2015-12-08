package io.locative.app.network;

import android.util.Log;

import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.locative.app.model.Fencelog;
import io.locative.app.utils.AeSimpleSHA1;
import io.locative.app.utils.Constants;
import io.locative.app.utils.StringConverter;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

@Singleton
public class GeofancyNetworkingWrapper {

    @Inject
    GeofancyNetworkService mService;


    public void doLogin(String username, String password, final GeofancyNetworkingCallback callback) {
        mService.login(username, password, Constants.API_ORIGIN, new Callback<String>() {

            @Override
            public void success(String string, Response response) {
                Log.d(Constants.LOG, "Login Success: " + string);
                String sessionId = null;
                try {
                    JSONObject json = new JSONObject(string);
                    sessionId = json.getString("success");
                } catch (Exception e) {
                    Log.e(Constants.LOG, e.getMessage(), e);
                } finally {
                    callback.onLoginFinished(sessionId != null && sessionId.length() > 0, sessionId);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(Constants.LOG, "Login Error: " + error);
                callback.onLoginFinished(false, null);
            }
        });
    }

    public void doSignup(String username, String password, String email, final GeofancyNetworkingCallback callback) {
        String token = null;
        try {
            token = AeSimpleSHA1.SHA1(username + ":" + password + "%" + email);
            Log.d(Constants.LOG, "Token: " + token);
        } catch (Exception e) {
            Log.e(Constants.LOG, "Caught Exception: " + e);
        }

        mService.signup(username, password, email, token, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                Log.d(Constants.LOG, "Signup Success: " + s);
                callback.onSignupFinished(true, false);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(Constants.LOG, "Signup Error: " + error);
                callback.onSignupFinished(false, error.getResponse().getStatus() == 409);
            }
        });
    }

    public void doCheckSession(String sessionId, final GeofancyNetworkingCallback callback) {
        mService.checkSession(sessionId, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                callback.onCheckSessionFinished(true);
            }

            @Override
            public void failure(RetrofitError error) {
                callback.onCheckSessionFinished(false);
            }
        });
    }

    public void doDispatchFencelog(String sessionId, Fencelog fencelog, final GeofancyNetworkingCallback callback) {
        mService.dispatchFencelog(
                sessionId,
                fencelog.longitude,
                fencelog.latitude,
                fencelog.locationId,
                fencelog.httpUrl,
                fencelog.httpMethod,
                fencelog.httpResponseCode,
                fencelog.httpResponse,
                fencelog.eventType.apiName,
                fencelog.fenceType,
                fencelog.origin,
                new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        callback.onDispatchFencelogFinished(true);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        callback.onDispatchFencelogFinished(false);
                    }
                });
    }
}
