package io.locative.app;

/**
 * Created by mkida on 3/08/2014.
 */

import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

public class GeofancyLocationManager {

    public static final int MY_PERMISSIONS_REQUEST = 1;

    private static final String TAG = "location";

    Timer timer1;
    LocationManager lm;
    LocationResult locationResult;
    boolean gps_enabled = false;
    boolean network_enabled = false;

    public boolean getLocation(Activity context, LocationResult result) {
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        locationResult = result;
        if (lm == null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        //don't start listeners if no provider is enabled
        if (!gps_enabled && !network_enabled)
            return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                context.requestPermissions(permissions, MY_PERMISSIONS_REQUEST);

                // TODO: Consider calling
                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                // TODO return a future or rxjava observable with the result so we can
                return false;
            }else{
                requestUpdates();
                return true;
            }
        }else{
            requestUpdates();
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestUpdates();
                } else {
                    Log.w(TAG, "Location permissions denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
        }
    }

    private void requestUpdates() {
        try{
            Log.i(TAG, "Requesting location updates every 20 sec");
            if (gps_enabled)
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
            if(network_enabled)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
            timer1 = new Timer();
            timer1.schedule(new GetLastLocation(), 20000);
        }catch(SecurityException e){
            Log.e(TAG, e.getMessage(), e);
        }
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.i(TAG, "Canceling location udpater");
            timer1.cancel();
            locationResult.gotLocation(location);
            try {
                lm.removeUpdates(this);
                lm.removeUpdates(locationListenerNetwork);
            }catch(SecurityException e){
                Log.e("location",e.getMessage(), e);
            }
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.i(TAG, "Canceling location updater");
            timer1.cancel();
            locationResult.gotLocation(location);
            try {
                lm.removeUpdates(this);
                lm.removeUpdates(locationListenerGps);
            }catch(SecurityException e){
                Log.e("location",e.getMessage(), e);
            }
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    private class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            try{
                lm.removeUpdates(locationListenerGps);
                lm.removeUpdates(locationListenerNetwork);

                Location net_loc=null, gps_loc=null;
                if(gps_enabled)
                    gps_loc=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(network_enabled)
                    net_loc=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                //if there are both values use the latest one
                if(gps_loc!=null && net_loc!=null){
                    if(gps_loc.getTime()>net_loc.getTime())
                        locationResult.gotLocation(gps_loc);
                    else
                        locationResult.gotLocation(net_loc);
                    return;
                }

                if(gps_loc!=null){
                    locationResult.gotLocation(gps_loc);
                    return;
                }
                if(net_loc!=null){
                    locationResult.gotLocation(net_loc);
                    return;
                }
                locationResult.gotLocation(null);
            }catch(SecurityException e){
                Log.e("location",e.getMessage(), e);
            }
        }
    }

    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }
}
