package io.kida.geofancy.app.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
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

    public void addItem(Geofence item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class Geofence {
        public String id;
        public String content;

        public Geofence(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
