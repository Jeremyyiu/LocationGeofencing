package io.locative.app.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.Geofence;

import javax.inject.Inject;

import io.locative.app.LocativeApplication;
import io.locative.app.model.Geofences;
import io.locative.app.persistent.Storage;
import io.locative.app.utils.Constants;
import io.locative.app.utils.Preferences;

public class TransitionService extends Service {

    public static final String EXTRA_GEOFENCE = "fence";
    public static final String EXTRA_TRANSITION_TYPE = "transitionType";
    public static final String EXTRA_HAS_RELEVANT_URL = "hasRelevantUrl";

    @Inject
    SharedPreferences mPreferences;

    @Inject
    TriggerManager mTriggerManager;

    @Inject
    Storage mStorage;

    private Geofences.Geofence mGeofence;
    private int mTransitionType;
    private boolean mHasRelevantUrl;

    private CountDownTimer mCountDownTimer;

    public TransitionService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((LocativeApplication) getApplication()).inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getExtras() == null) {
            Log.e(Constants.LOG, "Error getting intent or extras in TransitionService!!");
            return super.onStartCommand(intent, flags, startId);
        }
        mGeofence = (Geofences.Geofence)intent.getExtras().get(EXTRA_GEOFENCE);
        mTransitionType = intent.getIntExtra(EXTRA_TRANSITION_TYPE, 0);
        mHasRelevantUrl = intent.getBooleanExtra(EXTRA_HAS_RELEVANT_URL, false);
        mCountDownTimer = new CountDownTimer(mPreferences.getInt(Preferences.TRIGGER_THRESHOLD_VALUE, Preferences.TRIGGER_THRESHOLD_VALUE_DEFAULT), 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if (mTransitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    mGeofence.currentlyEntered = 0;
                    mStorage.insertOrUpdateFence(mGeofence);
                }
                mTriggerManager.triggerTransition(mGeofence, mTransitionType, mHasRelevantUrl);
            }
        }.start();
        return super.onStartCommand(intent, flags, startId);
    }

    public void stopTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    public void onDestroy() {
        stopTimer();
        super.onDestroy();
    }
}
