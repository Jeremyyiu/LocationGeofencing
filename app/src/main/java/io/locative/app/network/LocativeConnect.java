package io.locative.app.network;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import io.locative.app.utils.Constants;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class LocativeConnect {

    public void updateSession(String sessionId, String token, boolean sandbox) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        JSONObject jsonBody = null;
        try {
            jsonBody = new JSONObject();
            JSONObject fcm = new JSONObject();
            fcm.put("token", token);
            fcm.put("sandbox", sandbox ? "true" : "false");
            jsonBody.put("fcm", fcm);
        } catch (Exception ex) {
            Log.e(Constants.LOG, ex.toString());
        }

        // Example payload:
        // "{\"fcm\": { \"token\": \"" + token + "\", \"sandbox\": \"" + (sandbox ? "true":"false") + "\"}}"
        if (jsonBody == null) {
            // todo: implement error handling
            return;
        }
        RequestBody body = RequestBody.create(JSON, jsonBody.toString());

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(Constants.API_ENDPOINT + "/api/session/" + sessionId)
                .put(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // TODO: Implement onFailure
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                // TODO: Implement onResponse
            }
        });
    }
}
