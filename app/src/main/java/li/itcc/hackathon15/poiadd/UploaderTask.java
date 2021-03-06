package li.itcc.hackathon15.poiadd;

import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import li.itcc.hackathon15.ReleaseConfig;
import li.itcc.hackathon15.backend.poiApi.model.PoiCreateBean;
import li.itcc.hackathon15.backend.poiApi.model.PoiOverviewBean;
import li.itcc.hackathon15.database.PoiDBOpenHelper;
import li.itcc.hackathon15.database.PoiTableUpdater;
import li.itcc.hackathon15.services.PoiServices;

/**
 * Created by Arthur on 12.09.2015.
 */
public class UploaderTask extends AsyncTask<Void, Void, Void> {
    private final Context fContext;

    public UploaderTask(Context context) {
      fContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        SQLiteDatabase db = null;
        try {
            // check if we have anything to upload
            PoiDBOpenHelper dbOpenHelper = new PoiDBOpenHelper(fContext);
            db = dbOpenHelper.getWritableDatabase();
            PoiServices poiServices = new PoiServices(fContext, ReleaseConfig.URL);
            while (true) {
                if (!executeUpload(db, poiServices)) {
                    break;
                }
            }
        }
        catch (Exception x) {
            Log.e("UPLOAD", "Upload failed", x);
        }
        finally {
            if (db != null) {
                db.close();
            }
        }
        return null;
    }

    private boolean executeUpload(SQLiteDatabase db, PoiServices poiServices) throws Exception {
        db.beginTransaction();
        try {
            Cursor c = UploadTable.executeSelectUploadsStatement(db);
            if (c.getCount() == 0) {
                return false;
            }
            c.moveToFirst();
            PoiCreateBean param = UploadTable.loadBeanFromCursor(c);
            String uuid = param.getUuid();
            String fileName = UploadTable.loadBlobNameFromCursor(c);
            if (fileName != null) {
                InputStream in = fContext.openFileInput(fileName);
                if (in != null) {
                    ImageUploader uploader = new ImageUploader(poiServices);
                    // 1. upload the image file and get blob key
                    String imageBlobKey = uploader.uploadImage(in);
                    param.setImageBlobKey(imageBlobKey);
                }
            }
            // 2. insert bean to cloud
            PoiOverviewBean result = poiServices.insertPoi(param);
            // 3. insert the result into the local database
            PoiTableUpdater updater = new PoiTableUpdater(fContext);
            updater.insertPoiOverview(db, result);
            // delete local copy
            UploadTable.executeDeleteStatement(db, uuid);
            if (fileName != null) {
                // delete local file
                fContext.deleteFile(fileName);
            }
            db.setTransactionSuccessful();
            return true;
        }
        finally {
            db.endTransaction();
        }
    }

}
