package io.kida.geofancy.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DialerFilter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.util.prefs.PreferenceChangeEvent;

import io.kida.geofancy.app.R;
import retrofit.Callback;
import retrofit.http.Query;

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
    private ProgressDialog mProgressDialog = null;
    private GeofancyNetworking mNetworking;
    private GeofancyNetworkingCallback mNetworkingCallback;

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
        adjustUiToLoginState();
        SharedPreferences prefs = getPrefs();

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

        mNetworking = new GeofancyNetworking();
        final Context ctx = this;
        mNetworkingCallback = new GeofancyNetworkingCallback() {
            @Override
            public void onLoginFinished(boolean success, String sessionId) {
                mProgressDialog.dismiss();
                if (!success) {
                    simpleAlert("Username or Password incorrect. Please try again.");
                    return;
                }
                simpleAlert("Login successful! Your Fencelogs will now be visible when you log in at https://my.geofancy.com.");
                Log.d(Constants.LOG, "Login success with SessionID: " + sessionId);
                getPrefs().edit().putString(Constants.SESSION_ID, sessionId).commit();
                adjustUiToLoginState();
            }

            @Override
            public void onSignupFinished(boolean success, String sessionId) {

            }
        };
    }

    @Click(R.id.global_auth_switch)
    void switchHttpAuth(){
        mGlobalHttpAuthUsername.setEnabled(mGlobalHttpAuthSwitch.isChecked());
        mGlobalHttpAuthPassword.setEnabled(mGlobalHttpAuthSwitch.isChecked());
    }

    @Click(R.id.login_button)
    void loginOrLogout(){

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.loading);
        mProgressDialog.setMessage("Please wait…");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();

        if (!isLoggedIn()) {
            mNetworking.doLogin(mAccountUsernameText.getText().toString(), mAccountPasswordText.getText().toString(), mNetworkingCallback);
        } else {
            // TODO: Implement Logout via API (needs to be implemented on Server-Side)
            getPrefs().edit().putString(Constants.SESSION_ID, null).commit();
            adjustUiToLoginState();
            mProgressDialog.dismiss();
        }
    }

    private SharedPreferences getPrefs(){
        return this.getPreferences(MODE_PRIVATE);
    }

    private boolean isLoggedIn(){
        String sessionId = getPrefs().getString(Constants.SESSION_ID, null);
        if (sessionId != null) {
            return sessionId.length() > 0;
        }
        return false;
    }

    private void adjustUiToLoginState(){
        int visibility = LinearLayout.VISIBLE;

        if (isLoggedIn()) {
            // User is logged in
            visibility = LinearLayout.GONE;
        }

        mAccountUsernameText.setVisibility(visibility);
        mAccountPasswordText.setVisibility(visibility);
        mLoginButton.setText((visibility == LinearLayout.VISIBLE) ? "Login" : "Logout");
        mSignupButton.setVisibility(visibility);
        mLostpassButton.setVisibility(visibility);
    }

    private void simpleAlert(String msg){
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setNeutralButton("OK", null)
                .show();
    }

    private void save(boolean finish){
        SharedPreferences.Editor editor = getPrefs().edit();
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
