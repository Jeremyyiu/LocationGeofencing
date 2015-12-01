package io.locative.app;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mkida on 3/08/2014.
 */
public class Geofences {

    /**
     * An array of sample (dummy) items.
     */
    public static List<Geofence> ITEMS = new ArrayList<Geofence>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, Geofence> ITEM_MAP = new HashMap<String, Geofence>();

    public void clear() {
        ITEMS.clear();
    }

    public void addItem(Geofence item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class Geofence implements Serializable {
        public final String id;
        public final String title;
        public final String subtitle;
        public final int triggers;
        public final float latitude;
        public final float longitude;
        public final int radius;


        public Geofence(String id, String title, String subtitle, int triggers, float latitude, float longitude, int radius) {
            this.id = id;
            this.title = title;
            this.subtitle = subtitle;
            this.triggers = triggers;
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;

        }

        @Override
        public String toString() {
            return title;
        }

        public com.google.android.gms.location.Geofence toGeofence() {
            // Build a new Geofence object
            int transition = 0;
            boolean both = (
                    ((triggers & GeofenceProvider.TRIGGER_ON_ENTER) == GeofenceProvider.TRIGGER_ON_ENTER) &&
                    ((triggers & GeofenceProvider.TRIGGER_ON_EXIT) == GeofenceProvider.TRIGGER_ON_EXIT)
            );
            if(both) {
                Log.d(Constants.LOG, "ID: " + this.id + " trigger on BOTH");
                transition |= com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER;
                transition |= com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT;
            } else if (((triggers & GeofenceProvider.TRIGGER_ON_ENTER) == GeofenceProvider.TRIGGER_ON_ENTER)) {
                Log.d(Constants.LOG, "ID: " + this.id + " trigger on ENTER");
                transition |= com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER;
            } else if (((triggers & GeofenceProvider.TRIGGER_ON_EXIT) == GeofenceProvider.TRIGGER_ON_EXIT)) {
                Log.d(Constants.LOG, "ID: " + this.id + " trigger on EXIT");
                transition |= com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT;
            }
            Log.d(Constants.LOG, "Transition: " + transition);

            if (transition == 0) {
                return null;
            }

            return new com.google.android.gms.location.Geofence.Builder()
                    .setRequestId(this.id)
                    .setTransitionTypes(transition)
                    .setCircularRegion(
                            this.latitude,
                            this.longitude,
                            this.radius)
                    .setExpirationDuration(com.google.android.gms.location.Geofence.NEVER_EXPIRE)
                    .build();
        }
    }
}
