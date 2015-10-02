package li.itcc.hackathon15.poidetail;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.os.OperationCanceledException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import li.itcc.hackathon15.backend.poiApi.model.PoiDetailBean;
import li.itcc.hackathon15.config.CloudEndpoint;
import li.itcc.hackathon15.services.PoiServices;
import li.itcc.hackathon15.util.StreamUtil;

/**
 * Created by Arthur on 12.09.2015.
 */
public class PoiDetailLoader extends AsyncTaskLoader<PoiDetailBean> {
    private static final String KEY_UUID = "KEY_UUID";
    private final Bundle fArgs;

    private PoiDetailBean fResult;

    public static Bundle createArgs(String id) {
        Bundle result = new Bundle();
        result.putString(KEY_UUID, id);
        return result;
    }


    public PoiDetailLoader(Context context, int id, Bundle args) {
        super(context);
        fArgs = args;
    }

    @Override
    public PoiDetailBean loadInBackground() {
        try {
            Context context = getContext();
            String uuid = fArgs.getString(KEY_UUID);
            PoiServices poiServices = new PoiServices(context, CloudEndpoint.URL);
            PoiDetailBean detail = poiServices.getPoiDetails(uuid);
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            // load image if available
            String imageUrl = detail.getImageUrl();
            ImageStore store = new ImageStore(context);
            ImageStore.Key key = store.createKey(detail.getOverview().getUuid(), detail.getImageUpdateTime());
            if (imageUrl != null) {
                // check if download of image is needed
                if (!store.exists(key)) {
                    // patch host for testing
                    imageUrl = CloudEndpoint.dnsHack(imageUrl);
                    InputStream in = new URL(imageUrl).openStream();
                    OutputStream out = store.createImage(key);
                    StreamUtil.pumpAllAndClose(in, out);
                }
            }
            return detail;
        } catch (Exception x) {
            x.printStackTrace();
        }
        return null;
    }

    @Override
    public void deliverResult(PoiDetailBean data) {
        // called on ui thread
        if (isReset()) {
            return;
        }
        PoiDetailBean oldData = fResult;
        fResult = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (fResult != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(fResult);
        }

        if (takeContentChanged() || fResult == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if (fResult != null) {
            fResult = null;
        }
    }



}
