package li.itcc.hackathon15.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

import li.itcc.hackathon15.backend.poiApi.model.PoiOverviewBean;
import li.itcc.hackathon15.backend.poiApi.model.PoiOverviewListBean;
import li.itcc.hackathon15.poilist.ThumbnailCache;

/**
 * Created by patrik on 12/09/2015.
 */
public class PoiTableUpdater  {
    private final Context fContext;
    private final ThumbnailCache fCache;
    private PoiDBOpenHelper dbOpenHelper;

    public PoiTableUpdater(Context context) {
        fContext = context;
        fCache = new ThumbnailCache(context);
    }

    public void updatePoiTable(PoiOverviewListBean listBean) throws Exception {
        dbOpenHelper = new PoiDBOpenHelper(fContext);
        SQLiteDatabase dbWrite = dbOpenHelper.getWritableDatabase();
        // delete full table
        dbWrite.execSQL("delete from " + PoiDatabaseConstants.TABLE_POIS);
        fCache.deleteAllThumbnails();
        SQLiteStatement insertStatement = createInsertStatement(dbWrite);
        for (PoiOverviewBean poi:listBean.getList()) {
            executeInsert(insertStatement, poi);
        }
        insertStatement.close();
        fContext.getContentResolver().notifyChange(DatabaseContract.Pois.CONTENT_URI, null);
    }

    public void insertPoiOverview(PoiOverviewBean result) throws Exception {
        dbOpenHelper = new PoiDBOpenHelper(fContext);
        SQLiteDatabase dbWrite = dbOpenHelper.getWritableDatabase();
        SQLiteStatement insertStatement = createInsertStatement(dbWrite);
        executeInsert(insertStatement, result);
        insertStatement.close();
        fContext.getContentResolver().notifyChange(DatabaseContract.Pois.CONTENT_URI, null);
    }

    private void executeInsert(SQLiteStatement insertStatement, PoiOverviewBean poi) throws Exception {
        insertStatement.bindLong(1, poi.getPoiId());
        insertStatement.bindString(2, poi.getPoiName());
        insertStatement.bindDouble(3, poi.getLongitude());
        insertStatement.bindDouble(4, poi.getLatitude());
        insertStatement.execute();
        String thumbnail = poi.getThumbnailBase64();
        byte[] thumbnailData = Base64.decodeBase64(thumbnail);
        fCache.storeBitmap(poi.getPoiId(), thumbnailData);
    }

    private SQLiteStatement createInsertStatement(SQLiteDatabase dbWrite) {
        //SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        //queryBuilder.query(dbWrite, )
        String sql = "insert into " + PoiDatabaseConstants.TABLE_POIS + "(" +
        DatabaseContract.Pois.POI_ID + "," +
        DatabaseContract.Pois.POI_NAME + "," +
        DatabaseContract.Pois.POI_LONGITUDE + "," +
        DatabaseContract.Pois.POI_LATITUDE + ") values (?,?,?,?)";
        return dbWrite.compileStatement(sql);
    }


}
