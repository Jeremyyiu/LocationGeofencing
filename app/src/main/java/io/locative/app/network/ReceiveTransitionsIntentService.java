package io.locative.app.network;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import javax.inject.Inject;

import io.locative.app.LocativeApplication;
import io.locative.app.R;
import io.locative.app.model.EventType;
import io.locative.app.model.Geofences;
import io.locative.app.persistent.GeofenceProvider;

public class ReceiveTransitionsIntentService extends IntentService {

    public static final String TRANSITION_INTENT_SERVICE = "ReceiveTransitionsIntentService";

    private final String TAG = "TRANSITION";

    @Inject
    LocativeApiWrapper mLocativeNetworkingWrapper;

    @Inject
    SessionManager mSessionManager;

    @Inject
    RequestManager mRequestManager;

    public ReceiveTransitionsIntentService() {
        super(TRANSITION_INTENT_SERVICE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((LocativeApplication) getApplication()).inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Location Services error: " + geofencingEvent.getErrorCode());
            return;
        }
        Log.i(TAG, "Location Services geofencingEvent: " + geofencingEvent);

        int transitionType = geofencingEvent.getGeofenceTransition();
        List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();

        for (Geofence geofence : triggeredGeofences) {
            Log.d(TAG, "onHandle:" + geofence.getRequestId());
            processGeofence(geofence, transitionType);
        }
    }

    private void processGeofence(Geofence geofence, int transitionType) {

        ContentResolver resolver = this.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://" + getString(R.string.authority) + "/geofences"), null, "_id = ?", new String[]{geofence.getRequestId()}, null);
        if (cursor == null || cursor.getCount() == 0) {
            return; // TODO: Handle errors here
        }

        cursor.moveToFirst();
        Geofences.Geofence fence = GeofenceProvider.fromCursor(cursor);
        cursor.close();

        mRequestManager.dispatch(fence, getEventType(transitionType));
    }

    private LocativeApplication getApp() {
        return (LocativeApplication) getApplication();
    }

    @Nullable
    private EventType getEventType(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return EventType.ENTER;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return EventType.EXIT;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return null;
            default:
                return null;
        }

    }

    private void removeGeofences(List<String> requestIds) {
        Intent intent = new Intent(getApplicationContext(), LocativeService.class);

        String[] ids = new String[0];
        intent.putExtra(LocativeService.EXTRA_REQUEST_IDS, requestIds.toArray(ids));
        intent.putExtra(LocativeService.EXTRA_ACTION, LocativeService.Action.REMOVE);

        startService(intent);
    }
}