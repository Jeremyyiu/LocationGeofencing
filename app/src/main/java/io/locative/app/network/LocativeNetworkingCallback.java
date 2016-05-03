package io.locative.app.network;


import java.util.List;
import io.locative.app.model.Geofences;

public interface LocativeNetworkingCallback {

    void onLoginFinished(boolean success, String sessionId);

    void onSignupFinished(boolean success, boolean userAlreadyExisting);

    void onCheckSessionFinished(boolean sessionValid);

    void onDispatchFencelogFinished(boolean success);

    void onGetGeoFencesFinished(List<Geofences.Geofence> fences);

}
