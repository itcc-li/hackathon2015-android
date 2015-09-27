package li.itcc.hackathon15.poilist;

import android.content.Context;

import li.itcc.hackathon15.config.CloudEndpoint;
import li.itcc.hackathon15.backend.poiApi.model.PoiOverviewListBean;
import li.itcc.hackathon15.database.PoiTableUpdater;
import li.itcc.hackathon15.services.PoiOverviewQuery;
import li.itcc.hackathon15.services.PoiServices;
import li.itcc.hackathon15.util.loading.GenericTask;
import li.itcc.hackathon15.util.loading.TaskExecutionListener;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiListLoader {
    private final Context fContext;
    private final PoiListLoaderListener fListener;

    public interface PoiListLoaderListener extends TaskExecutionListener<PoiOverviewListBean> {
    }

    public PoiListLoader(Context context, PoiListLoaderListener listener) {
        fContext = context;
        fListener = listener;
    }

    public void refresh() {
        new RefreshTask(fListener).execute();
    }

    private class RefreshTask extends GenericTask<Void, PoiOverviewListBean> {
        private Throwable fException;

        public RefreshTask(PoiListLoaderListener listener) {
            super(listener);
        }

        @Override
        protected PoiOverviewListBean doInBackgroundOrThrow(Void... params) throws Exception {
            PoiServices poiServices = new PoiServices(fContext, CloudEndpoint.URL);
            PoiOverviewQuery q = new PoiOverviewQuery();
            PoiOverviewListBean listBean = poiServices.getPoiList(q);
            onTaskProgress(70);
            PoiTableUpdater poiTableUpdater = new PoiTableUpdater(fContext);
            poiTableUpdater.updatePoiTable(this, listBean);
            return listBean;
        }

    }
}
