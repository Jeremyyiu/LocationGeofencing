package io.locative.app.network;


public interface LocativeNetworkingCallback {

    void onLoginFinished(boolean success, String sessionId);

    void onSignupFinished(boolean success, boolean userAlreadyExisting);

    void onCheckSessionFinished(boolean sessionValid);

    void onDispatchFencelogFinished(boolean success);

}
