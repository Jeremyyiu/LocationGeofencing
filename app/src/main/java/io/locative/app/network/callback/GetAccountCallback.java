package io.locative.app.network.callback;

public interface GetAccountCallback {
    void onSuccess(String username, String email, String avatarUrl);
    void onFailure();
}
