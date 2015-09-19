package li.itcc.hackathon15.poiadd;

import java.io.File;
import java.io.RandomAccessFile;

import android.content.Context;
import android.os.AsyncTask;

import li.itcc.hackathon15.PoiConstants;
import li.itcc.hackathon15.backend.poiApi.model.PoiCreateBean;
import li.itcc.hackathon15.backend.poiApi.model.PoiOverviewBean;
import li.itcc.hackathon15.database.PoiTableUpdater;
import li.itcc.hackathon15.services.PoiServices;
import li.itcc.hackathon15.util.ProgressListener;
import li.itcc.hackathon15.util.ThrowableListener;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiAddSaver {
    private final Context fContext;
    private final PoiAddSaveProgressListener fListener;
    private File fLocalImageFile;

    public void setLocalImageFile(File localImageFile) {
        fLocalImageFile = localImageFile;
    }

    public interface PoiAddSaveProgressListener extends ProgressListener, ThrowableListener {

        void onPoiSaved(PoiOverviewBean newBean);

    }

    public PoiAddSaver(Context context, PoiAddSaveProgressListener listener) {
        fContext = context;
        fListener = listener;
    }

    public void save(PoiCreateBean bean) {
        new SaveTask().execute(bean);
    }

    private class SaveTask extends AsyncTask<PoiCreateBean, Integer, PoiOverviewBean> implements ProgressListener {
        private Throwable fException;

        public SaveTask() {
        }

        @Override
        protected PoiOverviewBean doInBackground(PoiCreateBean... params) {
            try {
                RandomAccessFile r = null;
                try {
                    PoiCreateBean param = params[0];
                    PoiServices poiServices = new PoiServices(fContext, PoiConstants.URL);
                    // 1. upload the image file and get blob key
                    File imageFile = fLocalImageFile;
                    ImageUploader uploader = new ImageUploader(poiServices, this);
                    String imageBlobKey = uploader.uploadImage(imageFile);
                    param.setImageBlobKey(imageBlobKey);
                    // 2. insert bean to cloud
                    PoiOverviewBean result = poiServices.insertPoi(param);
                    // 3. insert the result into the local database
                    PoiTableUpdater updater = new PoiTableUpdater(fContext);
                    updater.insertPoiOverview(result);
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

        @Override
        public void onProgressChanged(int percentage) {
            // method is called in the dedicated thread
            publishProgress(percentage);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // method is called in the UI thread
            super.onProgressUpdate(values);
            if (fListener != null) {
                fListener.onProgressChanged(values[0]);
            }
        }

        @Override
        protected void onPostExecute(PoiOverviewBean newBean) {
            super.onPostExecute(newBean);
            if (fListener != null) {
                if (fException != null) {
                    fListener.onThrowableOccurred(fException);
                }
                else {
                    fListener.onPoiSaved(newBean);
                }
            }
        }

    }

}
