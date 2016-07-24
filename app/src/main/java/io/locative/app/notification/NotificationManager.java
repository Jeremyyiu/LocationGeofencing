package io.locative.app.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.location.Geofence;

import io.locative.app.R;
import io.locative.app.view.GeofencesActivity;

public class NotificationManager {

    private Context mContext;

    public NotificationManager(Context context) {
        mContext = context;
    }

    public void showNotification(String title) {
        notify(getDefaultBuilder(title), 0);
    }

    public void showNotification(String title, int id, int transitionType) {
        NotificationCompat.Builder builder = getDefaultBuilder(title)
                .setContentText("Has been " + getTransitionTypeString(transitionType));
        notify(builder, transitionType * 100 + id);
    }

    private void notify(NotificationCompat.Builder builder, int id) {
        android.app.NotificationManager notificationManager = getDefaultNotificationManager();
        notificationManager.cancelAll();
        notificationManager.notify(id, builder.build());
    }

    private NotificationCompat.Builder getDefaultBuilder(String title) {
        return new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(0x29aae1)
                .setContentTitle(title)
                .setVibrate(new long[]{500, 500})
                .setContentIntent(getActivityIntent())
                .setAutoCancel(true);
    }

    private android.app.NotificationManager getDefaultNotificationManager() {
        return (android.app.NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private PendingIntent getActivityIntent() {
        Intent intent = new Intent(mContext, GeofencesActivity.class);
        intent.putExtra(GeofencesActivity.NOTIFICATION_CLICK, true);
        return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
}
