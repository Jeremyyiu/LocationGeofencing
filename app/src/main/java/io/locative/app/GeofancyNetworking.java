package io.locative.app;

import android.util.Log;

import org.json.JSONObject;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by mkida on 16/08/2014.
 */

interface  GeofancyNetworkingInterface {

    @GET("/api/session")
    void login(
            @Query("username") String username,
            @Query("password") String password,
            @Query("origin") String origin,
            Callback<String> callback);

    @FormUrlEncoded
    @POST("/api/signup")
    void signup(
            @Field("username") String username,
            @Field("password") String password,
            @Field("email") String email,
            @Field("token") String token,
            Callback<String> callback);

    @GET("/api/session/{session}")
    void checkSession(
            @Path("session") String sessionId,
            Callback<String>callback);

    @FormUrlEncoded
    @POST("/api/fencelogs/{session}")
    void dispatchFencelog(
            @Path("session") String sessionId,
            @Field("longitude") float longitude,
            @Field("latitude") float latitude,
            @Field("locationId") String locationId,
            @Field("httpUrl") String httpUrl,
            @Field("httpMethod") String httpMethod,
            @Field("httpResponseCode") String httpResponseCode,
            @Field("httpResponse") String httpResponse,
            @Field("eventType") String eventType,
            @Field("fenceType") String fenceType,
            @Field("origin") String origin,
            Callback<String> callback);
}

interface GeofancyNetworkingCallback {

    void onLoginFinished(boolean success, String sessionId);

    void onSignupFinished(boolean success, boolean userAlreadyExisting);

    void onCheckSessionFinished(boolean sessionValid);

    void onDispatchFencelogFinished(boolean success);

}

public class GeofancyNetworking {

    private GeofancyNetworkingInterface getInterface () {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.API_ENDPOINT)
                .setConverter(new StringConverter())
                .build();
        GeofancyNetworkingInterface network = restAdapter.create(GeofancyNetworkingInterface.class);
        return network;
    }

    public void doLogin(String username, String password, final GeofancyNetworkingCallback callback){
        getInterface().login(username, password, Constants.API_ORIGIN, new Callback<String>() {

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

        getInterface().signup(username, password, email, token, new Callback<String>() {
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
        getInterface().checkSession(sessionId, new Callback<String>() {
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
        getInterface().dispatchFencelog(
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
