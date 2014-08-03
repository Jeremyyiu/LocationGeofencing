package io.kida.geofancy.app;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

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

public class AddEditGeofenceActivity extends FragmentActivity {

    private MapFragment mMapFragment = null;
    private GoogleMap mMap = null;
    private GeofancyLocationManager mGeofancyLocationManager = null;
    private SeekBar mRadiusSlider = null;

    private MapAreaManager mCircleManager = null;
    private MapAreaWrapper mCircle = null;
    public ProgressDialog mProgressDialog = null;

    private GeocodeHandler mGeocoderHandler = null;
    private boolean mAddressIsDirty = true;
    private boolean mGeocoderIsActive = false;
    private boolean mGeocodeAndSave = false;

    // UI
    private Button mLocationButton = null;
    private EditText mCustomId = null;

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
        setContentView(R.layout.activity_add_edit_geofence);

        mLocationButton = (Button)findViewById(R.id.address_button);
        mCustomId = (EditText)findViewById(R.id.customLocationId);

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

        FragmentManager fm = getFragmentManager();
        mMapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        mMapFragment.onResume();
        mMap = mMapFragment.getMap();

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
//        mMap.getMap().setMapType(0);

        if(mMap.getMyLocation() != null) {
            Location location = mMap.getMyLocation();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
        }

        mGeofancyLocationManager = new GeofancyLocationManager();
        mGeofancyLocationManager.getLocation(this, locationResult);

        setupCircleManager();

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
        ContentResolver resolver = this.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("name", mLocationButton.getText().toString());
        resolver.insert(Uri.parse("content://" + getString(R.string.authority) + "/geofences"), values);

        if (finish) {
            this.finish();
        }
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

            if (mGeocodeAndSave == true) {
                Message.obtain(mGeocoderHandler, GeocodeHandler.SAVE_AND_FINISH, null).sendToTarget();
            }

            return null;
        }
    }
}
