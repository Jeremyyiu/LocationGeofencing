package io.locative.app.view;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import co.intentservice.chatui.ChatView;
import co.intentservice.chatui.models.ChatMessage;
import io.locative.app.R;
import io.locative.app.model.Notification;
import io.locative.app.network.LocativeNetworkingAdapter;

/**
 * Created by kida on 1/1/17.
 */

public class NotificationsFragment extends Fragment {
    public static final String TAG = "fragment.notifications";

    private List<Notification> mNotifications;

    private ChatView getChatView () {
        return (ChatView) getView().findViewById(R.id.chat_view);
    }

    public void refresh() {
        if (getActivity() != null) {
            for (Notification notification : mNotifications) {
                ChatMessage message = new ChatMessage(
                        notification.message,
                        notification.timestamp,
                        ChatMessage.Type.RECEIVED
                );
                getChatView().addMessage(message);
            }
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

        getChatView().findViewById(R.id.input_frame).setVisibility(View.INVISIBLE);
        getChatView().findViewById(R.id.sendButton).setVisibility(View.INVISIBLE);

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
