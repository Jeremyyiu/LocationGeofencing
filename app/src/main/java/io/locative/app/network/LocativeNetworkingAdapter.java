package io.locative.app.network;

import java.util.List;

import io.locative.app.model.Fencelog;
import io.locative.app.model.Geofences;
import io.locative.app.model.Notification;

public class LocativeNetworkingAdapter implements LocativeNetworkingCallback{
    @Override
    public void onLoginFinished(boolean success, String sessionId) {

    }

    @Override
    public void onSignupFinished(boolean success, boolean userAlreadyExisting) {

    }

    @Override
    public void onCheckSessionFinished(boolean sessionValid) {

    }

    @Override
    public void onDispatchFencelogFinished(boolean success) {

    }

    @Override
    public void onGetGeoFencesFinished(List<Geofences.Geofence> fences) {

    }

    @Override
    public void onGetFencelogsFinished(List<Fencelog> fencelogs) {

    }

    @Override
    public void onGetNotificationsFinished(List<Notification> notifications) {

    }
}
