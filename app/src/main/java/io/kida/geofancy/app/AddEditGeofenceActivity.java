package io.kida.geofancy.app;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.schuetz.mapareas.MapAreaManager;
import com.schuetz.mapareas.MapAreaManager.CircleManagerListener;
import com.schuetz.mapareas.MapAreaMeasure;
import com.schuetz.mapareas.MapAreaWrapper;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class AddEditGeofenceActivity extends ActionBarActivity {

    public int mEditGeofenceId = 0;
    private boolean mIsEditingGeofence = false;

    private GeofancyLocationManager mGeofancyLocationManager = null;
    private MapAreaManager mCircleManager = null;
    private MapAreaWrapper mCircle = null;
    public ProgressDialog mProgressDialog = null;

    private GeocodeHandler mGeocoderHandler = null;
    private boolean mAddressIsDirty = true;
    private boolean mGeocoderIsActive = false;
    private boolean mGeocodeAndSave = false;
    private boolean mSaved = false;
    private Constants.HttpMethod mEnterMethod = Constants.HttpMethod.POST;
    private Constants.HttpMethod mExitMethod = Constants.HttpMethod.POST;

    // UI
    private MapFragment mMapFragment = null;
    private GoogleMap mMap = null;
    private Button mLocationButton = null;
    private EditText mCustomId = null;
    private SeekBar mRadiusSlider = null;
    private Switch mTriggerEnter = null;
    private Switch mTriggerExit = null;
    private Button mEnterMethodButton = null;
    private EditText mEnterUrl = null;
    private Button mExitMethodButton = null;
    private EditText mExitUrl = null;
    private Switch mBasicAuthSwitch = null;
    private EditText mBasicAuthUsername = null;
    private EditText mBasicAuthPassword = null;

    GeofancyLocationManager.LocationResult locationResult = new GeofancyLocationManager.LocationResult(){
        @Override
        public void gotLocation(Location location){
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (mMap != null) {
                zoomToLocation(location);
            }
            setCircleToLocation(location);
            doReverseGeocoding(location);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Already existing (editing) Geofence?
        mEditGeofenceId = getIntent().getIntExtra("geofenceId", 0);
        Log.d(Constants.LOG, "mEditGeofenceId: " + mEditGeofenceId);
        if (mEditGeofenceId > 0) {
            mIsEditingGeofence = true;
        }

        setContentView(R.layout.activity_add_edit_geofence);

        // Get UI
        mLocationButton = (Button)findViewById(R.id.address_button);
        mCustomId = (EditText)findViewById(R.id.customLocationId);
        mTriggerEnter = (Switch)findViewById(R.id.trigger_enter);
        mTriggerExit = (Switch)findViewById(R.id.trigger_exit);
        mEnterMethodButton = (Button)findViewById(R.id.enter_method_button);
        mEnterUrl = (EditText)findViewById(R.id.enter_url_text);
        mEnterMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMethodForTriggerType(Constants.TriggerType.ARRIVAL);
            }
        });
        mExitMethodButton = (Button)findViewById(R.id.exit_method_button);
        mExitUrl = (EditText)findViewById(R.id.exit_url_text);
        mExitMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMethodForTriggerType(Constants.TriggerType.DEPARTURE);
            }
        });
        mRadiusSlider = (SeekBar)findViewById(R.id.radius_slider);
        mRadiusSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mCircle != null) {
                    mCircle.setRadius(progress * 10);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final Context ctx = this;
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            final EditText addressTextField = new EditText(ctx);
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext())
                        .setMessage("Enter Address manually:")
                        .setView(addressTextField)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doGeocodingAndPositionCircle(addressTextField.getText().toString());
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        mBasicAuthSwitch = (Switch)findViewById(R.id.basic_auth_switch);
        mBasicAuthUsername = (EditText)findViewById(R.id.basic_auth_username);
        mBasicAuthPassword = (EditText)findViewById(R.id.basic_auth_password);

        FragmentManager fm = getFragmentManager();
        mMapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        mMapFragment.onResume();
        mMap = mMapFragment.getMap();

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
//        mMap.getMap().setMapType(0);

        Cursor cursor = null;
        if (mIsEditingGeofence) {
            ContentResolver resolver = this.getContentResolver();
            cursor = resolver.query(Uri.parse("content://" + getString(R.string.authority) + "/geofences"), null, "_id = ?", new String[]{ String.valueOf(mEditGeofenceId) }, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                mLocationButton.setText(cursor.getString(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_NAME)));
                mRadiusSlider.setProgress(cursor.getInt(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_RADIUS)));
                mCustomId.setText(cursor.getString(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_CUSTOMID)));

                // Triggers
                int triggers = cursor.getInt(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_TRIGGER));
                mTriggerEnter.setChecked(((triggers & GeofenceProvider.TRIGGER_ON_ENTER) == GeofenceProvider.TRIGGER_ON_ENTER));
                mTriggerExit.setChecked(((triggers & GeofenceProvider.TRIGGER_ON_EXIT) == GeofenceProvider.TRIGGER_ON_EXIT));

                int enterMethod = cursor.getInt(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_ENTER_METHOD));
                mEnterMethodButton.setText(enterMethod == 0 ? "POST" : "GET");
                mEnterMethod = (Constants.HttpMethod.POST.ordinal() == enterMethod) ? Constants.HttpMethod.POST : Constants.HttpMethod.GET;
                mEnterUrl.setText(cursor.getString(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_ENTER_URL)));

                int exitMethod = cursor.getInt(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_EXIT_METHOD));
                mExitMethodButton.setText(exitMethod == 0 ? "POST" : "GET");
                mExitMethod = (Constants.HttpMethod.POST.ordinal() == enterMethod) ? Constants.HttpMethod.POST : Constants.HttpMethod.GET;
                mExitUrl.setText(cursor.getString(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_EXIT_URL)));

                mBasicAuthSwitch.setChecked(cursor.getInt(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_HTTP_AUTH)) != 0);
                mBasicAuthUsername.setText(cursor.getString(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_HTTP_USERNAME)));
                mBasicAuthPassword.setText(cursor.getString(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_HTTP_PASSWORD)));
            }
        }

        Location location = null;
        mGeofancyLocationManager = new GeofancyLocationManager();
        if (!mIsEditingGeofence) {
            mGeofancyLocationManager.getLocation(this, locationResult);
        }

        if(mMap.getMyLocation() != null && !mIsEditingGeofence) {
            location = mMap.getMyLocation();
            mMap.getMyLocation();
        } else if(cursor != null) {
            location = new Location("location");
            location.setLatitude(cursor.getDouble(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_LATITUDE)));
            location.setLongitude(cursor.getDouble(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_LONGITUDE)));
        }

        if (location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
        }

        setupCircleManager();
        if (mIsEditingGeofence) {
            setCircleToLocation(location);
            int radius = cursor.getInt(cursor.getColumnIndex(GeofenceProvider.Geofence.KEY_RADIUS));
            mCircle.setRadius(radius * 10);
            mRadiusSlider.setProgress(radius);
        }

        mGeocoderHandler = new GeocodeHandler(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_edit_geofence, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_save) {
            // Save Geofence / Add new one
            if (mAddressIsDirty == false) {
                this.save(true);
                return true;
            }

            if (mCircle == null) {
                return false;
            }

            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle(R.string.loading);
            mProgressDialog.setMessage(getResources().getString(R.string.geocoding_progress_message));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();

            mGeocodeAndSave = true;
            doReverseGeocodingOfCircleLocation();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void save(boolean finish) {

        Log.i(Constants.LOG, "Saved #1: " + (mSaved ? "true" : "false"));

        if (mSaved == true) {
            return;
        }

        mSaved = true;
        ContentResolver resolver = this.getContentResolver();
        ContentValues values = new ContentValues();

        String custom_id = mCustomId.getText().toString();
        if (mIsEditingGeofence) {
            ContentResolver existingResolver = this.getContentResolver();
            Cursor existingCursor = resolver.query(Uri.parse("content://" + getString(R.string.authority) + "/geofences"), null, "_id = ?", new String[]{ String.valueOf(mEditGeofenceId) }, null);
            if (existingCursor.getCount() > 0) {
                existingCursor.moveToFirst();
                if (custom_id.length() == 0) {
                    custom_id = existingCursor.getString(existingCursor.getColumnIndex(GeofenceProvider.Geofence.KEY_CUSTOMID));
                }
            }
        }

        if (custom_id.length() == 0 && !mIsEditingGeofence) {
            custom_id = new UUID(new Random().nextLong(), new Random().nextLong()).toString();
        }

        int triggers = 0;
        if (mTriggerEnter.isChecked()) {
            triggers |= GeofenceProvider.TRIGGER_ON_ENTER;
        }
        if (mTriggerExit.isChecked()) {
            triggers |= GeofenceProvider.TRIGGER_ON_EXIT;
        }

        Log.d(Constants.LOG, "Triggers: " + triggers);

        values.put(GeofenceProvider.Geofence.KEY_NAME, mLocationButton.getText().toString());
        values.put(GeofenceProvider.Geofence.KEY_RADIUS, mRadiusSlider.getProgress());
        values.put(GeofenceProvider.Geofence.KEY_CUSTOMID, custom_id);
        values.put(GeofenceProvider.Geofence.KEY_ENTER_METHOD, this.methodForTriggerType(Constants.TriggerType.ARRIVAL).ordinal());
        values.put(GeofenceProvider.Geofence.KEY_ENTER_URL, mEnterUrl.getText().toString());
        values.put(GeofenceProvider.Geofence.KEY_TRIGGER, triggers);
        values.put(GeofenceProvider.Geofence.KEY_EXIT_METHOD, this.methodForTriggerType(Constants.TriggerType.DEPARTURE).ordinal());
        values.put(GeofenceProvider.Geofence.KEY_EXIT_URL, mExitUrl.getText().toString());
        values.put(GeofenceProvider.Geofence.KEY_HTTP_AUTH, mBasicAuthSwitch.isChecked() ? 1 : 0);
        values.put(GeofenceProvider.Geofence.KEY_HTTP_USERNAME, mBasicAuthUsername.getText().toString());
        values.put(GeofenceProvider.Geofence.KEY_HTTP_PASSWORD, mBasicAuthPassword.getText().toString());
        values.put(GeofenceProvider.Geofence.KEY_LATITUDE, mCircle.getCenter().latitude);
        values.put(GeofenceProvider.Geofence.KEY_LONGITUDE, mCircle.getCenter().longitude);

        if (mIsEditingGeofence) {
            resolver.update(Uri.parse("content://" + getString(R.string.authority) + "/geofences"), values, "_id = ?", new String[]{String.valueOf(mEditGeofenceId)});
        } else {
            resolver.insert(Uri.parse("content://" + getString(R.string.authority) + "/geofences"), values);
        }

        if (finish) {
            this.finish();
            Log.i(Constants.LOG, "Finished!");
        }

        mSaved = false;
        Log.i(Constants.LOG, "Saved #2: " + (mSaved ? "true" : "false"));

    }

    // Zoom to Location
    private void zoomToLocation(Location location){
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 16));
    }

    // Setup Circle
    private void setCircleToLocation(Location location) {
        // Set Circle
        if (mMap != null && mCircleManager != null && mCircle == null) {
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            mCircle = new MapAreaWrapper(mMap, position, 100, 5.0f, 0xffff0000, 0x33ff0000, 1, 1000);
            mCircleManager.add(mCircle);
            mRadiusSlider.setProgress(10);
        }
    }

    // Radius Circle
    private void setupCircleManager() {
        mCircleManager = new MapAreaManager(mMap,

            4, Color.RED, Color.HSVToColor(70, new float[] {1, 1, 200}), //styling

            -1,//custom drawables for move and resize icons

            0.5f, 0.5f, //sets anchor point of move / resize drawable in the middle

            new MapAreaMeasure(100, MapAreaMeasure.Unit.pixels), //circles will start with 100 pixels (independent of zoom level)

            new CircleManagerListener() { //listener for all circle events

                @Override
                public void onCreateCircle(MapAreaWrapper draggableCircle) {

                }

                @Override
                public void onMoveCircleEnd(MapAreaWrapper draggableCircle) {
                    Log.d(Constants.LOG, "onMoveCircleEnd");
                    doReverseGeocodingOfCircleLocation();
                }

                @Override
                public void onMoveCircleStart(MapAreaWrapper draggableCircle) {
                    mAddressIsDirty = true;
                }

        });
    }

    // Reverse Geocoder
    private void doReverseGeocodingOfCircleLocation() {
        if (mCircle != null) {
            Location location = new Location("Geofence");
            location.setLongitude(mCircle.getCenter().longitude);
            location.setLatitude(mCircle.getCenter().latitude);
            doReverseGeocoding(location);
        }
    }

    private void doGeocodingAndPositionCircle(String addr) {
        if (mCircle != null) {
            Address address = new GeofancyGeocoder().getLatLongFromAddress(addr, this);
            if (address == null) {
                new AlertDialog.Builder(this)
                .setMessage("No location found. Please refine your query.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
                return;
            }

            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mCircle.setCenter(latLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        }
    }

    private void doReverseGeocoding(Location location) {
        // Since the geocoding API is synchronous and may take a while.  You don't want to lock
        // up the UI thread.  Invoking reverse geocoding in an AsyncTask.
        mGeocoderIsActive = true;
        Log.d(Constants.LOG, "doReverseGeocoding for location: " + location);
        (new ReverseGeocodingTask(this)).execute(location);
    }

    private class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
        Context mContext;

        public ReverseGeocodingTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected Void doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

            Location loc = params[0];
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
                // Update address field with the exception.
//                Message.obtain(mGeocoderHandler, UPDATE_ADDRESS, e.toString()).sendToTarget();
                Log.e(Constants.LOG, "Error when Reverse-Geocoding: " + e.toString());
            }
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                // Format the first line of address (if available), city, and country name.
                String addressText = String.format("%s, %s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getLocality(),
                        address.getCountryName());
                // Update address field on UI.
                Message.obtain(mGeocoderHandler, GeocodeHandler.UPDATE_ADDRESS, addressText).sendToTarget();
            }
            mAddressIsDirty = false;
            mGeocoderIsActive = false;

            if (mGeocodeAndSave) {
                mGeocodeAndSave = false;
                Message.obtain(mGeocoderHandler, GeocodeHandler.SAVE_AND_FINISH, null).sendToTarget();
            }

            return null;
        }
    }

    private void selectMethodForTriggerType(final Constants.TriggerType t) {

        new AlertDialog.Builder(this)
            .setMessage("Choose HTTP Method")
            .setPositiveButton("POST", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (t == Constants.TriggerType.ARRIVAL) {
                        mEnterMethodButton.setText("POST");
                        mEnterMethod = Constants.HttpMethod.POST;
                    } else {
                        mExitMethodButton.setText("POST");
                        mExitMethod = Constants.HttpMethod.POST;
                    }
                    dialog.dismiss();
                }
            })
            .setNeutralButton("GET", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (t == Constants.TriggerType.ARRIVAL) {
                        mEnterMethodButton.setText("GET");
                        mEnterMethod = Constants.HttpMethod.GET;
                    } else {
                        mExitMethodButton.setText("GET");
                        mExitMethod = Constants.HttpMethod.GET;
                    }
                    dialog.dismiss();
                }
            })
            .show();
    }

    private Constants.HttpMethod methodForTriggerType(Constants.TriggerType t) {
        if (t == Constants.TriggerType.ARRIVAL) {
            return mEnterMethod;
        }
        return mExitMethod;
    };
}
