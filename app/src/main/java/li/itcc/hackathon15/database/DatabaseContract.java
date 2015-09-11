package li.itcc.hackathon15.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Arthur on 12.12.2014.
 */
public class DatabaseContract {

    public static final String AUTHORITY = "li.itcc.hackathon15";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static class Books {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/books");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.li.itcc.hackathon15.book";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.li.itcc.hackathon15.book";

        public static final String _ID = BaseColumns._ID;
        public static final String BOOK_ID = "book_id";
        public static final String BOOK_NAME = "book_name";
        public static final String BOOK_LONGITUDE = "book_longitude";
        public static final String BOOK_LATITUDE = "book_latitude";
    }
}
