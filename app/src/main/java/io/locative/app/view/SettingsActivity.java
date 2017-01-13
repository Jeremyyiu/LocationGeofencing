package io.locative.app.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.google.firebase.iid.FirebaseInstanceId;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.locative.app.LocativeApplication;
import io.locative.app.R;
import io.locative.app.model.EventType;
import io.locative.app.model.Fencelog;
import io.locative.app.network.LocativeApiWrapper;
import io.locative.app.network.LocativeNetworkingAdapter;
import io.locative.app.network.LocativeNetworkingCallback;
import io.locative.app.utils.Constants;
import io.locative.app.utils.Preferences;

public class SettingsActivity extends BaseActivity {

    private Constants.HttpMethod mHttpMethod = Constants.HttpMethod.POST;

    private ProgressDialog mProgressDialog = null;
    private LocativeNetworkingCallback mNetworkingCallback;

    @BindView(R.id.global_http_url)
    EditText mUrlText;

    @BindView(R.id.global_http_method_button)
    Button mGlobalHttpMethodButton;

    @BindView(R.id.global_auth_switch)
    Switch mGlobalHttpAuthSwitch;

    @BindView(R.id.global_auth_username)
    EditText mGlobalHttpAuthUsername;

    @BindView(R.id.global_auth_password)
    EditText mGlobalHttpAuthPassword;

    @BindView(R.id.send_test_button)
    Button mSendTestButton;

    @BindView(R.id.notification_success_switch)
    Switch mNotificationSuccessSwitch;

    @BindView(R.id.notification_fail_switch)
    Switch mNotificationFailSwitch;

    @BindView(R.id.notification_only_latest_switch)
    Switch mNotificationOnlyLatestSwitch;

    @BindView(R.id.notification_sound_switch)
    Switch mNotificationSoundSwitch;

    @BindView(R.id.login_button)
    Button mLoginButton;

    @BindView(R.id.signup_button)
    Button mSignupButton;

    @BindView(R.id.lostpass_button)
    Button mLostpassButton;

    @Inject
    LocativeApiWrapper mLocativeNetworkingWrapper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((LocativeApplication) getApplication()).getComponent().inject(this);

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
        mNotificationOnlyLatestSwitch.setChecked(mPrefs.getBoolean(Preferences.NOTIFICATION_SHOW_ONLY_LATEST, false));
        mNotificationSoundSwitch.setChecked(mPrefs.getBoolean(Preferences.NOTIFICATION_SOUND, false));
        mHttpMethod = (Constants.HttpMethod.POST.ordinal() == mPrefs.getInt(Preferences.HTTP_METHOD, 0)) ? Constants.HttpMethod.POST : Constants.HttpMethod.GET;

        mNetworkingCallback = new LocativeNetworkingAdapter() {
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
    public void onResume() {
        super.onResume();

        if (getIntent() != null && getIntent().getData() != null && getIntent().getData().getHost().equals("login")) {
            // Login intent via redirect
            String sessionId = getIntent().getData().getQueryParameter("token");
            if (sessionId != null && sessionId.length() > 0) {
                // Let's login using our token then
                Log.d(Constants.LOG, "Login success with SessionID: " + sessionId);
                mSessionManager.setSessionId(sessionId);
                adjustUiToLoginState();
            }
        }

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
        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://my.locative.io/signup")
        );
        startActivity(browserIntent);
        finish();
    }

    @OnClick(R.id.login_button)
    public void loginOrLogout() {
        if (!mSessionManager.hasSession()) {
            Intent browserIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://my.locative.io/mobile-login?origin=" + Constants.API_ORIGIN + "&sandbox=false&fcm=" + FirebaseInstanceId.getInstance().getToken())
            );
            startActivity(browserIntent);
            finish();
        } else {
            // TODO: Implement Logout via API (needs to be implemented on Server-Side)
            mSessionManager.clearSession();
            adjustUiToLoginState();
        }
    }

    @OnClick(R.id.lostpass_button)
    public void lostPassword() {
        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://my.locative.io/lostpassword")
        );
        startActivity(browserIntent);
        finish();
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
        editor.putBoolean(Preferences.NOTIFICATION_SHOW_ONLY_LATEST, mNotificationOnlyLatestSwitch.isChecked());
        editor.putBoolean(Preferences.NOTIFICATION_SOUND, mNotificationSoundSwitch.isChecked());

        editor.apply();

        if (finish) {
            finish();
        }
    }
}
