package io.locative.app.view;

import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;
import io.locative.app.GeofancyApplication;
import io.locative.app.R;
import io.locative.app.model.Geofences;
import io.locative.app.network.GeofencingService;
import io.locative.app.utils.Constants;

public class GeofencesActivity extends BaseActivity implements GeofenceFragment.OnFragmentInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor>, NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.drawer)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.nav_view)
    NavigationView mNavigationView;

    @Bind(R.id.container)
    FrameLayout mContentFrame;

    @Bind(R.id.add_geofence)
    FloatingActionButton mFabButton;

    private ActionBarDrawerToggle mDrawerToogle;


    private GeofenceFragment mGeofenceFragment = null;
    private boolean firstResume = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeofancyApplication.inject(this);

        if (savedInstanceState == null) {
            firstResume = true;
        }

        if (mToolbar != null) {
            mToolbar.setNavigationIcon(R.drawable.ic_menu_white_24px);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        // drawer toogle with listner
        mDrawerToogle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                syncState();
                mFabButton.show();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                syncState();
            }

            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                mFabButton.hide();
            }
        };
        mDrawerToogle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToogle);
        mNavigationView.setNavigationItemSelectedListener(this);


        mGeofenceFragment = GeofenceFragment.newInstance("str1", "str2");
        getFragmentManager().beginTransaction().replace(R.id.container, mGeofenceFragment, "").commit();

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
    public void onResume() {
        super.onResume();
        // first start of this activity , open drawer and hide FAB 
        if (firstResume) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
            mDrawerToogle.syncState();
            mFabButton.hide();
        }

        firstResume = false;

    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_geofences;
    }

    @Override
    protected String getToolbarTitle() {
        return null;
    }

    @Override
    protected int getMenuResourceId() {
        return 0;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToogle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        Fragment fragment = null;

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        switch (item.getItemId()) {
            case R.id.geofence:
                if (mGeofenceFragment == null) {
                    mGeofenceFragment = GeofenceFragment.newInstance("str1", "str2");
                }
                fragment = mGeofenceFragment;
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.feedback:
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constants.SUPPORT_MAIL_URI)));
                break;
            case R.id.twitter:
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constants.TWITTER_URI)));
                break;
            case R.id.facebook:
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constants.FACEBOOK_URI)));
                break;
            default:
                break;
        }
        if (fragment != null) {
            transaction.replace(R.id.container, fragment, "").commit();
        }
        setTitle(item.getTitle());
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.add_geofence)
    public void addGeofenceClick() {
        Intent addEditGeofencesIntent = new Intent(this, AddEditGeofenceActivity.class);
        startActivity(addEditGeofencesIntent);
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

        ArrayList<Geofences.Geofence> items = new ArrayList<Geofences.Geofence>();

        while (data.moveToNext()) {
            Geofences.Geofence item = new Geofences.Geofence(
                    data.getString(data.getColumnIndex("_id")),
                    data.getString(data.getColumnIndex("name")),
                    data.getString(data.getColumnIndex("custom_id")),
                    data.getInt(data.getColumnIndex("triggers")),
                    data.getFloat(data.getColumnIndex("latitude")),
                    data.getFloat(data.getColumnIndex("longitude")),
                    data.getInt(data.getColumnIndex("radius")));

            mGeofenceFragment.geofences.addItem(item);
            items.add(item);
        }
        mGeofenceFragment.refresh();

        updateGeofencingService(items);
    }

    private void updateGeofencingService(ArrayList<Geofences.Geofence> items) {
        Intent geofencingService = new Intent(this, GeofencingService.class);
        geofencingService.putExtra(GeofencingService.EXTRA_ACTION, GeofencingService.Action.ADD);
        geofencingService.putExtra(GeofencingService.EXTRA_GEOFENCE, items);
        this.startService(geofencingService);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public SharedPreferences getPrefs() {
        return this.getPreferences(MODE_PRIVATE);
    }

    private GeofancyApplication getApp() {
        return (GeofancyApplication) getApplication();
    }
}
