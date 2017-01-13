package io.locative.app.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;
import io.locative.app.R;
import io.locative.app.model.Notification;
import io.locative.app.network.LocativeNetworkingAdapter;

public class NotificationsFragment extends Fragment {
    public static final String TAG = "fragment.notifications";

    private List<Notification> mNotifications;

    private ChatView getChatView () {
        View view = getView();
        if (view == null) {
            return null;
        }
        return (ChatView)view.findViewById(R.id.chat_view);
    }
    private LinearLayout getProgressBar() {
        View view = getView();
        if (view == null) {
            return null;
        }
        return (LinearLayout)view.findViewById(R.id.loader);
    }

    public void refresh() {
        if (getActivity() != null) {
            if (mNotifications.isEmpty()) {
                // No notications available, show placeholder notifications like on iOS
                ChatMessage message = new ChatMessage(
                        getString(R.string.no_notifications),
                        System.currentTimeMillis(),
                        ChatMessage.Type.RECEIVED
                );

                final ChatView chatView = getChatView();
                if (chatView != null) {
                    chatView.addMessage(message);
                }
            } else {
                // Build notification history and present it
                for (Notification notification : mNotifications) {
                    ChatMessage message = new ChatMessage(
                            notification.message,
                            notification.timestamp,
                            ChatMessage.Type.RECEIVED
                    );

                    final ChatView chatView = getChatView();
                    if (chatView != null) {
                        chatView.addMessage(message);
                    }
                }
            }

        }
        // Remove placeholder view
        final LinearLayout progressBar = getProgressBar();
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ChatView chatView = getChatView();
        if (chatView != null) {
            chatView.findViewById(R.id.input_frame).setVisibility(View.INVISIBLE);
            chatView.findViewById(R.id.sendButton).setVisibility(View.INVISIBLE);
        }

        final LinearLayout progressBar = getProgressBar();
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        GeofencesActivity ga = (GeofencesActivity)getActivity();
        ga.mFabButton.hide();
        ga.mLocativeNetworkingWrapper.getNotifications(ga.mSessionManager.getSessionId(), new LocativeNetworkingAdapter() {
            @Override
            public void onGetNotificationsFinished(List<Notification> notifications) {
                mNotifications = notifications;
                refresh();
            }
        });
    }
}
