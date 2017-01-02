package io.locative.app.notification;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by kida on 1/1/17.
 */

public class NotificationMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Implement notification handling
        Log.d("Locative", "From: " + remoteMessage.getFrom());
        Log.d("Locative", "Notification Message Body: " + remoteMessage.getNotification().getBody());
    }
}
