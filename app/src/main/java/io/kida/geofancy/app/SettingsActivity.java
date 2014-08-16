package io.kida.geofancy.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.prefs.PreferenceChangeEvent;

import io.kida.geofancy.app.R;

@EActivity(R.layout.activity_settings)

@OptionsMenu(R.menu.settings)

public class SettingsActivity extends Activity {

    private static String HTTP_URL = "httpUrl";
    private static String HTTP_METHOD = "httpMethod";
    private static String HTTP_AUTH = "httpAuth";
    private static String HTTP_USERNAME = "httpUsername";
    private static String HTTP_PASSWORD = "httpPassword";
    private static String NOTIFICATION_SUCCESS = "notificationSuccess";
    private static String NOTIFICATION_FAIL = "notificationFailure";
    private static String NOTIFICATION_SOUND = "notificationSound";

    private int mHttpMethod = 0;

    @ViewById(R.id.global_http_url)
    EditText mUrlText;

    @ViewById(R.id.global_http_method_button)
    Button mGlobalHttpMethodButton;

    @ViewById(R.id.global_auth_switch)
    Switch mGlobalHttpAuthSwitch;

    @ViewById(R.id.global_auth_username)
    EditText mGlobalHttpAuthUsername;

    @ViewById(R.id.global_auth_password)
    EditText mGlobalHttpAuthPassword;

    @ViewById(R.id.send_test_button)
    Button mSendTestButton;

    @ViewById(R.id.notification_success_switch)
    Switch mNotificationSuccessSwitch;

    @ViewById(R.id.notification_fail_switch)
    Switch mNotificationFailSwitch;

    @ViewById(R.id.notification_sound_switch)
    Switch mNotificationSoundSwitch;

    @ViewById(R.id.account_username_text)
    EditText mAccountUsernameText;

    @ViewById(R.id.account_password_text)
    EditText mAccountPasswordText;

    @ViewById(R.id.login_button)
    Button mLoginButton;

    @ViewById(R.id.signup_button)
    Button mSignupButton;

    @ViewById(R.id.lostpass_button)
    Button mLostpassButton;

    @OptionsItem(R.id.action_settings)
    void save(){
        save(true);
    }

    @AfterViews
    void setup(){
        SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);

        mUrlText.setText(prefs.getString(HTTP_URL, null));
        mGlobalHttpMethodButton.setText(
                prefs.getInt(HTTP_METHOD, 0) == 0 ? "POST" : "GET"
        );
        mGlobalHttpAuthSwitch.setChecked(
                prefs.getBoolean(HTTP_AUTH, false)
        );
        switchHttpAuth();

        mGlobalHttpAuthUsername.setText(prefs.getString(HTTP_USERNAME, null));
        mGlobalHttpAuthPassword.setText(prefs.getString(HTTP_PASSWORD, null));
        mNotificationSoundSwitch.setChecked(prefs.getBoolean(NOTIFICATION_SUCCESS, false));
        mNotificationFailSwitch.setChecked(prefs.getBoolean(NOTIFICATION_FAIL, false));
        mNotificationSoundSwitch.setChecked(prefs.getBoolean(NOTIFICATION_SOUND, false));

    }

    @Click(R.id.global_auth_switch)
    void switchHttpAuth(){
        mGlobalHttpAuthUsername.setEnabled(mGlobalHttpAuthSwitch.isChecked());
        mGlobalHttpAuthPassword.setEnabled(mGlobalHttpAuthSwitch.isChecked());
    }

    private void save(boolean finish){
        SharedPreferences.Editor editor = this.getPreferences(MODE_PRIVATE).edit();
        editor.putString(HTTP_URL, mUrlText.getText().toString());
        editor.putInt(HTTP_METHOD, mHttpMethod);
        editor.putBoolean(HTTP_AUTH, mGlobalHttpAuthSwitch.isChecked());
        editor.putString(HTTP_USERNAME, mGlobalHttpAuthUsername.getText().toString());
        editor.putString(HTTP_PASSWORD, mGlobalHttpAuthPassword.getText().toString());
        editor.putBoolean(NOTIFICATION_SUCCESS, mNotificationSuccessSwitch.isChecked());
        editor.putBoolean(NOTIFICATION_FAIL, mNotificationFailSwitch.isChecked());
        editor.putBoolean(NOTIFICATION_SOUND, mNotificationSoundSwitch.isChecked());

        editor.commit();

        if (finish) {
            finish();
        }
    }
}
