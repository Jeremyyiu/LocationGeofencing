package io.locative.app.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.locative.app.LocativeApplication;
import io.locative.app.R;
import io.locative.app.geo.LocativeGeocoder;
import io.locative.app.model.Fencelog;
import io.locative.app.model.Geofences;
import io.locative.app.model.Notification;
import io.locative.app.network.LocativeApiWrapper;
import io.locative.app.network.LocativeConnect;
import io.locative.app.network.LocativeNetworkingCallback;
import io.locative.app.network.LocativeService;
import io.locative.app.network.SessionManager;
import io.locative.app.persistent.GeofenceProvider;
import io.locative.app.persistent.Storage;
import io.locative.app.utils.Constants;
import io.locative.app.utils.Dialog;

public class GeofencesActivity extends BaseActivity implements GeofenceFragment.OnFragmentInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor>, NavigationView.OnNavigationItemSelectedListener, ImportGeofenceFragment.OnGeofenceSelection {
    public static final String NOTIFICATION_CLICK = "notification_click";
    private final LocativeConnect connect = new LocativeConnect();

    @BindView(R.id.drawer)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    @BindView(R.id.container)
    FrameLayout mContentFrame;

    @BindView(R.id.add_geofence)
    FloatingActionButton mFabButton;

    @Inject
    LocativeApiWrapper mLocativeNetworkingWrapper;

    @Inject
    SessionManager mSessionManager;

    @Inject
    Storage mStorage;

    private ActionBarDrawerToggle mDrawerToogle;

    private GeofenceFragment mGeofenceFragment = null;
    private FencelogsFragment mFenceLogsFragment = null;
    private NotificationsFragment mNotificationsFragment = null;

    private boolean firstResume = false; // never open drawer initially

