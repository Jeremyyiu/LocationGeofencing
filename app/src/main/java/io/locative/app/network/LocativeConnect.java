package io.locative.app.network;

import java.io.IOException;

import io.locative.app.utils.Constants;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by kida on 2/1/17.
 */

public class LocativeConnect {

    public void updateSession(String sessionId, String token, boolean sandbox) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, "{\"fcm\": { \"token\": \"" + token + "\", \"sandbox\": \"" + (sandbox ? "true":"false") + "\"}}");

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
