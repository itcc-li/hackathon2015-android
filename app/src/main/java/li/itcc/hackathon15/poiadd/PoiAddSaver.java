package li.itcc.hackathon15.poiadd;

import java.io.File;
import java.io.RandomAccessFile;

import android.content.Context;
import android.os.AsyncTask;

import li.itcc.hackathon15.TitleHolder;
import li.itcc.hackathon15.services.PoiDetailBean;
import li.itcc.hackathon15.services.PoiServices;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiAddSaver {
    private final Context fContext;
    private final PoiAddSaveDoneListener fListener;

    public interface PoiAddSaveDoneListener {
        void onDetailSaved(Throwable th);
    }

    public PoiAddSaver(Context context, PoiAddSaveDoneListener listener) {
        fContext = context;
        fListener = listener;
    }

    public void save(PoiDetailBean bean) {
        new SaveTask().execute(bean);
    }

    private class SaveTask extends AsyncTask<PoiDetailBean, Void, Void> {
        private Throwable fException;

        public SaveTask() {
        }


        @Override
        protected Void doInBackground(PoiDetailBean... params) {
            try {
                RandomAccessFile r = null;
                try {
                    PoiDetailBean param = params[0];
                    // resolve the image file in this thread
                    File imageFile = param.getImageFile();
                    if (imageFile.exists() && imageFile.canRead()) {
                        r = new RandomAccessFile(imageFile, "r");
                        byte[] dest = new byte[(int)imageFile.length()];
                        r.readFully(dest);
                        r.close();
                        r = null;
                        param.setImage(dest);
                    }
                    PoiServices poiServices = new PoiServices(fContext, TitleHolder.PoiConstants.URL);
                    poiServices.savePoiDetails(param);
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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (fListener != null) {
                fListener.onDetailSaved(fException);
            }
        }
    }
}
