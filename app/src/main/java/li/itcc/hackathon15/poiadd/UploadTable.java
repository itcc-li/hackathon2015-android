package li.itcc.hackathon15.poiadd;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import li.itcc.hackathon15.backend.poiApi.model.PoiCreateBean;
import li.itcc.hackathon15.database.DbUtil;

/**
 * Created by Arthur on 25.09.2015.
 */
public class UploadTable {
    private static final String TABLE_POI_UPLOADS = "PoiUploads";
    private static final String _ID = BaseColumns._ID;
    private static final String COL_LATITUDE = "col_latitude";
    private static final String COL_LONGITUDE = "col_longitude";
    private static final String COL_EXACT_LATITUDE = "col_exact_latitude";
    private static final String COL_EXACT_LONGITUDE = "col_exact_longitude";
    private static final String COL_NAME = "col_name";
    private static final String COL_DESCRIPTION = "col_description";
    private static final String COL_LOCAL_IMAGE_FILE = "col_local_image_file";

    public static void createTable(SQLiteDatabase db) {
        String sql = "create table " +
                TABLE_POI_UPLOADS + " (" + _ID +
                " integer primary key autoincrement, " +
                COL_LATITUDE + " real not null, " +
                COL_LONGITUDE + " real not null, " +
                COL_EXACT_LATITUDE + " real, " +
                COL_EXACT_LONGITUDE + " real, " +
                COL_NAME + " string not null, " +
                COL_DESCRIPTION + " string, " +
                COL_LOCAL_IMAGE_FILE + " string);";
        db.execSQL(sql);
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + TABLE_POI_UPLOADS);
    }

    public static SQLiteStatement createInsertStatement(SQLiteDatabase dbWrite) {
        String sql = "insert into " + TABLE_POI_UPLOADS + "(" +
                COL_LATITUDE + "," +
                COL_LONGITUDE + "," +
                COL_EXACT_LATITUDE + "," +
                COL_EXACT_LONGITUDE + "," +
                COL_NAME + "," +
                COL_DESCRIPTION + ") values (?,?,?,?,?,?)";
        return dbWrite.compileStatement(sql);
    }

    public static void bindToInsertStatement(SQLiteStatement stmt, PoiCreateBean detail) {
        stmt.bindDouble(1, detail.getLatitude());
        stmt.bindDouble(2, detail.getLongitude());
        DbUtil.bindDoubleOrNull(3, stmt, detail.getExactLatitude());
        DbUtil.bindDoubleOrNull(4, stmt, detail.getExactLongitude());
        stmt.bindString(5, detail.getPoiName());
        DbUtil.bindStringOrNull(6, stmt, detail.getPoiDescription());
    }

    public static SQLiteStatement createUpdateStatement(SQLiteDatabase dbWrite) {
        String sql = "update " + TABLE_POI_UPLOADS + " set " +
                COL_LOCAL_IMAGE_FILE + "=?" +
                " where " + _ID + "=?;";
        return dbWrite.compileStatement(sql);
    }

    public static void bindToUpdateStatement(SQLiteStatement stmt, long id, String fileName) {
        DbUtil.bindStringOrNull(1, stmt, fileName);
        stmt.bindLong(2, id);
    }

    public static Cursor executeSelectUploadsStatement(SQLiteDatabase db) {
        String sql = "select " +
                _ID + "," +
                COL_LATITUDE + "," +
                COL_LONGITUDE + "," +
                COL_EXACT_LATITUDE + "," +
                COL_EXACT_LONGITUDE + "," +
                COL_NAME + "," +
                COL_DESCRIPTION + "," +
                COL_LOCAL_IMAGE_FILE +
                " from " + TABLE_POI_UPLOADS + ";";
        return db.rawQuery(sql, null);
    }

    public static long loadIdFromCursor(Cursor c) {
        return c.getLong(0);
    }

    public static PoiCreateBean loadBeanFromCursor(Cursor c) {
        PoiCreateBean bean = new PoiCreateBean();
        bean.setLatitude(c.getDouble(1));
        bean.setLongitude(c.getDouble(2));
        bean.setExactLatitude(DbUtil.getDoubleOrNull(c, 3));
        bean.setExactLongitude(DbUtil.getDoubleOrNull(c, 4));
        bean.setPoiName(c.getString(5));
        bean.setPoiDescription(DbUtil.getStringOrNull(c, 6));
        return bean;
    }

    public static String loadBlobNameFromCursor(Cursor c) {
        return c.getString(7);
    }

    public static void executeDeleteStatement(SQLiteDatabase db, long id) {
        String sql = "delete from " + TABLE_POI_UPLOADS +
                " where " + _ID  + "=?;";
        SQLiteStatement stmt = db.compileStatement(sql);
        stmt.bindLong(1, id);
        stmt.executeUpdateDelete();
    }

}
