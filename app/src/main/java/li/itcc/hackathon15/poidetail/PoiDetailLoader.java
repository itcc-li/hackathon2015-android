package li.itcc.hackathon15.poidetail;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;

import li.itcc.hackathon15.TitleHolder;
import li.itcc.hackathon15.services.PoiDetailBean;
import li.itcc.hackathon15.services.PoiDetailServices;
import li.itcc.hackathon15.services.PoiFullDetailBean;
import li.itcc.hackathon15.services.PoiServices;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiDetailLoader {
    private final Context fContext;
    private final PoiDetailLoadDoneListener fListener;

    public interface PoiDetailLoadDoneListener {
        void onPoiDetailsLoaded(PoiFullDetailBean data, Throwable th);
    }

    public PoiDetailLoader(Context context, PoiDetailLoadDoneListener listener) {
        fContext = context;
        fListener = listener;
    }

    public void load(long poiId) {
        new LoadTask().execute(poiId);
    }

    private class LoadTask extends AsyncTask<Long, Void, PoiFullDetailBean> {
        private Throwable fException;

        @Override
        protected PoiFullDetailBean doInBackground(Long... params) {
            try {
                Long param = params[0];
                PoiDetailServices poiServices = new PoiDetailServices(fContext, TitleHolder.PoiConstants.URL);
                return poiServices.loadFullDetails(params[0].longValue());
            }
            catch (Throwable th) {
                fException = th;
            }
            return null;
        }

        @Override
        protected void onPostExecute(PoiFullDetailBean result) {
            super.onPostExecute(result);
            if (fListener != null) {
                fListener.onPoiDetailsLoaded(result, fException);
            }
        }
    }
}
