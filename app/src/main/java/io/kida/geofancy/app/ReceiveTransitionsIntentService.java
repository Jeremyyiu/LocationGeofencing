package io.kida.geofancy.app;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.ArrayList;
import java.util.List;

import io.kida.geofancy.app.R;

public class ReceiveTransitionsIntentService extends IntentService {

    public static final String TRANSITION_INTENT_SERVICE = "ReceiveTransitionsIntentService";

    public ReceiveTransitionsIntentService() {
        super(TRANSITION_INTENT_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (LocationClient.hasError(intent)) {
            Log.e(TRANSITION_INTENT_SERVICE, "Location Services error: " + LocationClient.getErrorCode(intent));
            return;
        }

        int transitionType = LocationClient.getGeofenceTransition(intent);

        List<Geofence> triggeredGeofences = LocationClient.getTriggeringGeofences(intent);
        List<String> triggeredIds = new ArrayList<String>();

        for (Geofence geofence : triggeredGeofences) {
            Log.d("GEO", "onHandle:" + geofence.getRequestId());
            processGeofence(geofence, transitionType);
            triggeredIds.add(geofence.getRequestId());
        }

//        if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
//            removeGeofences(triggeredIds);
//        }
    }

    private void processGeofence(Geofence geofence, int transitionType) {

        ContentResolver resolver = this.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://" + getString(R.string.authority) + "/geofences"), null, "_id = ?", new String[]{ geofence.getRequestId() }, null);
        if (cursor.getCount() == 0) {
            return;
        }
        cursor.moveToFirst();

        String locationName = cursor.getString(cursor.getColumnIndex("name"));
        if (locationName.length() == 0) {
            locationName = "Unknown Location";
        }
        String transition = transitionType == Geofence.GEOFENCE_TRANSITION_ENTER ? "Enter " : "Exit ";

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext());

        PendingIntent openActivityIntetnt = PendingIntent.getActivity(this, 0, new Intent(this, GeofencesActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        int id = Integer.parseInt(geofence.getRequestId());

        notificationBuilder
                .setSmallIcon(R.drawable.ic_launcher)
//                .setContentTitle("Geofence id: " + id)
                .setContentTitle(locationName)
                .setContentText("Has been " + getTransitionTypeString(transitionType))
                .setVibrate(new long[]{500, 500})
                .setContentIntent(openActivityIntetnt)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(transitionType * 100 + id, notificationBuilder.build());

        Log.d("GEO", "notification built:" + id);
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

    private void removeGeofences(List<String> requestIds) {
        Intent intent = new Intent(getApplicationContext(), GeofencingService.class);

        String[] ids = new String[0];
        intent.putExtra(GeofencingService.EXTRA_REQUEST_IDS, requestIds.toArray(ids));
        intent.putExtra(GeofencingService.EXTRA_ACTION, GeofencingService.Action.REMOVE);

        startService(intent);
    }
}