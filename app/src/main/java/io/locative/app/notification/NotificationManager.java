package io.locative.app.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

import io.locative.app.LocativeApplication;
import io.locative.app.R;
import io.locative.app.utils.Preferences;
import io.locative.app.utils.ResourceUtils;
import io.locative.app.view.GeofencesActivity;
import com.google.android.gms.location.Geofence;

import javax.inject.Inject;

import static io.locative.app.LocativeApplication.getApplication;

public class NotificationManager {

    private Context mContext;

    @Inject
    SharedPreferences mPrefs;

    @Inject
    ResourceUtils mResourceUtils;

    public NotificationManager(Context context) {
        ((LocativeApplication) getApplication()).inject(this);
        mContext = context;
    }

    public void showNotification(String title, String body) {
        notify(getDefaultBuilder(title).setContentText(body), 0);
    }

    public void showNotification(String title, int id, int transitionType) {
        NotificationCompat.Builder builder = getDefaultBuilder(title)
                .setContentText("Has been " + getTransitionTypeString(transitionType));
        notify(builder, transitionType * 100 + id);
    }

    private void notify(NotificationCompat.Builder builder, int id) {
        android.app.NotificationManager notificationManager = getDefaultNotificationManager();
        if (mPrefs.getBoolean(Preferences.NOTIFICATION_SHOW_ONLY_LATEST, false)) {
            notificationManager.cancelAll();
        }
        notificationManager.notify(id, builder.build());
    }

    private NotificationCompat.Builder getDefaultBuilder(String title) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(0x29aae1)
                .setContentTitle(title)
                .setVibrate(new long[]{500, 500})
                .setContentIntent(getActivityIntent())
                .setAutoCancel(true);

        if (mPrefs.getBoolean(Preferences.NOTIFICATION_SOUND, false)) {
            builder.setSound(mResourceUtils.rawResourceUri(R.raw.notification));
        }

        return builder;
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
                return mContext.getString(R.string.entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return mContext.getString(R.string.left);
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return mContext.getString(R.string.entered);
            default:
                return mContext.getString(R.string.visited);
        }
    }
}