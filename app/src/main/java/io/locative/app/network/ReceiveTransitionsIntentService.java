package io.locative.app.network;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import javax.inject.Inject;

import io.locative.app.LocativeApplication;
import io.locative.app.R;
import io.locative.app.model.EventType;
import io.locative.app.model.Fencelog;
import io.locative.app.persistent.GeofenceProvider;
import io.locative.app.view.GeofencesActivity;

public class ReceiveTransitionsIntentService extends IntentService {

    public static final String TRANSITION_INTENT_SERVICE = "ReceiveTransitionsIntentService";

    private final String TAG = "TRANSITION";

    @Inject
    LocativeApiWrapper mLocativeNetworkingWrapper;

    @Inject
    SessionManager mSessionManager;

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
//            String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    geofencingEvent.getErrorCode());
//            Log.e(TAG, errorMessage);
            Log.e(TAG, "Location Services error: " + geofencingEvent.getErrorCode());
            return;
        }
        Log.i(TAG, "Location Services geofencingEvent: " + geofencingEvent);

        int transitionType = geofencingEvent.getGeofenceTransition();
        List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();
        //List<String> triggeredIds = new ArrayList<String>();

        for (Geofence geofence : triggeredGeofences) {
            Log.d(TAG, "onHandle:" + geofence.getRequestId());
            processGeofence(geofence, transitionType);
            //triggeredIds.add(geofence.getRequestId());
        }

//        if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
//            removeGeofences(triggeredIds);
//        }
    }

    private void processGeofence(Geofence geofence, int transitionType) {

        ContentResolver resolver = this.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://" + getString(R.string.authority) + "/geofences"), null, "_id = ?", new String[]{geofence.getRequestId()}, null);
        if (cursor == null || cursor.getCount() == 0) {
            return;
        }
        cursor.moveToFirst();

        String customId = cursor.getString(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_CUSTOMID));
        float latitude = cursor.getFloat(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_LATITUDE));
        float longitude = cursor.getFloat(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_LONGITUDE));
        String locationName = cursor.getString(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_CUSTOMID));
        if (locationName.length() == 0) {
            locationName = "Unknown Location";
        }
        cursor.close();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        Intent intent = new Intent(this, GeofencesActivity.class);
        intent.putExtra(GeofencesActivity.NOTIFICATION_CLICK, true);
        PendingIntent openActivityIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        int id = Integer.parseInt(geofence.getRequestId());

        notificationBuilder
             //   .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(0x29aae1)
//                .setContentTitle("Geofence id: " + id)
                .setContentTitle(locationName)
                .setContentText("Has been " + getTransitionTypeString(transitionType))
                .setVibrate(new long[]{500, 500})
                .setContentIntent(openActivityIntent)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancelAll();
        nm.notify(transitionType * 100 + id, notificationBuilder.build());

        Log.d(TAG, "notification built:" + id);
        reportFenceLogToApi(customId, latitude, longitude, getEventType(transitionType));
        Log.d(TAG, "fence logged:" + customId);

    }

    private void reportFenceLogToApi(String customId, float latitude, float longitude, @Nullable EventType eventType) {
        String sessionId = mSessionManager.getSessionId();
        if (sessionId != null && eventType != null) {
            Fencelog fencelog = new Fencelog();
            fencelog.locationId = customId;
            fencelog.latitude = latitude;
            fencelog.longitude = longitude;
            fencelog.eventType = eventType;
            fencelog.origin = Build.MODEL;
            mLocativeNetworkingWrapper.doDispatchFencelog(sessionId, fencelog, new LocativeNetworkingAdapter() {
                @Override
                public void onDispatchFencelogFinished(boolean success) {
                    // nothing to do here
                }
            });
        }
    }

    private LocativeApplication getApp() {
        return (LocativeApplication) getApplication();
    }


    private String getTransitionTypeString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "left";
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return "dwelled";
            default:
                return "happened an unknown event.";
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