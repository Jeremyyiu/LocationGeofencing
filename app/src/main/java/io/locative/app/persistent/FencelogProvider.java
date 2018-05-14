package io.locative.app.persistent;

import io.locative.app.R;

import de.triplet.simpleprovider.AbstractProvider;
import de.triplet.simpleprovider.Column;
import de.triplet.simpleprovider.Table;

public class FencelogProvider extends AbstractProvider {

    private static int SCHEMA_VERSION = 1;

    @Override
    protected String getAuthority() {
        return getContext().getString(R.string.authority);
    }

    @Override
    public int getSchemaVersion() {
        return SCHEMA_VERSION;
    }

    @Table
    public class Fencelog {

        @Column(value = Column.FieldType.INTEGER, primaryKey = true)
        public static final String KEY_ID = "_id";

        @Column(Column.FieldType.FLOAT)
        public static final String KEY_LONGITUDE = "longitude";

        @Column(Column.FieldType.FLOAT)
        public static final String KEY_LATITUDE = "latitude";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_LOCATION_ID = "locationId";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_HTTP_URL = "httpUrl";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_HTTP_METHOD = "httpMethod";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_HTTP_RESPONSE_CODE = "httpResponseCode";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_HTTP_RESPONSE = "httpResponse";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_EVENT_TYPE = "eventType";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_FENCE_TYPE = "fenceType";

        @Column(Column.FieldType.TEXT)
        public static final String KEY_ORIGIN = "origin";
    }
}
