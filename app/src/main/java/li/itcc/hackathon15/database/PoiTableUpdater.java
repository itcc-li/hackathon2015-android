package li.itcc.hackathon15.database;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Base64;

import li.itcc.hackathon15.backend.poiApi.model.PoiOverviewBean;
import li.itcc.hackathon15.backend.poiApi.model.PoiOverviewListBean;
import li.itcc.hackathon15.util.ThumbnailCache;
import li.itcc.hackathon15.util.loading.TaskProgressListener;

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

    public void updatePoiTable(TaskProgressListener listener, PoiOverviewListBean listBean) throws Exception {
        dbOpenHelper = new PoiDBOpenHelper(fContext);
        SQLiteDatabase dbWrite = dbOpenHelper.getWritableDatabase();
        // delete full table
        dbWrite.execSQL("delete from " + PoiDatabaseConstants.TABLE_POIS);
        fCache.deleteAllThumbnails();
        listener.onTaskProgress(80);
        List<PoiOverviewBean> list = listBean.getList();
        if (list != null) {
            SQLiteStatement insertStatement = PoiOverviewTable.createInsertStatement(dbWrite);
            int insertCount = listBean.getList().size();
            int doneInserts = 0;
            for (PoiOverviewBean poi : listBean.getList()) {
                executeInsert(insertStatement, poi);
                doneInserts++;
                int progress = 80 * (20 * doneInserts / insertCount);
                listener.onTaskProgress(progress);
            }
            insertStatement.close();
        }
        fContext.getContentResolver().notifyChange(DatabaseContract.Pois.CONTENT_URI, null);
    }

    public void insertPoiOverview(SQLiteDatabase db, PoiOverviewBean result) throws Exception {
        SQLiteStatement insertStatement = PoiOverviewTable.createInsertStatement(db);
        executeInsert(insertStatement, result);
        insertStatement.close();
        fContext.getContentResolver().notifyChange(DatabaseContract.Pois.CONTENT_URI, null);
    }



    private void executeInsert(SQLiteStatement insertStatement, PoiOverviewBean poi) throws Exception {
        insertStatement.bindString(1, poi.getUuid());
        insertStatement.bindString(2, poi.getName());
        String shortDescription = poi.getShortDescription();
        if (shortDescription == null) {
            insertStatement.bindNull(3);
        }
        else {
            insertStatement.bindString(3, shortDescription);
        }
        insertStatement.bindDouble(4, poi.getLongitude());
        insertStatement.bindDouble(5, poi.getLatitude());
        insertStatement.execute();
        String thumbnail = poi.getThumbnailBase64();
        if (thumbnail != null && thumbnail.length() > 0) {
            byte[] thumbnailData = android.util.Base64.decode(thumbnail, Base64.DEFAULT);
            fCache.storeBitmap(poi.getUuid(), thumbnailData);
        }
    }



}
