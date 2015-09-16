package li.itcc.hackathon15.poiadd;

import java.io.File;
import java.io.RandomAccessFile;

import android.content.Context;
import android.os.AsyncTask;

import li.itcc.hackathon15.PoiConstants;
import li.itcc.hackathon15.database.PoiTableUpdater;
import li.itcc.hackathon15.services.PoiServices;
import li.itcc.hackaton15.backend.poiApi.model.PoiCreateBean;
import li.itcc.hackaton15.backend.poiApi.model.PoiCreateResultBean;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiAddSaver {
    private final Context fContext;
    private final PoiAddSaveDoneListener fListener;
    private File fLocalImageFile;

    public void setLocalImageFile(File localImageFile) {
        fLocalImageFile = localImageFile;
    }

    public interface PoiAddSaveDoneListener {
        void onDetailSaved(PoiCreateResultBean newBean, Throwable th);
    }

    public PoiAddSaver(Context context, PoiAddSaveDoneListener listener) {
        fContext = context;
        fListener = listener;
    }

    public void save(PoiCreateBean bean) {
        new SaveTask().execute(bean);
    }

    private class SaveTask extends AsyncTask<PoiCreateBean, Void, PoiCreateResultBean> {
        private Throwable fException;

        public SaveTask() {
        }


        @Override
        protected PoiCreateResultBean doInBackground(PoiCreateBean... params) {
            try {
                RandomAccessFile r = null;
                try {
                    PoiCreateBean param = params[0];
                    // resolve the image file in this thread
                    File imageFile = fLocalImageFile;
                    String url = uploadBlob(imageFile);
                    param.setImageBlobUrl(url);
                    // insert
                    PoiServices poiServices = new PoiServices(fContext, PoiConstants.URL);
                    PoiCreateResultBean result = poiServices.insertPoi(param);
                    // insert the result into the database
                    PoiTableUpdater updater = new PoiTableUpdater(fContext);
                    updater.insertPoiOverview(param, result);
                    return result;
                }
                finally {
                    if (r != null) {
                        r.close();
                    }
                }
            }
            catch (Throwable th) {
                fException = th;
            }
            return null;
        }

        private String uploadBlob(File imageFile) {
            // TODO: upload blob
            return null;
        }

        @Override
        protected void onPostExecute(PoiCreateResultBean newBean) {
            super.onPostExecute(newBean);
            if (fListener != null) {
                fListener.onDetailSaved(newBean, fException);
            }
        }
    }
}
