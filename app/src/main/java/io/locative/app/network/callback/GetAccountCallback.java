package io.locative.app.network.callback;

/**
 * Created by kida on 5/1/17.
 */

public interface GetAccountCallback {
    void onSuccess(String username, String email, String avatarUrl);
    void onFailure();
}
