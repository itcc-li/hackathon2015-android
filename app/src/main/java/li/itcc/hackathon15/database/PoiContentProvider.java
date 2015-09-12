package li.itcc.hackathon15.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by Arthur on 26.11.2014.
 */
public class PoiContentProvider extends ContentProvider {

    //Create the constants used to differentiate between the different URI
    //requests.
    private static final int ALL_ROWS = 1;
    private static final int SINGLE_ROW = 2;

    private static final UriMatcher devicesUriMatcher;

    static {
        devicesUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        devicesUriMatcher.addURI(DatabaseContract.AUTHORITY, "pois", ALL_ROWS);
        devicesUriMatcher.addURI(DatabaseContract.AUTHORITY, "pois/#", SINGLE_ROW);
    }

    private PoiDBOpenHelper dbOpenHelper;

    @Override
    public boolean onCreate() {
        dbOpenHelper = new PoiDBOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db;
        try {
            db = dbOpenHelper.getWritableDatabase();
        }
        catch (SQLiteException ex) {
            db = dbOpenHelper.getReadableDatabase();
        }
        String groupBy = null;
        String having = null;
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch(devicesUriMatcher.match(uri)) {
        case SINGLE_ROW:
            String rowID = uri.getPathSegments().get(1);
            queryBuilder.appendWhere(DatabaseContract.Pois._ID + "=" + rowID);
        default:
            break;
        }
        queryBuilder.setTables(PoiDatabaseConstants.TABLE_POIS);
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch(devicesUriMatcher.match(uri)) {
        case ALL_ROWS:
            return DatabaseContract.Pois.CONTENT_TYPE;
        case SINGLE_ROW:
            return DatabaseContract.Pois.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        switch(devicesUriMatcher.match(uri)) {
        case SINGLE_ROW:
            String rowID = uri.getPathSegments().get(1);
            selection = DatabaseContract.Pois._ID + "=" + rowID + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
        default:
            break;
        }
        // To return the number of deleted items you must specify a where
        // clause. To delete all rows and return a value pass in "1".
        if (selection == null) {
            selection = "1";
        }
        int deleteCount = db.delete(PoiDatabaseConstants.TABLE_POIS, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return deleteCount;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        long id = db.insert(PoiDatabaseConstants.TABLE_POIS, null, values);
        if (id > -1) {
            Uri insertedId = ContentUris.withAppendedId(DatabaseContract.Pois.CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(insertedId, null);
            return insertedId;
        }
        else {
            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        switch(devicesUriMatcher.match(uri)) {
        case SINGLE_ROW:
            String rowID = uri.getPathSegments().get(1);
            selection = DatabaseContract.Pois._ID + "=" + rowID + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
        default:
            break;
        }
        int updateCount = db.update(PoiDatabaseConstants.TABLE_POIS, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }
}
