package io.kida.geofancy.app;

import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.schuetz.mapareas.MapAreaManager;
import com.schuetz.mapareas.MapAreaManager.CircleManagerListener;
import com.schuetz.mapareas.MapAreaMeasure;
import com.schuetz.mapareas.MapAreaWrapper;

public class AddEditGeofenceActivity extends FragmentActivity implements LocationListener {

    private MapFragment mMapFragment = null;
    private GoogleMap mMap = null;
    private LocationManager mLocationManager = null;
    private SeekBar mRadiusSlider = null;

    private MapAreaManager mCircleManager = null;
    private MapAreaWrapper mCircle = null;

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (mMap != null) {
            zoomToLocation(location);
        }
        mLocationManager.removeUpdates(this);

        // Set Circle
        if (mMap != null && mCircleManager != null && mCircle == null) {
            LatLng position = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
            mCircle = new MapAreaWrapper(mMap, position, 100, 5.0f, 0xffff0000, 0x33ff0000, 1, 1000);
            mCircleManager.add(mCircle);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (status == LocationProvider.AVAILABLE) {
            mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(new Criteria(), true), 5000, 0, this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_geofence);

        mRadiusSlider = (SeekBar)findViewById(R.id.radius_slider);
        mRadiusSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCircle.setRadius(progress * 10);
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

        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = mLocationManager.getBestProvider(criteria, true);
        Location location = mLocationManager.getLastKnownLocation(provider);
        mLocationManager.requestLocationUpdates(provider, 20000, 0, this);

        setupCircleManager();
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Zoom to Location
    private void zoomToLocation(Location location){
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 16));
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
                public void onResizeCircleEnd(MapAreaWrapper draggableCircle) {

                }

                @Override
                public void onCreateCircle(MapAreaWrapper draggableCircle) {

                }

                @Override
                public void onMoveCircleEnd(MapAreaWrapper draggableCircle) {

                }

                @Override
                public void onMoveCircleStart(MapAreaWrapper draggableCircle) {

                }

                @Override
                public void onResizeCircleStart(MapAreaWrapper draggableCircle) {

                }

                @Override
                public void onMinRadius(MapAreaWrapper draggableCircle) {

                }

                @Override
                public void onMaxRadius(MapAreaWrapper draggableCircle) {

            }
        });
    }
}
