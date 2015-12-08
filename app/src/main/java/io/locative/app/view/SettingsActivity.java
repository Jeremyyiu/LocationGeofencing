package io.locative.app.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import io.locative.app.GeofancyApplication;
import io.locative.app.R;
import io.locative.app.model.EventType;
import io.locative.app.model.Fencelog;
import io.locative.app.network.GeofancyNetworkingCallback;
import io.locative.app.network.GeofancyNetworkingWrapper;
import io.locative.app.utils.Constants;

public class SettingsActivity extends BaseActivity {

    private static String HTTP_URL = "httpUrl";
    private static String HTTP_METHOD = "httpMethod";
    private static String HTTP_AUTH = "httpAuth";
    private static String HTTP_USERNAME = "httpUsername";
    private static String HTTP_PASSWORD = "httpPassword";
    private static String NOTIFICATION_SUCCESS = "notificationSuccess";
    private static String NOTIFICATION_FAIL = "notificationFailure";
    private static String NOTIFICATION_SOUND = "notificationSound";

    private Constants.HttpMethod mHttpMethod = Constants.HttpMethod.POST;

    private ProgressDialog mProgressDialog = null;
    private GeofancyNetworkingCallback mNetworkingCallback;

    @Bind(R.id.global_http_url)
    EditText mUrlText;

    @Bind(R.id.global_http_method_button)
    Button mGlobalHttpMethodButton;

    @Bind(R.id.global_auth_switch)
    Switch mGlobalHttpAuthSwitch;

    @Bind(R.id.global_auth_username)
    EditText mGlobalHttpAuthUsername;

    @Bind(R.id.global_auth_password)
    EditText mGlobalHttpAuthPassword;

    @Bind(R.id.send_test_button)
    Button mSendTestButton;

    @Bind(R.id.notification_success_switch)
    Switch mNotificationSuccessSwitch;

    @Bind(R.id.notification_fail_switch)
    Switch mNotificationFailSwitch;

    @Bind(R.id.notification_sound_switch)
    Switch mNotificationSoundSwitch;

    @Bind(R.id.account_username_text)
    EditText mAccountUsernameText;

    @Bind(R.id.account_password_text)
    EditText mAccountPasswordText;

    @Bind(R.id.login_button)
    Button mLoginButton;

    @Bind(R.id.signup_button)
    Button mSignupButton;

    @Bind(R.id.lostpass_button)
    Button mLostpassButton;

    @Inject
    GeofancyNetworkingWrapper mGeofancyNetworkingWrapper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeofancyApplication.inject(this);

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
        mHttpMethod = (Constants.HttpMethod.POST.ordinal() == getPrefs().getInt(HTTP_METHOD, 0)) ? Constants.HttpMethod.POST : Constants.HttpMethod.GET;

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
                getApp().setSessionId(sessionId);
                adjustUiToLoginState();
            }

            @Override
            public void onSignupFinished(boolean success, boolean userAlreadyExisting) {

            }

            @Override
            public void onCheckSessionFinished(boolean sessionValid) {
                if (!sessionValid) {
                    getApp().clearSession();
                    adjustUiToLoginState();
                }
            }

            @Override
            public void onDispatchFencelogFinished(boolean success) {
                mProgressDialog.dismiss();
                simpleAlert(success ? "Your Fencelog was submitted successfully!" : "There was an error submitting your Fencelog.");
            }
        };


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
    public void onResume() {
        super.onResume();
        mGeofancyNetworkingWrapper.doCheckSession(getApp().getSessionId(), mNetworkingCallback);
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

    @OnClick(R.id.global_auth_switch)
    public void switchHttpAuth() {
        mGlobalHttpAuthUsername.setEnabled(mGlobalHttpAuthSwitch.isChecked());
        mGlobalHttpAuthPassword.setEnabled(mGlobalHttpAuthSwitch.isChecked());
    }

    @OnClick(R.id.signup_button)
    public void signup() {
        Intent intent = new Intent(this, SignupActivity.class);
        this.startActivity(intent);
    }

    @OnClick(R.id.login_button)
    public void loginOrLogout() {
        showProgressDialog("Please wait…");
        if (!getApp().hasSession()) {
            mGeofancyNetworkingWrapper.doLogin(mAccountUsernameText.getText().toString(), mAccountPasswordText.getText().toString(), mNetworkingCallback);
        } else {
            // TODO: Implement Logout via API (needs to be implemented on Server-Side)
            //mNetworking.doLogout()
            getApp().clearSession();
            adjustUiToLoginState();
            mProgressDialog.dismiss();
        }
    }

    @OnClick(R.id.send_test_button)
    public void sendTestFencelog() {
        Fencelog fencelog = new Fencelog();
        fencelog.locationId = "test";
        fencelog.eventType = EventType.ENTER;
        showProgressDialog("Please wait…");
        String sessionId = getApp().getSessionId();
        if (sessionId != null) {
            mGeofancyNetworkingWrapper.doDispatchFencelog(sessionId, fencelog, mNetworkingCallback);
        }
    }

    @OnClick(R.id.global_http_method_button)
    public void selectMethodForTriggerType() {

        new AlertDialog.Builder(this)
                .setMessage("Choose HTTP Method")
                .setPositiveButton("POST", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mGlobalHttpMethodButton.setText("POST");
                        mHttpMethod = Constants.HttpMethod.POST;
                    }
                })
                .setNeutralButton("GET", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mGlobalHttpMethodButton.setText("GET");
                        mHttpMethod = Constants.HttpMethod.GET;
                    }
                })
                .show();
    }

    private GeofancyApplication getApp() {
        return (GeofancyApplication) getApplication();
    }

    private void showProgressDialog(String message) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.loading);
        mProgressDialog.setMessage(message);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    private SharedPreferences getPrefs() {
        return this.getPreferences(MODE_PRIVATE);
    }

    private void adjustUiToLoginState() {
        int visibility = LinearLayout.VISIBLE;

        if (getApp().hasSession()) {
            // User is logged in
            visibility = LinearLayout.GONE;
        }

        mAccountUsernameText.setVisibility(visibility);
        mAccountPasswordText.setVisibility(visibility);
        mLoginButton.setText((visibility == LinearLayout.VISIBLE) ? "Login" : "Logout");
        mSignupButton.setVisibility(visibility);
        mLostpassButton.setVisibility(visibility);
    }

    private void simpleAlert(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setNeutralButton("OK", null)
                .show();
    }

    private void save(boolean finish) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(HTTP_URL, mUrlText.getText().toString());
        editor.putInt(HTTP_METHOD, mHttpMethod.ordinal());
        editor.putBoolean(HTTP_AUTH, mGlobalHttpAuthSwitch.isChecked());
        editor.putString(HTTP_USERNAME, mGlobalHttpAuthUsername.getText().toString());
        editor.putString(HTTP_PASSWORD, mGlobalHttpAuthPassword.getText().toString());
        editor.putBoolean(NOTIFICATION_SUCCESS, mNotificationSuccessSwitch.isChecked());
        editor.putBoolean(NOTIFICATION_FAIL, mNotificationFailSwitch.isChecked());
        editor.putBoolean(NOTIFICATION_SOUND, mNotificationSoundSwitch.isChecked());

        editor.apply();

        if (finish) {
            finish();
        }
    }
}
