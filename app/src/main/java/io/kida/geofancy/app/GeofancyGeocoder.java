package io.kida.geofancy.app;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by mkida on 16/08/2014.
 */
public class GeofancyGeocoder {
    public Address getLatLongFromAddress(String addr, Context ctx)
    {
        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        try {
            List<Address> list = geocoder.getFromLocationName(addr, 1);
            if (list.size() == 0) {
                return null;
            }
            return list.get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
