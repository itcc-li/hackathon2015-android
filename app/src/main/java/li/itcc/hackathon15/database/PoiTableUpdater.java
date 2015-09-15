package li.itcc.hackathon15.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

import li.itcc.hackathon15.poilist.ThumbnailCache;
import li.itcc.hackaton15.backend.poiApi.model.PoiOverviewBean;
import li.itcc.hackaton15.backend.poiApi.model.PoiOverviewListBean;

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

        dbWrite.execSQL("delete from " + PoiDatabaseConstants.TABLE_POIS);

        String sql = "insert into " + PoiDatabaseConstants.TABLE_POIS + "(" + DatabaseContract.Pois.POI_ID + ",POI_NAME, POI_LONGITUDE, POI_LATITUDE) VALUES (?,?,?,?)";
        SQLiteStatement insert = dbWrite.compileStatement(sql);

        for (PoiOverviewBean poi:listBean.getList()) {
            insert.bindLong(1, poi.getPoiId());
            insert.bindString(2, poi.getPoiName());
            insert.bindDouble(3, poi.getLongitude());
            insert.bindDouble(4, poi.getLatitude());
            insert.execute();
            String thumbnail = poi.getThumbnail();
            byte[] thumbnailData = Base64.decodeBase64(thumbnail);
            fCache.storeBitmap(poi.getPoiId(), thumbnailData);
        }
        fContext.getContentResolver().notifyChange(Uri.parse("content://li.itcc.provider.hackathon15/pois"), null);

    }

}
