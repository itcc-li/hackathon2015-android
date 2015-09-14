package li.itcc.hackathon15.poiadd;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;

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

    public void save(PoiDetailBean bean, Bitmap bitmap) {
        new SaveTask(bitmap).execute(bean);
    }

    private class SaveTask extends AsyncTask<PoiDetailBean, Void, Void> {
        private final Bitmap fBitmap;
        private Throwable fException;

        public SaveTask(Bitmap bitmap) {
            fBitmap = bitmap;
        }


        @Override
        protected Void doInBackground(PoiDetailBean... params) {
            try {
                PoiDetailBean param = params[0];
                if (fBitmap != null) {
                    // compress bitmap to jpeg in background
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    fBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();
                    param.setImage(out.toByteArray());
                }
                PoiServices poiServices = new PoiServices(fContext, TitleHolder.PoiConstants.URL);
                poiServices.savePoiDetails(params[0]);
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
