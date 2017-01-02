package io.locative.app.network;

import io.locative.app.utils.Constants;

/**
 * Created by kida on 1/1/17.
 */

public class SessionUpdatePayload {
    public class FCM {

        private FCM(String token) {
            this.token = token;
        }

        public String token;
        public String sandbox = "false";
    }

    public SessionUpdatePayload(String token) {
        this.fcm = new FCM(token);
    }

    public final String origin = Constants.API_ORIGIN;
    public FCM fcm;
}
