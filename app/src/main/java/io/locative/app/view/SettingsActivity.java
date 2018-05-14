package io.locative.app.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import butterknife.BindView;
import io.locative.app.LocativeApplication;
import io.locative.app.R;
import io.locative.app.utils.Preferences;

public class SettingsActivity extends BaseActivity {

    @BindView(R.id.notification_success_switch)
    Switch mNotificationSuccessSwitch;

    @BindView(R.id.notification_fail_switch)
    Switch mNotificationFailSwitch;

    @BindView(R.id.notification_only_latest_switch)
    Switch mNotificationOnlyLatestSwitch;

    @BindView(R.id.notification_sound_switch)
    Switch mNotificationSoundSwitch;

    @BindView(R.id.trigger_threshold_enabled_switch)
    Switch mTriggerThresholdEnabled;

    @BindView(R.id.trigger_threshold_seekbar)
    SeekBar mTriggerThresholdSeekBar;

    @BindView(R.id.trigger_threshold_notice)
    TextView mTriggerThresholdNotice;


    private void updateThresholdNotice() {
        if (mTriggerThresholdEnabled.isChecked()) {
            mTriggerThresholdNotice.setText(getString(R.string.trigger_threshold_notice, mTriggerThresholdSeekBar.getProgress()));
            return;
        }
        mTriggerThresholdNotice.setText(getString(R.string.trigger_threshold_notice_disabled));
    }

    private void setupThreshold() {
        mTriggerThresholdEnabled.setChecked(mPrefs.getBoolean(Preferences.TRIGGER_THRESHOLD_ENABLED, false));
        mTriggerThresholdSeekBar.setEnabled(mTriggerThresholdEnabled.isChecked());
        mTriggerThresholdSeekBar.setProgress(mPrefs.getInt(Preferences.TRIGGER_THRESHOLD_VALUE, Preferences.TRIGGER_THRESHOLD_VALUE_DEFAULT) / 1000);
        mTriggerThresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateThresholdNotice();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mTriggerThresholdEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mTriggerThresholdSeekBar.setEnabled(b);
                updateThresholdNotice();
            }
        });
        updateThresholdNotice();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((LocativeApplication) getApplication()).getComponent().inject(this);

        mNotificationSuccessSwitch.setChecked(mPrefs.getBoolean(Preferences.NOTIFICATION_SUCCESS, false));
        mNotificationFailSwitch.setChecked(mPrefs.getBoolean(Preferences.NOTIFICATION_FAIL, false));
        mNotificationOnlyLatestSwitch.setChecked(mPrefs.getBoolean(Preferences.NOTIFICATION_SHOW_ONLY_LATEST, false));
        mNotificationSoundSwitch.setChecked(mPrefs.getBoolean(Preferences.NOTIFICATION_SOUND, false));

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                save(true);
                return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupThreshold();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_settings;
    }

    @Override
    protected String getToolbarTitle() {
        return "Settings";
    }

    @Override
    protected int getMenuResourceId() {
        return R.menu.settings;
    }

    private void save(boolean finish) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(Preferences.NOTIFICATION_SUCCESS, mNotificationSuccessSwitch.isChecked());
        editor.putBoolean(Preferences.NOTIFICATION_FAIL, mNotificationFailSwitch.isChecked());
        editor.putBoolean(Preferences.NOTIFICATION_SHOW_ONLY_LATEST, mNotificationOnlyLatestSwitch.isChecked());
        editor.putBoolean(Preferences.NOTIFICATION_SOUND, mNotificationSoundSwitch.isChecked());
        editor.putBoolean(Preferences.TRIGGER_THRESHOLD_ENABLED, mTriggerThresholdEnabled.isChecked());
        editor.putInt(Preferences.TRIGGER_THRESHOLD_VALUE, mTriggerThresholdSeekBar.getProgress() * 1000);
        editor.apply();

        if (finish) {
            finish();
        }
    }
}
