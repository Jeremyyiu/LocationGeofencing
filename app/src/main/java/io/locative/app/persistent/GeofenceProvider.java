package io.locative.app.persistent;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

import de.triplet.simpleprovider.AbstractProvider;
import de.triplet.simpleprovider.Column;
import de.triplet.simpleprovider.Table;
import io.locative.app.R;
import io.locative.app.model.Geofences;

/**
 * Created by kimar on 16.05.14.
 */
public class GeofenceProvider extends AbstractProvider {

    private static int SCHEMA_VERSION = 1;

    public static final int TRIGGER_ON_ENTER = 0x01;
    public static final int TRIGGER_ON_EXIT = 0x02;

    protected String getAuthority() {
        return getContext().getString(R.string.authority);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result =  super.insert(uri, values);
        Toast.makeText(getContext(), "Geofence added", Toast.LENGTH_SHORT)
                .show();
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int result = super.delete(uri, selection, selectionArgs);
        Toast.makeText(getContext(), "Geofence removed", Toast.LENGTH_SHORT)
                .show();
        return result;
    }

    public static Geofences.Geofence fromCursor (Cursor cursor) {
        return new Geofences.Geofence(
                cursor.getString(cursor.getColumnIndex("_id")),
                cursor.getString(cursor.getColumnIndex("name")),
                cursor.getString(cursor.getColumnIndex("custom_id")),
                cursor.getInt(cursor.getColumnIndex("triggers")),
                cursor.getFloat(cursor.getColumnIndex("latitude")),
                cursor.getFloat(cursor.getColumnIndex("longitude")),
                cursor.getInt(cursor.getColumnIndex("radius"))
        );
    }

    @Override
    public int getSchemaVersion() {
        return SCHEMA_VERSION;
    }

    @Table
    public class Geofence {

        @Column(value = Column.FieldType.INTEGER, primaryKey = true)
        public static final String KEY_ID = "_id";

        @Column(value = Column.FieldType.TEXT, unique = true)
        public static final String KEY_CUSTOMID = "custom_id";

        @Column(Column.FieldType.INTEGER)
        public static final String KEY_ENTER_METHOD = "enter_method";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_ENTER_URL = "enter_url";

        @Column(Column.FieldType.INTEGER)
        public static final String KEY_EXIT_METHOD = "exit_method";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_EXIT_URL = "exit_url";

        @Column(Column.FieldType.INTEGER)
        public static final String KEY_HTTP_AUTH = "http_auth";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_HTTP_USERNAME = "http_username";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_HTTP_PASSWORD = "http_password";

        @Column(Column.FieldType.INTEGER)
        public static final String KEY_IBEACON_MINOR = "ibeacon_minor";

        @Column(Column.FieldType.INTEGER)
        public static final String KEY_IBEACON_MAJOR = "ibeacon_major";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_IBEACON_UUID = "ibeacon_uuid";

        @Column(Column.FieldType.FLOAT)
        public static final String KEY_LONGITUDE = "longitude";

        @Column(Column.FieldType.FLOAT)
        public static final String KEY_LATITUDE = "latitude";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_NAME = "name";

        @Column(Column.FieldType.FLOAT)
        public static final String KEY_RADIUS = "radius"; // in meters?

        @Column(Column.FieldType.INTEGER)
        public static final String KEY_TRIGGER = "triggers";

        @Column(Column.FieldType.INTEGER)
        public static final String KEY_TYPE = "type";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_UUID = "uuid";
    }
}
