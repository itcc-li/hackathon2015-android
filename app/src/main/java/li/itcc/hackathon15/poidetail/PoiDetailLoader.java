package li.itcc.hackathon15.poidetail;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import android.content.Context;

import li.itcc.hackathon15.config.CloudEndpoint;
import li.itcc.hackathon15.backend.poiApi.model.PoiDetailBean;
import li.itcc.hackathon15.services.PoiServices;
import li.itcc.hackathon15.util.StreamUtil;
import li.itcc.hackathon15.util.loading.GenericTask;
import li.itcc.hackathon15.util.loading.TaskExecutionListener;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiDetailLoader {
    private final Context fContext;
    private final PoiDetailLoaderListener fListener;

    public interface PoiDetailLoaderListener extends TaskExecutionListener<PoiDetailBean> {
    }

    public PoiDetailLoader(Context context, PoiDetailLoaderListener listener) {
        fContext = context;
        fListener = listener;
    }

    public void load(String poiId) {
        new LoadTask(fListener).execute(poiId);
    }

    private class LoadTask extends GenericTask<String, PoiDetailBean> {

        public LoadTask(PoiDetailLoaderListener listener) {
            super(listener);
        }

        @Override
        protected PoiDetailBean doInBackgroundOrThrow(String... params) throws Exception {
            String param = params[0];
            PoiServices poiServices = new PoiServices(fContext, CloudEndpoint.URL);
            PoiDetailBean detail = poiServices.getPoiDetails(params[0]);
            // load image if available
            String imageUrl = detail.getImageUrl();
            ImageStore store = new ImageStore(fContext);
            ImageStore.Key key = store.createKey(detail.getOverview().getUuid(), detail.getImageUpdateTime());
            if (imageUrl != null) {
                // check if download of image is needed
                if (!store.exists(key)) {
                    // patch host for testing
                    imageUrl = CloudEndpoint.dnsHack(imageUrl);
                    InputStream in = new URL(imageUrl).openStream();
                    OutputStream out = store.createImage(key);
                    StreamUtil.pumpAllAndClose(in, out, detail.getImageSize(), this);
                }
            }
            return detail;
        }
    }
}
