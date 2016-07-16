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
import io.locative.app.LocativeApplication;
import io.locative.app.R;
import io.locative.app.model.EventType;
import io.locative.app.model.Fencelog;
import io.locative.app.network.LocativeNetworkingAdapter;
import io.locative.app.network.LocativeNetworkingCallback;
import io.locative.app.network.LocativeApiWrapper;
import io.locative.app.utils.Constants;
import io.locative.app.utils.Preferences;

public class SettingsActivity extends BaseActivity {

    private Constants.HttpMethod mHttpMethod = Constants.HttpMethod.POST;

    private ProgressDialog mProgressDialog = null;
    private LocativeNetworkingCallback mNetworkingCallback;

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
    LocativeApiWrapper mLocativeNetworkingWrapper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((LocativeApplication) getApplication()).inject(this);

        adjustUiToLoginState();

        mUrlText.setText(mPrefs.getString(Preferences.HTTP_URL, null));
        mGlobalHttpMethodButton.setText(
                mPrefs.getInt(Preferences.HTTP_METHOD, 0) == 0 ? "POST" : "GET"
        );
        mGlobalHttpAuthSwitch.setChecked(
                mPrefs.getBoolean(Preferences.HTTP_AUTH, false)
        );
        switchHttpAuth();

        mGlobalHttpAuthUsername.setText(mPrefs.getString(Preferences.HTTP_USERNAME, null));
        mGlobalHttpAuthPassword.setText(mPrefs.getString(Preferences.HTTP_PASSWORD, null));
        mNotificationSuccessSwitch.setChecked(mPrefs.getBoolean(Preferences.NOTIFICATION_SUCCESS, false));
        mNotificationFailSwitch.setChecked(mPrefs.getBoolean(Preferences.NOTIFICATION_FAIL, false));
        mNotificationSoundSwitch.setChecked(mPrefs.getBoolean(Preferences.NOTIFICATION_SOUND, false));
        mHttpMethod = (Constants.HttpMethod.POST.ordinal() == mPrefs.getInt(Preferences.HTTP_METHOD, 0)) ? Constants.HttpMethod.POST : Constants.HttpMethod.GET;

        mNetworkingCallback = new LocativeNetworkingAdapter() {
            @Override
            public void onLoginFinished(boolean success, String sessionId) {
                mProgressDialog.dismiss();
                if (!success) {
                    simpleAlert("Username or Password incorrect. Please try again.");
                    return;
                }
                simpleAlert("Login successful! Your Fencelogs will now be visible when you log in at https://my.locative.io");
                Log.d(Constants.LOG, "Login success with SessionID: " + sessionId);
                mSessionManager.setSessionId(sessionId);
                adjustUiToLoginState();
            }

            @Override
            public void onCheckSessionFinished(boolean sessionValid) {
                if (!sessionValid) {
                    mSessionManager.clearSession();
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
        mLocativeNetworkingWrapper.doCheckSession(mSessionManager.getSessionId(), mNetworkingCallback);
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
        if (!mSessionManager.hasSession()) {
            mLocativeNetworkingWrapper.doLogin(mAccountUsernameText.getText().toString(), mAccountPasswordText.getText().toString(), mNetworkingCallback);
        } else {
            // TODO: Implement Logout via API (needs to be implemented on Server-Side)
            //mNetworking.doLogout()
            mSessionManager.clearSession();
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
        String sessionId = mSessionManager.getSessionId();
        if (sessionId != null) {
            mLocativeNetworkingWrapper.doDispatchFencelog(sessionId, fencelog, mNetworkingCallback);
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


    private void showProgressDialog(String message) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.loading);
        mProgressDialog.setMessage(message);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
    }

    private void adjustUiToLoginState() {
        int visibility = LinearLayout.VISIBLE;

        if (mSessionManager.hasSession()) {
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
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(Preferences.HTTP_URL, mUrlText.getText().toString());
        editor.putInt(Preferences.HTTP_METHOD, mHttpMethod.ordinal());
        editor.putBoolean(Preferences.HTTP_AUTH, mGlobalHttpAuthSwitch.isChecked());
        editor.putString(Preferences.HTTP_USERNAME, mGlobalHttpAuthUsername.getText().toString());
        editor.putString(Preferences.HTTP_PASSWORD, mGlobalHttpAuthPassword.getText().toString());
        editor.putBoolean(Preferences.NOTIFICATION_SUCCESS, mNotificationSuccessSwitch.isChecked());
        editor.putBoolean(Preferences.NOTIFICATION_FAIL, mNotificationFailSwitch.isChecked());
        editor.putBoolean(Preferences.NOTIFICATION_SOUND, mNotificationSoundSwitch.isChecked());

        editor.apply();

        if (finish) {
            finish();
        }
    }
}
