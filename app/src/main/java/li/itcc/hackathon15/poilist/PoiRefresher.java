package li.itcc.hackathon15.poilist;

import android.content.Context;
import android.os.AsyncTask;

import li.itcc.hackathon15.PoiConstants;
import li.itcc.hackathon15.database.PoiTableUpdater;
import li.itcc.hackathon15.services.PoiOverviewQuery;
import li.itcc.hackathon15.services.PoiServices;
import li.itcc.hackathon15.backend.poiApi.model.PoiOverviewListBean;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiRefresher {
    private final Context fContext;
    private final RefreshDoneListener fListener;

    public interface RefreshDoneListener {
        void onRefreshDone(Throwable th);
    }

    public PoiRefresher(Context context, RefreshDoneListener listener) {
        fContext = context;
        fListener = listener;
    }

    public void refresh() {
        new RefreshTask().execute();
    }

    private class RefreshTask extends AsyncTask<Void, Void, Void> {
        private Throwable fException;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                PoiServices poiServices = new PoiServices(fContext, PoiConstants.URL);
                PoiOverviewQuery q = new PoiOverviewQuery();
                PoiOverviewListBean listBean = poiServices.getPoiList(q);
                PoiTableUpdater poiTableUpdater = new PoiTableUpdater(fContext);
                poiTableUpdater.updatePoiTable(listBean);
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
                fListener.onRefreshDone(fException);
            }
        }
    }
}
