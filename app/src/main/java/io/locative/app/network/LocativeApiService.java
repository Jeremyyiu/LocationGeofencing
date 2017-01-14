package io.locative.app.network;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface LocativeApiService {

    @GET("/api/session/{session}")
    void checkSession(
            @Path("session") String sessionId,
            Callback<String> callback
    );

    @GET("/api/account")
    void getAccount(
            @Query("sessionId") String sessionId,
            Callback<String> callback
    );

    @FormUrlEncoded
    @POST("/api/fencelogs/{session}")
    void dispatchFencelog(
            @Path("session") String sessionId,
            @Field("longitude") double longitude,
            @Field("latitude") double latitude,
            @Field("locationId") String locationId,
            @Field("httpUrl") String httpUrl,
            @Field("httpMethod") String httpMethod,
            @Field("httpResponseCode") String httpResponseCode,
            @Field("eventType") String eventType,
            @Field("fenceType") String fenceType,
            @Field("origin") String origin,
            Callback<String> callback
    );

    @GET("/api/geofences")
    void getGeofences(
            @Query("sessionId") String sessionId,
            Callback<String> callback
    );

    @GET("/api/fencelogs/{session}")
    void getFencelogs(
            @Path("session") String sessionId,
            @Query("limit") int limit,
            Callback<String> callback
    );

    @GET("/api/notifications")
    void getNotifications(
            @Query("sessionId") String sessionId,
            Callback<String> callback
    );
}
