package io.locative.app.network;


import java.util.List;

import io.locative.app.model.Fencelog;
import io.locative.app.model.Geofences;
import io.locative.app.model.Notification;

// TODO clean this up, bad design

public interface LocativeNetworkingCallback {
    void onDispatchFencelogFinished(boolean success);

    void onGetGeoFencesFinished(List<Geofences.Geofence> fences);

    void onGetFencelogsFinished(List<Fencelog> fencelogs);

    void onGetNotificationsFinished(List<Notification> notifications);
}
