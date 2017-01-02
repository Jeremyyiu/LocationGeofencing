package io.locative.app.network;

import com.google.gson.JsonObject;

/**
 * Created by kida on 2/1/17.
 */

public class FcmPayloadBuilder {
    private String token;
    private boolean sandbox;

    public FcmPayloadBuilder(String token, boolean sandbox) {
        this.token = token;
        this.sandbox = sandbox;
    }

    public JsonObject build() {
        JsonObject payload = new JsonObject();
        JsonObject fcm = new JsonObject();
        fcm.addProperty("token", token);
        fcm.addProperty("sandbox", sandbox);
        payload.add("fcm", fcm);
        return payload;
    }
}
