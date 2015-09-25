package li.itcc.hackathon15.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Arthur on 12.12.2014.
 */
public class DatabaseContract {

    public static final String AUTHORITY = "li.itcc.provider.hackathon15";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static class Pois {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/pois");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.li.itcc.hackathon15.poi";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.li.itcc.hackathon15.poi";

        public static final String _ID = BaseColumns._ID;
        public static final String POI_ID = "poi_id";
        public static final String POI_NAME = "poi_name";
        public static final String POI_SHORT_DESCRIPTION = "poi_short_desc";
        public static final String POI_LONGITUDE = "poi_longitude";
        public static final String POI_LATITUDE = "poi_latitude";
    }

}