    private String fragmentTag = GeofenceFragment.TAG;
    private static final String FRAGMENTTAG = "current.fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(FRAGMENTTAG))
                fragmentTag = savedInstanceState.getString(FRAGMENTTAG);
        }
        super.onCreate(savedInstanceState);
        ((LocativeApplication) getApplication()).inject(this);

        /* never open drawer initially
        if (savedInstanceState == null) {
            firstResume = true;
        }*/

        if (getIntent() != null) {
            Intent intent = getIntent();
            if (intent.getBooleanExtra(NOTIFICATION_CLICK, false)) {
                if (mFenceLogsFragment == null)
                    mFenceLogsFragment = new FencelogsFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.container, mFenceLogsFragment, "").commit();
                mFabButton.hide();
            }
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
                if (isFragmentVisible(GeofenceFragment.TAG)) {
                    mFabButton.show();
                }
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

        /*ContentResolver resolver = this.getContentResolver();

        ContentValues values = new ContentValues();
        values.put("custom_id", "123");
        resolver.insert(Uri.parse("content://" + getString(R.string.authority) + "/geofences"), values);*/
    }

    private boolean isFragmentVisible(String tag) {
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);
        return (fragment != null && fragment.isVisible());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(FRAGMENTTAG, fragmentTag);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // FCM token
        updateFcmToken();

        FragmentManager fragman = getFragmentManager();
        switch (fragmentTag) {
            case GeofenceFragment.TAG: {
                Fragment f = fragman.getFragment(new Bundle(), GeofenceFragment.TAG);
                if (mGeofenceFragment == null)
                    mGeofenceFragment = f != null ? (GeofenceFragment) f : GeofenceFragment.newInstance("str1", "str2");
                fragman.beginTransaction().replace(R.id.container, mGeofenceFragment, GeofenceFragment.TAG).commit();
                if (Geofences.ITEMS.size() == 0) {
                    load();
                }
                mGeofenceFragment.setLoading(false);
                break;
            }
            case FencelogsFragment.TAG: {
                Fragment f = fragman.getFragment(new Bundle(), FencelogsFragment.TAG);
                if (mFenceLogsFragment == null)
                    mFenceLogsFragment = f != null ? (FencelogsFragment) f : new FencelogsFragment();
                fragman.beginTransaction().replace(R.id.container, mFenceLogsFragment, FencelogsFragment.TAG).commit();
                break;
            }
            case NotificationsFragment.TAG: {
                Fragment f = fragman.getFragment(new Bundle(), NotificationsFragment.TAG);
                if (mNotificationsFragment == null)
                    mNotificationsFragment = f != null ? (NotificationsFragment) f : new NotificationsFragment();
                fragman.beginTransaction().replace(R.id.container, mNotificationsFragment, NotificationsFragment.TAG).commit();
                break;
            }
        }
    }

    private void updateFcmToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(Constants.LOG, "FCM Token: " + token);
        if (token == null) {
            return;
        }
        connect.updateSession(mSessionManager.getSessionId(), token, false);
    }

    public void load() {
        if (getLoaderManager().getLoader(0) == null)
            getLoaderManager().initLoader(0, null, this);
        getLoaderManager().getLoader(0).forceLoad();
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
        boolean cancelled = false;

        switch (item.getItemId()) {
            case R.id.geofence:
                if (mGeofenceFragment == null) {
                    mGeofenceFragment = GeofenceFragment.newInstance("str1", "str2");
                }
                fragment = mGeofenceFragment;
                fragmentTag = GeofenceFragment.TAG;
                mFabButton.show();
                break;
            case R.id.fencelogs:
                if (!mSessionManager.hasSession()) {
                    // don't try to show FencelogsFragment if user is not logged in
                    // instead show AlertDialog and offer chance to log in / create account
                    final GeofencesActivity self = this;
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.not_logged_in)
                            .setMessage(R.string.need_login)
                            .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(self, SettingsActivity.class));
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .setCancelable(true)
                            .create().show();
                    cancelled = true;
                    break;
                }
                // in case the user is logged in, just continue as usual
                if (mFenceLogsFragment == null) {
                    mFenceLogsFragment = new FencelogsFragment();
                }
                fragment = mFenceLogsFragment;
                fragmentTag = FencelogsFragment.TAG;
                mFabButton.hide();
                break;
            case R.id.notifications:
                if (!mSessionManager.hasSession()) {
                    // don't try to show FencelogsFragment if user is not logged in
                    // instead show AlertDialog and offer chance to log in / create account
                    final GeofencesActivity self = this;
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.not_logged_in)
                            .setMessage(R.string.need_login)
                            .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(self, SettingsActivity.class));
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .setCancelable(true)
                            .create().show();
                    cancelled = true;
                    break;
                }
                // in case the user is logged in, just continue as usual
                if (mNotificationsFragment == null) {
                    mNotificationsFragment = new NotificationsFragment();
                }
                fragment = mNotificationsFragment;
                fragmentTag = NotificationsFragment.TAG;
                mFabButton.hide();
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.feedback:
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(Constants.SUPPORT_URI)));
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
            transaction.replace(R.id.container, fragment, fragmentTag).commit();
        }

        if (cancelled) {
            // operation has been cancelled because prerequisites have failed
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return false;
        }

        setTitle(item.getTitle());
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.add_geofence)
    public void addGeofenceClick() {

        if (!mSessionManager.hasSession()) {
            // We're not logged in, automatically go to create new Geofence and omit import
            createGeofence();
            return;
        }

        // use is logged in, let him chose between creating or importing Geofences
        AddGeofenceDialog geofenceDialog = AddGeofenceDialogFragment.createInstance();
        geofenceDialog.setLocallyListener(new AddGeofenceDialogFragment.AddGeofenceResultListener() {
            @Override
            public void onResult() {
                createGeofence();
            }
        });
        geofenceDialog.setImportListener(new AddGeofenceDialogFragment.AddGeofenceResultListener() {
            @Override
            public void onResult() {
                // TODO we better create an extra activity because currently when the user clicks back he leaves the app
                Fragment fragment = new ImportGeofenceFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.container, fragment, "").commit();
                setTitle(R.string.geofence_import);
                mFabButton.hide();
            }
        });
        geofenceDialog.show(getFragmentManager());
    }

    private void createGeofence() {
        Intent addEditGeofencesIntent = new Intent(GeofencesActivity.this, AddEditGeofenceActivity.class);
        GeofencesActivity.this.startActivity(addEditGeofencesIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse("content://" + getString(R.string.authority) + "/geofences");
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mGeofenceFragment.geofences.clear();
        ArrayList<Geofences.Geofence> items = new ArrayList<>();
        while (data.moveToNext()) {
            Geofences.Geofence item = GeofenceProvider.fromCursor(data);
            mGeofenceFragment.geofences.addItem(item);
            items.add(item);
        }
        mGeofenceFragment.refresh();

        updateGeofencingService(items);
    }

    private void updateGeofencingService(ArrayList<Geofences.Geofence> items) {
        Intent geofencingService = new Intent(this, LocativeService.class);
        geofencingService.putExtra(LocativeService.EXTRA_ACTION, LocativeService.Action.ADD);
        geofencingService.putExtra(LocativeService.EXTRA_GEOFENCE, items);
        this.startService(geofencingService);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public SharedPreferences getPrefs() {
        return this.getPreferences(MODE_PRIVATE);
    }

    private LocativeApplication getApp() {
        return (LocativeApplication) getApplication();
    }

    public void onGeofenceImportSelection(final Geofences.Geofence fence) {
        if (!mStorage.fenceExistsWithCustomId(fence)) {
            insertGeofence(fence);
            return;
        }

        new AlertDialog.Builder(this)
                .setMessage("A Geofence with the same custom ID already exists. Would you like to overwrite the existing Geofence?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        insertGeofence(fence);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private void insertGeofence(final Geofences.Geofence fence) {
        final Activity self = this;
        final ProgressDialog dialog = Dialog.getIndeterminateProgressDialog(this, "Importing Geofence…");
        dialog.show();

        new Thread(new Runnable() {
           @Override
           public void run() {
               Address address = new LocativeGeocoder().getFromLatLong(fence.latitude, fence.longitude, self);
               if (address != null) {
                   fence.name = address.getAddressLine(0);
               }
               mStorage.insertOrUpdateFence(fence);
               final FragmentManager fragmentManager = getFragmentManager();
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       dialog.dismiss();
                       FragmentTransaction transaction = fragmentManager.beginTransaction();
                       Geofences.ITEMS.add(fence);
                       mGeofenceFragment.setLoading(false);
                       transaction.replace(R.id.container, mGeofenceFragment, GeofenceFragment.TAG).commit();
                       setTitle(R.string.title_geofences);
                       mFabButton.show();
                   }
               });
           }
       }).run();

    }

}
