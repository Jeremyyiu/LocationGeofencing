package io.kida.geofancy.app;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

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
import java.lang.reflect.Type;
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

}

interface  GeofancyNetworkingCallback {

    public void onLoginFinished(boolean success);

}

public class GeofancyNetworking {

    public void doLogin(String username, String password, final GeofancyNetworkingCallback callback){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.API_ENDPOINT)
                .setConverter(new StringConverter())
                .build();

        GeofancyNetworkingInterface network = restAdapter.create(GeofancyNetworkingInterface.class);
        network.login(username, password, Constants.API_ORIGIN, new Callback<String>() {

            @Override
            public void success(String string, Response response) {
                Log.d(Constants.LOG, "Login Success: " + string);
                callback.onLoginFinished(false);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(Constants.LOG, "Login Error: " + error);
                callback.onLoginFinished(false);
            }
        });
    }
}
