package li.itcc.hackathon15.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;

import li.itcc.hackathon15.poilist.ThumbnailCache;
import li.itcc.hackathon15.services.PoiBean;
import li.itcc.hackathon15.services.PoiListBean;

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

    public void updatePoiTable(PoiListBean listBean) throws Exception {

        dbOpenHelper = new PoiDBOpenHelper(fContext);
        SQLiteDatabase dbWrite = dbOpenHelper.getWritableDatabase();

        dbWrite.execSQL("delete from " + PoiDatabaseConstants.TABLE_POIS);

        String sql = "insert into " + PoiDatabaseConstants.TABLE_POIS + "(" + DatabaseContract.Pois.POI_ID + ",POI_NAME, POI_LONGITUDE, POI_LATITUDE) VALUES (?,?,?,?)";
        SQLiteStatement insert = dbWrite.compileStatement(sql);

        for (int i = 0; i < listBean.getAllPolis().length; i++) {
            PoiBean poi = listBean.getAllPolis()[i];
            insert.bindLong(1, poi.getId());
            insert.bindString(2, poi.getPoiName());
            insert.bindDouble(3, poi.getLongitude());
            insert.bindDouble(4, poi.getLatitude());
            insert.execute();
            fCache.storeBitmap(poi.getId(), poi.getThumbnail());
        }
        fContext.getContentResolver().notifyChange(Uri.parse("content://li.itcc.provider.hackathon15/pois"), null);

    }

}
