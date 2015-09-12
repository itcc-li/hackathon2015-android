package li.itcc.hackathon15.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class PoiDBOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SummitBookDatabase.db";
    private static final int DATABASE_VERSION = 1;

    // SQL Statement to create a new database.
    private static final String DATABASE_CREATE = "create table " +
    PoiDatabaseConstants.TABLE_POIS + " (" + DatabaseContract.Pois._ID +
    " integer primary key autoincrement, " +
    DatabaseContract.Pois.POI_ID + " integer, " +
    DatabaseContract.Pois.POI_LONGITUDE + " real, " +
    DatabaseContract.Pois.POI_LATITUDE + " real, " +
    DatabaseContract.Pois.POI_NAME + " string not null);";

    public PoiDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when no database exists in disk and the helper class needs
    // to create a new one.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    // Called when there is a database version mismatch meaning that
    // the version of the database on disk needs to be upgraded to
    // the current version.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Log the version upgrade.
        Log.w("TaskDBAdapter", "Upgrading from version " +
        oldVersion + " to " +
        newVersion + ", which will destroy all old data");

        // Upgrade the existing database to conform to the new
        // version. Multiple previous versions can be handled by
        // comparing oldVersion and newVersion values.

        // The simplest case is to drop the old table and create a new one.
        db.execSQL("DROP TABLE IF IT EXISTS " + PoiDatabaseConstants.TABLE_POIS);
        // Create a new one.
        onCreate(db);
    }
}
