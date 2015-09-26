package li.itcc.hackathon15.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

/**
 * Created by Arthur on 26.09.2015.
 */
public class PoiOverviewTable {


    public static SQLiteStatement createInsertStatement(SQLiteDatabase db) {
        String sql = "insert into " + PoiDatabaseConstants.TABLE_POIS + "(" +
                DatabaseContract.Pois._ID + "," +
                DatabaseContract.Pois.POI_NAME + "," +
                DatabaseContract.Pois.POI_SHORT_DESCRIPTION + "," +
                DatabaseContract.Pois.POI_LONGITUDE + "," +
                DatabaseContract.Pois.POI_LATITUDE + ") values (?,?,?,?,?)";
        return db.compileStatement(sql);
    }

}
