package li.itcc.hackathon15.poidetail;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import li.itcc.hackathon15.backend.poiApi.model.PoiDetailBean;
import li.itcc.hackathon15.database.DbUtil;

/**
 * Created by Arthur on 25.09.2015.
 */
public class PoiDetailTable {
    private static final String TABLE_POI_DETAILS = "PoiDetails";
    private static final String _ID = BaseColumns._ID;
    private static final String COL_DESCRIPTION = "col_description";

    public static void createTable(SQLiteDatabase db) {
        String sql = "create table " +
                TABLE_POI_DETAILS + " (" + _ID +
                " string primary key, " +
                COL_DESCRIPTION + " string);";
        db.execSQL(sql);
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + TABLE_POI_DETAILS);
    }

    public static SQLiteStatement createInsertStatement(SQLiteDatabase db) {
        String sql = "insert into " + TABLE_POI_DETAILS + "(" +
                _ID + "," +
                COL_DESCRIPTION + ") values (?,?)";
        return db.compileStatement(sql);
    }

    public static void bindToInsertStatement(SQLiteStatement stmt, PoiDetailBean detail) {
        stmt.bindString(1, detail.getOverview().getUuid());
        DbUtil.bindStringOrNull(2, stmt, detail.getDescription());
    }

    public static SQLiteStatement createSelectStatement(SQLiteDatabase db) {
        String sql = "select " +
                COL_DESCRIPTION +
                " from " + TABLE_POI_DETAILS +
                " where _ID = ?;";
        return db.compileStatement(sql);
    }

    public static void bindToSlectStatement(SQLiteStatement stmt, String uuid) {
        stmt.bindString(1, uuid);
    }

    public static PoiDetailBean loadBeanFromCursor(Cursor c) {
        PoiDetailBean bean = new PoiDetailBean();
        bean.setDescription(DbUtil.getStringOrNull(c, 0));
        return bean;
    }
}
