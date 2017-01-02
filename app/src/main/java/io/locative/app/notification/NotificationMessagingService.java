package io.locative.app.notification;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.locative.app.utils.Constants;

/**
 * Created by kida on 1/1/17.
 */

public class NotificationMessagingService extends FirebaseMessagingService {

    final NotificationManager notificationManager = new NotificationManager(this);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(Constants.LOG, "From: " + remoteMessage.getFrom());
        Log.d(Constants.LOG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        // TODO: Implement notifications while app is running in foreground
    }
}
