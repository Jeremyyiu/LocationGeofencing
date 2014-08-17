package io.kida.geofancy.app;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.cast.Cast;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.mime.MimeUtil;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Created by mkida on 16/08/2014.
 */

interface  GeofancyNetworkingInterface {

    @GET("/api/session")
    public void login(@Query("username") String username, @Query("password") String password, @Query("origin") String origin, Callback<String> callback);

    @POST("/api/signup")
    public void signup(@Field("username") String username, @Field("password") String password, @Field("email") String email, @Field("token") String token, Callback<String> callback);
}

interface  GeofancyNetworkingCallback {

    public void onLoginFinished(boolean success, String sessionId);

    public void onSignupFinished(boolean success, String sessionId);

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
                    Log.e(Constants.LOG, "Caught Exception: " + e);
                } finally {
                    callback.onLoginFinished((sessionId.length() > 0), sessionId);
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
            token = AeSimpleSHA1.SHA1(username + ":" + password + email);
        } catch (Exception e) {
            Log.e(Constants.LOG, "Caught Exception: " + e);
        }

        getInterface().signup(username, password, email, token, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                Log.d(Constants.LOG, "Signup Success: " + s);
                String sessionId = null;
                try {
                    JSONObject json = new JSONObject(s);
                    sessionId = json.getString("sessionId");
                } catch (Exception e) {
                    Log.e(Constants.LOG, "Caught Exception: " + e);
                } finally {
                    callback.onSignupFinished(true, sessionId);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(Constants.LOG, "Signup Error: " + error);
                callback.onSignupFinished(false, null);
            }
        });
    }
}
