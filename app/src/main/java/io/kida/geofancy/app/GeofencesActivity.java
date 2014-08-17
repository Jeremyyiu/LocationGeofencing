package io.kida.geofancy.app;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.TextView;
import android.net.Uri;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import de.triplet.simpleprovider.*;

import android.util.Log;

public class GeofencesActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GeofenceFragment.OnFragmentInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private GeofenceFragment mGeofenceFragment = null;
    private GeofancyNetworking mNetworking = null;
    private  GeofancyNetworkingCallback mNetworkingCallback = null;

    private enum DrawerItem {
        GEOFENCES,
        SETTINGS,
        FEEDBACK,
        TWITTER,
        FACEBOOK
    }

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofences);

        mNetworking = new GeofancyNetworking();
        mNetworkingCallback = new GeofancyNetworkingCallback() {
            @Override
            public void onLoginFinished(boolean success, String sessionId) {

            }

            @Override
            public void onSignupFinished(boolean success, boolean userAlreadyExisting) {

            }

            @Override
            public void onCheckSessionFinished(boolean sessionValid) {
                if (!sessionValid) {
                    getPrefs().edit().putString(Constants.SESSION_ID, null).commit();
                }
            }
        };

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        /*ContentResolver resolver = this.getContentResolver();

        ContentValues values = new ContentValues();
        values.put("custom_id", "123");
        resolver.insert(Uri.parse("content://" + getString(R.string.authority) + "/geofences"), values);*/

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    @Override
    public void onResume(){
        super.onResume();


    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;

        DrawerItem item = DrawerItem.values()[position];
        switch (item) {
            case GEOFENCES: {
                if (mGeofenceFragment == null) {
                    mGeofenceFragment = new GeofenceFragment().newInstance("str1", "str2");
                }
                fragment = mGeofenceFragment;
                break;
            }
            case SETTINGS: {
                Intent settingsActivityIntent = new Intent(this, SettingsActivity_.class);
                this.startActivity(settingsActivityIntent);
                break;
            }
            case FEEDBACK: {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constants.SUPPORT_MAIL_URI)));
                break;
            }
            case TWITTER: {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constants.TWITTER_URI));
                startActivity(intent);
                break;
            }
            case FACEBOOK: {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constants.FACEBOOK_URI));
                startActivity(intent);
                break;
            }
        }


        if (fragment != null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_geofences);
                break;
            case 2:
                mTitle = getString(R.string.title_settings);
                break;
            case 3:
                mTitle = getString(R.string.title_facebook);
                break;
            case 4:
                mTitle = getString(R.string.title_support);
                break;
            case 5:
                mTitle = getString(R.string.title_twitter);
                break;
            case 6:
                mTitle = getString(R.string.title_facebook);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.geofences, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_geofence) {
            Intent addEditGeofencesIntent = new Intent(this, AddEditGeofenceActivity.class);
            this.startActivity(addEditGeofencesIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse("content://" + getString(R.string.authority) + "/geofences");
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //DatabaseUtils.dumpCursor(data);

        mGeofenceFragment.geofences.clear();

        while(data.moveToNext()) {
            Geofences.Geofence item = new Geofences.Geofence(
                    data.getString(data.getColumnIndex("_id")),
                    data.getString(data.getColumnIndex("name")),
                    data.getString(data.getColumnIndex("custom_id")));
            mGeofenceFragment.geofences.addItem(item);
        }
        mGeofenceFragment.refresh();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public SharedPreferences getPrefs(){
        return this.getPreferences(MODE_PRIVATE);
    }
}
