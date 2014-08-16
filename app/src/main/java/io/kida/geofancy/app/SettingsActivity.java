package io.kida.geofancy.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import io.kida.geofancy.app.R;

public class SettingsActivity extends Activity {

    private SharedPreferences mPreferences = null;

//    @ViewById(R.id.global_http_url)
    EditText mUrlText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Grab UI
        /*

        <EditText
            android:id="@+id/global_http_url"/>

        <Button
            android:id="@+id/global_http_method_button"/>

        <Switch
            android:id="@+id/global_auth_switch"/>

        <EditText
            android:id="@+id/global_auth_username"/>

        <EditText
            android:id="@+id/global_auth_password"/>

        <Button
            android:id="@+id/send_test_button"/>

        <Switch
            android:id="@+id/notification_success_switch"/>

        <Switch
            android:id="@+id/notification_fail_switch"/>

        <Switch
            android:id="@+id/notification_sound_switch"/>

        <EditText

            android:id="@+id/account_username_text"/>

        <EditText
            android:id="@+id/account_password_text"/>

        <Button
            android:id="@+id/login_button"/>

        <Button
            android:id="@+id/signup_button"/>

        <Button
            android:id="@+id/lostpass_button"/>
         */

        mPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            save();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void save(){

    }
}
