package io.locative.app.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.locative.app.LocativeApplication;
import io.locative.app.R;
import io.locative.app.geo.LocativeGeocoder;
import io.locative.app.model.Account;
import io.locative.app.model.Geofences;
import io.locative.app.persistent.GeofenceProvider;
import io.locative.app.persistent.Storage;
import io.locative.app.service.LocativeService;
import io.locative.app.utils.Constants;
import io.locative.app.utils.Dialog;
import io.locative.app.utils.Preferences;

public class GeofencesActivity extends BaseActivity implements GeofenceFragment.OnFragmentInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String NOTIFICATION_CLICK = "notification_click";

    private AccountHeader mHeader;

    private Drawer mDrawer;

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    @BindView(R.id.container)
    FrameLayout mContentFrame;

    @BindView(R.id.add_geofence)
    FloatingActionButton mFabButton;

    @BindView(R.id.toolbar_actionbar)
    Toolbar mToolbar;

    @Inject
    Storage mStorage;

    private GeofenceFragment mGeofenceFragment = null;

    private boolean firstResume = false; // never open drawer initially

    private String fragmentTag = GeofenceFragment.TAG;
    private static final String FRAGMENTTAG = "current.fragment";
    private static final String BUNDLE_DRAWER_STATE = "bundleDrawerState";

    private ProfileDrawerItem getEmptyProfileDrawerItem() {
        return new ProfileDrawerItem()
                .withIdentifier(0)
                .withName(getString(R.string.drawer_header_username_placeholder))
                .withIcon(ContextCompat.getDrawable(this, R.drawable.logo_round_512px));
    }

    private void updateHeaderWithAccount(@Nullable Account account) {
        if (account == null) {
            mHeader.updateProfile(
                    getEmptyProfileDrawerItem()
            );
            return;
        }
        mHeader.updateProfile(
                new ProfileDrawerItem()
                        .withIdentifier(0)
                        .withName(account.getUsername())
                        .withEmail(account.getEmail())
                        .withIcon(account.getAvatarUrl())
        );
    }

    private void updateDrawerHeader() {
        if (mPrefs.getString(Preferences.ACCOUNT, null) != null) {
            mPrefs.edit().remove(Preferences.ACCOUNT).apply();
        }
        // update header in drawer
        updateHeaderWithAccount(null);
    }

    private void setupDrawer() {
        final GeofencesActivity self = this;

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(getApplicationContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
                }

                //we use the default one for
                //DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()

                return super.placeholder(ctx, tag);
            }
        });

        mHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(new ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(
                        getEmptyProfileDrawerItem()
                )
                .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                    @Override
                    public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                        startActivity(new Intent(self, SettingsActivity.class));
                        mDrawer.closeDrawer();
                        return true;
                    }

                    @Override
                    public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .withOnAccountHeaderSelectionViewClickListener(new AccountHeader.OnAccountHeaderSelectionViewClickListener() {
                    @Override
                    public boolean onClick(View view, IProfile profile) {
                        startActivity(new Intent(self, SettingsActivity.class));
                        mDrawer.closeDrawer();
                        return true;
                    }
                })
                .build();

        updateDrawerHeader();

        class DRAWER_ITEMS {
            final static int GEOFENCES = 1;
            final static int SETTINGS = 2;
        }

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(mHeader)
                .withToolbar(mToolbar)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(getResources().getString(R.string.title_geofences)),
                        new PrimaryDrawerItem().withName(getResources().getString(R.string.title_settings)).withSelectable(false)
                        )
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        updateDrawerHeader();
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                .withFooter(R.layout.drawer_footer)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (!(drawerItem instanceof PrimaryDrawerItem)) {
                            return false;
                        }

                        Fragment fragment = null;

                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        boolean cancelled = false;

                        switch (position) {
                            case DRAWER_ITEMS.GEOFENCES:
                                if (mGeofenceFragment == null) {
                                    mGeofenceFragment = GeofenceFragment.newInstance("str1", "str2");
                                }
                                fragment = mGeofenceFragment;
                                fragmentTag = GeofenceFragment.TAG;
                                mFabButton.show();
                                setTitle(getResources().getString(R.string.app_name));
                                break;
                            case DRAWER_ITEMS.SETTINGS:
                                startActivity(new Intent(self, SettingsActivity.class));
                                break;
                            default:
                                break;
                        }
                        if (fragment != null) {
                            transaction.replace(R.id.container, fragment, fragmentTag).commit();
                        }

                        if (cancelled) {
                            // operation has been cancelled because prerequisites have failed
                            mDrawer.closeDrawer();
                            return false;
                        }

                        mDrawer.closeDrawer();
                        return true;
                    }
                })
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(FRAGMENTTAG)) {
                fragmentTag = savedInstanceState.getString(FRAGMENTTAG);
            }
            if (savedInstanceState.containsKey(BUNDLE_DRAWER_STATE)) {
               if (mDrawer != null) {
                   mDrawer.setSelectionAtPosition(savedInstanceState.getInt(BUNDLE_DRAWER_STATE));
               }
            }
        }
        super.onCreate(savedInstanceState);
        ((LocativeApplication) getApplication()).getComponent().inject(this);
        setupDrawer();

        /* never open drawer initially
        if (savedInstanceState == null) {
            firstResume = true;
        }*/

        if (mToolbar != null) {
            mToolbar.setNavigationIcon(R.drawable.ic_menu_white_24px);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawer.openDrawer();
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(FRAGMENTTAG, fragmentTag);
        outState.putInt(BUNDLE_DRAWER_STATE, mDrawer.getCurrentSelectedPosition());
    }

    @Override
    protected void onStart() {
        super.onStart();

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
                break;
            }
        }
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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(BUNDLE_DRAWER_STATE)) {
            if (mDrawer != null) {
                mDrawer.setSelectionAtPosition(savedInstanceState.getInt(BUNDLE_DRAWER_STATE));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // first start of this activity , open drawer and hide FAB
        if (firstResume) {
            mDrawer.openDrawer();
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
//        mDrawerToogle.syncState();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.add_geofence)
    public void addGeofenceClick() {
        createGeofence();
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
                       transaction.replace(R.id.container, mGeofenceFragment, GeofenceFragment.TAG).commit();
                       setTitle(R.string.title_geofences);
                       mFabButton.show();
                   }
               });
           }
       }).run();

    }

}
