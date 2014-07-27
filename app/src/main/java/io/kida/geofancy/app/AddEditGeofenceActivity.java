package io.kida.geofancy.app;

import android.app.FragmentManager;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class AddEditGeofenceActivity extends FragmentActivity implements LocationListener {

    private MapFragment mMapFragment = null;
    private GoogleMap mMap = null;
    private LocationManager mLocationManager = null;
    private Circle mCircle = null;
    private SeekBar mRadiusSlider = null;

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
        if (mCircle == null) {
            drawMarkerWithCircle(latLng, 100.0);
        } else {
            updateMarkerWithCircle(latLng);
        }
        mLocationManager.removeUpdates(this);
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
                if (mCircle != null) {
                    float radius = progress * 10;
                    if (mMap != null) {
                        Location location = mMap.getMyLocation();
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        updateMarkerWithCircle(latLng, radius);
                    }
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

        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = mLocationManager.getBestProvider(criteria, true);
        Location location = mLocationManager.getLastKnownLocation(provider);
        mLocationManager.requestLocationUpdates(provider, 20000, 0, this);
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
    private void updateMarkerWithCircle(LatLng position) {
        mCircle.setCenter(position);
    }

    private void updateMarkerWithCircle(LatLng position, double radius) {
        mCircle.remove();
        mCircle = mMap.addCircle(getCircleOptions(radius, position));
        mCircle.setFillColor(0xffff0000);
        mCircle.setZIndex(1000);
    }

    private void drawMarkerWithCircle(LatLng position, double radiusInMeters){
        if (mRadiusSlider != null) {
            mRadiusSlider.setProgress((int)radiusInMeters / 10);
        }
        mCircle = mMap.addCircle(getCircleOptions(radiusInMeters, position));
    }

    private CircleOptions getCircleOptions(double radiusInMeters, LatLng position) {
        int strokeColor = 0xffee0000; //red outline
        int shadeColor = 0x44ff0000; //opaque red fill
        return new CircleOptions().center(position).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
    }
}
