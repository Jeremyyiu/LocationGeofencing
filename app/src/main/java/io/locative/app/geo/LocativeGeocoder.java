package io.locative.app.geo;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocativeGeocoder {

    @Nullable
    public Address getLatLongFromAddress(String addr, Context ctx) {
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

    @Nullable
    public Address getFromLatLong(double latitude, double longitude, Context ctx) {
        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        try {
            List<Address> list = geocoder.getFromLocation(latitude, longitude, 1);
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
