package io.locative.app;

import de.triplet.simpleprovider.AbstractProvider;
import de.triplet.simpleprovider.Column;
import de.triplet.simpleprovider.Table;

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
    public int getSchemaVersion() {
        return SCHEMA_VERSION;
    }

    @Table
    public class Geofence {

        @Column(value = Column.FieldType.INTEGER, primaryKey = true)
        public static final String KEY_ID = "_id";

        @Column(Column.FieldType.TEXT)
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
        public static final String KEY_RADIUS = "radius";

        @Column(Column.FieldType.INTEGER)
        public static final String KEY_TRIGGER = "triggers";

        @Column(Column.FieldType.INTEGER)
        public static final String KEY_TYPE = "type";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_UUID = "uuid";
    }
}
