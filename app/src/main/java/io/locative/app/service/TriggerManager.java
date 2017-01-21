package io.locative.app.service;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.Geofence;

import java.util.Random;

import javax.inject.Inject;

import io.locative.app.LocativeApplication;
import io.locative.app.model.EventType;
import io.locative.app.model.Geofences;
import io.locative.app.network.RequestManager;
import io.locative.app.notification.NotificationManager;
import io.locative.app.utils.Constants;
import io.locative.app.utils.Preferences;

import static io.locative.app.LocativeApplication.getApplication;

public class TriggerManager {

    @Inject
    RequestManager mRequestManager;

    @Inject
    SharedPreferences mPreferences;

    @Inject
    NotificationManager mNotificationManager;

    public TriggerManager() {
        ((LocativeApplication) getApplication()).inject(this);
    }

    public void triggerTransition(Geofences.Geofence fence, int transitionType, boolean hasRelevantUrl) {
        if (!hasRelevantUrl) {
            // not global url is set, bail out and show classic notification
            Log.d(Constants.LOG, "Presenting classic notification for " + fence.uuid);
            if (mPreferences.getBoolean(Preferences.NOTIFICATION_SUCCESS, false)) {
                mNotificationManager.showNotification(
                        fence.getRelevantId(),
                        new Random().nextInt(),
                        transitionType
                );
            }
            Log.d(Constants.LOG, "Dispatching Fencelog for " + fence.uuid);
            mRequestManager.dispatchFencelog(
                    fence,
                    getEventType(transitionType),
                    null,
                    null,
                    0
            );
        } else {
            Log.d(Constants.LOG, "Dispatching Request for " + fence.uuid);
            mRequestManager.dispatch(fence, getEventType(transitionType));
        }
    }

    @Nullable
    private EventType getEventType(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return EventType.ENTER;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return EventType.EXIT;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return EventType.ENTER;
            default:
                return null;
        }

    }
}
