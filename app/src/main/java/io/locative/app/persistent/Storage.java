package io.locative.app.persistent;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import io.locative.app.R;
import io.locative.app.model.Geofences;

public enum Storage {

    // TODO try to centralize storage access here

    INSTANCE;

    public Geofences.Geofence insertOrUpdateFence(Geofences.Geofence fence, Context context) {
        final String QUERY = GeofenceProvider.Geofence.KEY_CUSTOMID + " = ?";
        final String[] PARAMETERS = new String[]{fence.uuid};
        final Uri URL = Uri.parse("content://" + context.getString(R.string.authority) + "/geofences");
        ContentResolver resolver = context.getContentResolver();
        Cursor existingCursor = resolver.query(URL, null, QUERY, PARAMETERS, null);
        try {
            if (existingCursor != null && existingCursor.getCount() > 0) {
                resolver.update(URL, makeContentValuesForGeofence(fence), QUERY, PARAMETERS);
            } else {
                Uri result = resolver.insert(URL, makeContentValuesForGeofence(fence));
                fence.uuid = result.getLastPathSegment();
            }
        }finally {
            if (existingCursor != null) {
                existingCursor.close();
            }
        }

        return fence;
    }

    @NonNull
    private ContentValues makeContentValuesForGeofence(Geofences.Geofence fence) {
        ContentValues values = new ContentValues();
        values.put(GeofenceProvider.Geofence.KEY_NAME, fence.name);
        values.put(GeofenceProvider.Geofence.KEY_ID, fence.uuid);
        values.put(GeofenceProvider.Geofence.KEY_RADIUS, fence.radiusMeters);
        values.put(GeofenceProvider.Geofence.KEY_CUSTOMID, fence.locationId);
        values.put(GeofenceProvider.Geofence.KEY_ENTER_METHOD, fence.enterMethod);
        values.put(GeofenceProvider.Geofence.KEY_ENTER_URL, fence.enterUrl);
        values.put(GeofenceProvider.Geofence.KEY_TRIGGER, fence.triggers);
        values.put(GeofenceProvider.Geofence.KEY_EXIT_METHOD, fence.exitMethod);
        values.put(GeofenceProvider.Geofence.KEY_EXIT_URL, fence.exitUrl);
        values.put(GeofenceProvider.Geofence.KEY_HTTP_AUTH, fence.httpAuth);
        values.put(GeofenceProvider.Geofence.KEY_HTTP_USERNAME, fence.httpUsername);
        values.put(GeofenceProvider.Geofence.KEY_HTTP_PASSWORD, fence.httpPassword);
        values.put(GeofenceProvider.Geofence.KEY_LATITUDE, fence.latitude);
        values.put(GeofenceProvider.Geofence.KEY_LONGITUDE, fence.longitude);
        return values;
    }
}
